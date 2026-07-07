package com.hamric.feature.categories.utils

import com.hamric.core.model.Category
import com.hamric.core.network.response.SourceResponse
import com.hamric.core.network.response.SourcesResponse

object TestDataFactory {
    fun createCategoryList(count: Int = 5): List<Category> {
        val categories = mapOf(
            "business" to "Business",
            "technology" to "Technology",
            "sports" to "Sports",
            "entertainment" to "Entertainment",
            "science" to "Science",
            "health" to "Health"
        )
        return categories.entries.take(count).mapIndexed { index, (id, name) ->
            Category(
                id = id,
                name = name,
                sourcesCount = (10 + index * 5)
            )
        }
    }

    fun createSourceResponse(
        id: String = "bbc-news",
        name: String = "BBC News",
        description: String = "BBC News description",
        url: String = "https://bbc.com",
        category: String = "general",
        language: String = "en",
        country: String = "uk"
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
            createSourceResponse(category = "business", id = "business-1", name = "Business News"),
            createSourceResponse(category = "technology", id = "tech-1", name = "Tech News"),
            createSourceResponse(category = "business", id = "business-2", name = "Business Insider"),
            createSourceResponse(category = "sports", id = "sports-1", name = "Sports News")
        ),
        message: String? = null
    ): SourcesResponse {
        return SourcesResponse(
            status = status,
            sources = sources,
            message = message
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
            code = code
        )
    }
}