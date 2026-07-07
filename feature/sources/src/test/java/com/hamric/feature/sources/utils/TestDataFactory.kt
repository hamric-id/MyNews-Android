package com.hamric.feature.sources.utils

import com.hamric.core.model.Source
import com.hamric.core.network.response.SourceResponse
import com.hamric.core.network.response.SourcesResponse

object TestDataFactory {

    fun createSource(
        id: String = "bbc-news",
        name: String = "BBC News",
        description: String? = "British news broadcaster",
        url: String? = "https://bbc.com",
        category: String? = "general",
        language: String? = "en",
        country: String? = "gb"
    ): Source {
        return Source(
            id = id,
            name = name,
            description = description,
            url = url,
            category = category,
            language = language,
            country = country
        )
    }

    fun createSourceList(count: Int = 5): List<Source> {
        val sources = listOf(
            "BBC News" to "general",
            "CNN" to "general",
            "TechCrunch" to "technology",
            "ESPN" to "sports",
            "The Guardian" to "general"
        )
        return sources.take(count).mapIndexed { index, (name, category) ->
            createSource(
                id = "source-$index",
                name = name,
                description = "$name description",
                category = category,
                country = "us",
                language = "en"
            )
        }
    }

    fun createSourceResponse(
        id: String = "bbc-news",
        name: String = "BBC News",
        description: String? = "British news broadcaster",
        url: String? = "https://bbc.com",
        category: String = "general",
        language: String = "en",
        country: String = "gb"
    ): SourceResponse {
        return SourceResponse(
            id = id,
            name = name,
            description = description,
            url = url,
            category = category,
            language = language,
            country = country
        )
    }

    fun createSourcesResponse(
        status: String = "ok",
        sources: List<SourceResponse> = listOf(
            createSourceResponse(category = "general", id = "bbc-news", name = "BBC News"),
            createSourceResponse(category = "general", id = "cnn", name = "CNN"),
            createSourceResponse(category = "technology", id = "techcrunch", name = "TechCrunch"),
            createSourceResponse(category = "sports", id = "espn", name = "ESPN"),
            createSourceResponse(category = "general", id = "guardian", name = "The Guardian")
        ),
        message: String? = null,
        totalResults: Int = 5,
        page: Int = 1,
        totalPages: Int = 1
    ): SourcesResponse {
        return SourcesResponse(
            status = status,
            sources = sources,
            message = message,
            totalResults = totalResults,
            page = page,
            totalPages = totalPages
        )
    }

    fun createErrorResponse(
        message: String = "API key invalid",
        code: String = "apiKeyInvalid"
    ): SourcesResponse {
        return SourcesResponse(
            status = "error",
            sources = emptyList(),
            message = message,
            code = code,
            totalResults = 0,
            page = 1,
            totalPages = 0
        )
    }
}