package com.quietchatter.batch.adaptor.out.loki

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

@Component
class LokiClient(
    restClientBuilder: RestClient.Builder,
    @Value("\${app.loki.url:http://localhost:3100}") private val lokiUrl: String
) {
    private val log = LoggerFactory.getLogger(LokiClient::class.java)
    private val restClient = restClientBuilder.baseUrl(lokiUrl).build()

    companion object {
        private val IP_PATTERN = Pattern.compile("IP: ([\\d\\.]+)")
        private val PRINCIPAL_PATTERN = Pattern.compile("Principal: AuthMember\\[id=([a-f0-9\\-]+)")
        private val URI_PATTERN = Pattern.compile("URI: ([^,]+)")
        private val STATUS_PATTERN = Pattern.compile("Status: (\\d+)")
    }

    fun fetchLokiStats(targetDateStr: String): LokiStats {
        log.info("Fetching Loki stats for date: {}", targetDateStr)
        val targetDate = LocalDate.parse(targetDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
        
        val startUnixNano = targetDate.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli() * 1000000
        val endUnixNano = targetDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli() * 1000000 - 1

        val query = "{container=\"quiet-chatter\"} |= \"MdcFilter\""
        
        val uniqueIps = mutableSetOf<String>()
        val activeUsers = mutableSetOf<String>()
        val errors = mutableMapOf<String, Int>()
        val bookViews = mutableMapOf<String, Int>()
        val searchKeywords = mutableMapOf<String, Int>()

        var currentEnd = endUnixNano
        val limit = 5000

        try {
            while (currentEnd > startUnixNano) {
                val finalCurrentEnd = currentEnd
                val response = restClient.get()
                    .uri { uriBuilder ->
                        uriBuilder
                            .path("/loki/api/v1/query_range")
                            .queryParam("query", query)
                            .queryParam("start", startUnixNano)
                            .queryParam("end", finalCurrentEnd)
                            .queryParam("limit", limit)
                            .build()
                    }
                    .retrieve()
                    .body(LokiResponse::class.java)

                if (response?.data?.result == null || response.data.result.isEmpty()) {
                    break
                }

                processLokiResponse(response, uniqueIps, activeUsers, errors, bookViews, searchKeywords)

                val totalLines = response.data.result.sumOf { it.values.size }

                if (totalLines < limit) {
                    break
                }

                val oldestTs = response.data.result.flatMap { it.values }
                    .map { it[0].toLong() }
                    .minOrNull() ?: startUnixNano

                currentEnd = oldestTs - 1
            }

            return buildStatsText(uniqueIps, activeUsers, errors, bookViews, searchKeywords)
        } catch (e: Exception) {
            log.error("Failed to fetch data from Loki", e)
            return LokiStats("- Loki 데이터 조회 실패: \${e.message}", emptyMap())
        }
    }

    private fun processLokiResponse(
        response: LokiResponse, uniqueIps: MutableSet<String>, activeUsers: MutableSet<String>,
        errors: MutableMap<String, Int>, bookViews: MutableMap<String, Int>,
        searchKeywords: MutableMap<String, Int>
    ) {
        response.data?.result?.flatMap { it.values }
            ?.filter { it.size >= 2 }
            ?.map { it[1] }
            ?.forEach { logLine ->
                if (logLine.contains("Request Start")) {
                    processRequestStart(logLine, uniqueIps, activeUsers, bookViews, searchKeywords)
                }
                if (logLine.contains("Request End")) {
                    processRequestEnd(logLine, errors)
                }
            }
    }

    private fun processRequestStart(
        logLine: String, uniqueIps: MutableSet<String>, activeUsers: MutableSet<String>,
        bookViews: MutableMap<String, Int>, searchKeywords: MutableMap<String, Int>
    ) {
        extractAndAdd(IP_PATTERN, logLine, uniqueIps)
        extractAndAdd(PRINCIPAL_PATTERN, logLine, activeUsers)

        val uriMatcher = URI_PATTERN.matcher(logLine)
        if (uriMatcher.find()) {
            val uri = uriMatcher.group(1).trim()
            if (uri.startsWith("/v1/books/")) {
                extractBookId(uri, bookViews)
            }
            if (uri.startsWith("/v1/books?keyword=")) {
                extractSearchKeyword(uri, searchKeywords)
            }
        }
    }

    private fun processRequestEnd(logLine: String, errors: MutableMap<String, Int>) {
        val statusMatcher = STATUS_PATTERN.matcher(logLine)
        if (statusMatcher.find()) {
            val status = statusMatcher.group(1).toInt()
            if (status >= 400) {
                val errorKey = "[\$status]"
                errors[errorKey] = errors.getOrDefault(errorKey, 0) + 1
            }
        }
    }

    private fun extractAndAdd(pattern: Pattern, text: String, set: MutableSet<String>) {
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            set.add(matcher.group(1))
        }
    }

    private fun extractBookId(uri: String, bookViews: MutableMap<String, Int>) {
        val bookId = uri.substring("/v1/books/".length)
        if (!bookId.contains("?")) {
            bookViews[bookId] = bookViews.getOrDefault(bookId, 0) + 1
        }
    }

    private fun extractSearchKeyword(uri: String, searchKeywords: MutableMap<String, Int>) {
        try {
            val queryString = uri.substring(uri.indexOf("?") + 1)
            for (param in queryString.split("&")) {
                if (param.startsWith("keyword=")) {
                    val rawKeyword = param.substring("keyword=".length)
                    val decodedKeyword = URLDecoder.decode(rawKeyword, StandardCharsets.UTF_8)
                    searchKeywords[decodedKeyword] = searchKeywords.getOrDefault(decodedKeyword, 0) + 1
                    return
                }
            }
        } catch (e: Exception) {
            log.warn("Failed to parse keyword from URI: {}", uri)
        }
    }

    private fun buildStatsText(
        uniqueIps: Set<String>, activeUsers: Set<String>,
        errors: Map<String, Int>, bookViews: Map<String, Int>,
        searchKeywords: Map<String, Int>
    ): LokiStats {
        val sb = StringBuilder()
        sb.append("- 고유 접속 IP 수: \${uniqueIps.size}\n")
        sb.append("- DAU (일간 활성 유저): \${activeUsers.size}\n")

        sb.append("- 에러 발생 현황: ")
        if (errors.isEmpty()) {
            sb.append("없음\n")
        } else {
            errors.forEach { (k, v) -> sb.append("$k (${v}건) ") }
            sb.append("\n")
        }

        sb.append("- 인기 검색어: ")
        if (searchKeywords.isEmpty()) {
            sb.append("없음\n")
        } else {
            searchKeywords.entries.sortedByDescending { it.value }
                .take(5)
                .forEach { e -> sb.append("\"\${e.key}\" (\${e.value}회) ") }
            sb.append("\n")
        }

        return LokiStats(sb.toString(), bookViews)
    }
}

