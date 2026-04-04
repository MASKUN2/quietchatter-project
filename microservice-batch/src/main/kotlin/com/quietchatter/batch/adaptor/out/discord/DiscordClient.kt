package com.quietchatter.batch.adaptor.out.discord

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class DiscordClient(
    restClientBuilder: RestClient.Builder,
    @Value("\${app.discord.webhook-url:}") private val webhookUrl: String
) {
    private val log = LoggerFactory.getLogger(DiscordClient::class.java)
    private val restClient = restClientBuilder.build()

    fun sendReport(message: String) {
        if (webhookUrl.isBlank()) {
            log.warn("Discord webhook URL is not configured. Skipping report delivery.")
            return
        }

        try {
            restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf("content" to message))
                .retrieve()
                .toBodilessEntity()
            log.info("Successfully sent daily report to Discord.")
        } catch (e: Exception) {
            log.error("Failed to send daily report to Discord", e)
        }
    }
}
