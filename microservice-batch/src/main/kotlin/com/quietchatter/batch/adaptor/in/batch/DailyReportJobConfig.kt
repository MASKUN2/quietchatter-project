package com.quietchatter.batch.adaptor.`in`.batch

import com.quietchatter.batch.adaptor.out.discord.DiscordClient
import com.quietchatter.batch.adaptor.out.loki.LokiClient
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.model.ChatModel
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Configuration
class DailyReportJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val lokiClient: LokiClient,
    private val discordClient: DiscordClient,
    private val chatModel: ChatModel,
    @Qualifier("talkJdbcTemplate") private val talkJdbcTemplate: JdbcTemplate,
    @Qualifier("bookJdbcTemplate") private val bookJdbcTemplate: JdbcTemplate
) {
    private val log = LoggerFactory.getLogger(DailyReportJobConfig::class.java)

    @Bean
    fun dailyReportJob(): Job {
        return JobBuilder("dailyReportJob", jobRepository)
            .start(generateDailyReportStep())
            .build()
    }

    @Bean
    fun generateDailyReportStep(): Step {
        return StepBuilder("generateDailyReportStep", jobRepository)
            .tasklet({ _, chunkContext ->
                val targetDateStr = chunkContext.stepContext.jobParameters["targetDate"] as? String
                    ?: LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                
                log.info("Starting Daily Report Generation for {}", targetDateStr)

                val targetDate = LocalDate.parse(targetDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                val startOfDay = targetDate.atStartOfDay()
                val endOfDay = targetDate.plusDays(1).atStartOfDay().minusNanos(1)

                val lokiStats = lokiClient.fetchLokiStats(targetDateStr)
                val dbStats = collectDbStats(startOfDay, endOfDay, lokiStats.bookViews)
                val rawStats = lokiStats.rawStatsText + dbStats
                
                val aiBriefing = generateAiBriefing(rawStats)
                
                val finalReport = "## 📊 일간 서비스 리포트 ($targetDateStr)\n\n$aiBriefing\n\n---\n**상세 지표**\n$rawStats"

                discordClient.sendReport(finalReport)

                log.info("Daily Report Generation completed.")
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }

    private fun collectDbStats(start: LocalDateTime, end: LocalDateTime, bookViews: Map<String, Int>): String {
        val stats = StringBuilder()
        appendReactionStats(stats, start, end)
        appendBookViewStats(stats, bookViews)
        return stats.toString()
    }

    private fun appendReactionStats(stats: StringBuilder, start: LocalDateTime, end: LocalDateTime) {
        try {
            val sql = "SELECT type, COUNT(*) as count FROM reaction WHERE created_at BETWEEN ? AND ? GROUP BY type"
            val rows = talkJdbcTemplate.queryForList(sql, start, end)
            
            stats.append("- 일간 리액션: ")
            if (rows.isEmpty()) {
                stats.append("없음\n")
                return
            }
            rows.forEach { row ->
                val type = row["type"]
                val count = row["count"]
                stats.append("$type($count) ")
            }
            stats.append("\n")
        } catch (e: Exception) {
            log.error("Failed to fetch reaction stats from DB", e)
            stats.append("- 일간 리액션: 조회 실패\n")
        }
    }

    private fun appendBookViewStats(stats: StringBuilder, bookViews: Map<String, Int>) {
        stats.append("- 조회수가 높은 책:\n")
        if (bookViews.isEmpty()) {
            stats.append("  없음\n")
            return
        }
        
        try {
            val bookIds = bookViews.keys.map { UUID.fromString(it) }
            if (bookIds.isEmpty()) {
                stats.append("  없음\n")
                return
            }

            val inSql = StringJoiner(",", "(", ")")
            bookIds.forEach { _ -> inSql.add("?") }
            val sql = "SELECT id, title FROM book WHERE id IN $inSql"
            
            val idToTitle = bookJdbcTemplate.query(sql, { rs, _ ->
                rs.getString("id") to rs.getString("title")
            }, *bookIds.toTypedArray()).toMap()
            
            bookViews.entries.sortedByDescending { it.value }
                .take(5)
                .forEach { (id, count) ->
                    val title = idToTitle[id] ?: "알 수 없는 책 ($id)"
                    stats.append("  - \"$title\" (${count}회)\n")
                }
        } catch (e: Exception) {
            log.error("Failed to fetch book titles from DB", e)
            stats.append("  - 책 정보 조회 실패\n")
        }
    }

    private fun generateAiBriefing(rawStats: String): String {
        val prompt = "다음은 서비스의 전일자 지표입니다. 이 데이터를 바탕으로 어제 서비스의 주요 동향, 특이사항(특히 에러 관련), 그리고 긍정적인 지표를 요약하는 3~4줄의 브리핑 리포트를 작성해주세요.\n\n$rawStats"
        return try {
            chatModel.call(prompt)
        } catch (e: Exception) {
            log.error("AI Briefing generation failed", e)
            "AI 브리핑을 생성할 수 없습니다. (API 설정 확인 필요)"
        }
    }
}
