package com.quietchatter.batch.adaptor.out.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class TalkServiceClient(
    restClientBuilder: RestClient.Builder,
    @Value("\${app.service.talk.url:http://microservice-talk:8080}") private val talkUrl: String
) {
    private val restClient = restClientBuilder.baseUrl(talkUrl).build()

    fun getReactionStats(start: LocalDateTime, end: LocalDateTime): Map<String, Long> {
        val response = restClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/v1/talks/internal/reactions/stats")
                    .queryParam("start", start.format(DateTimeFormatter.ISO_DATE_TIME))
                    .queryParam("end", end.format(DateTimeFormatter.ISO_DATE_TIME))
                    .build()
            }
            .retrieve()
            .body(object : ParameterizedTypeReference<Map<String, Long>>() {})

        return response ?: emptyMap()
    }
}
