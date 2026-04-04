package com.quietchatter.batch.adaptor.out.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class BookServiceClient(
    restClientBuilder: RestClient.Builder,
    @Value("\${app.service.book.url:http://microservice-book:8080}") private val bookUrl: String
) {
    private val restClient = restClientBuilder.baseUrl(bookUrl).build()

    fun getBookTitles(ids: List<String>): Map<String, String> {
        if (ids.isEmpty()) return emptyMap()

        val response = restClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/v1/books/internal/books")
                    .queryParam("ids", ids.joinToString(","))
                    .build()
            }
            .retrieve()
            .body(object : ParameterizedTypeReference<List<BookInternalResponse>>() {})

        return response?.associate { it.id to it.title } ?: emptyMap()
    }
}

data class BookInternalResponse(
    val id: String,
    val title: String
)
