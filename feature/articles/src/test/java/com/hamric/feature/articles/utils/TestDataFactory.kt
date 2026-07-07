package com.hamric.feature.articles.utils

import com.hamric.core.model.Article
import com.hamric.core.network.response.ArticleResponse
import com.hamric.core.network.response.ArticleSourceResponse
import com.hamric.core.network.response.ArticlesResponse

object TestDataFactory {


    fun createArticle(
        id: String? = "article-1",
        title: String = "Test Article",
        description: String? = "Test description",
        content: String? = "Test content",
        url: String = "https://example.com/article-1",
        urlToImage: String? = "https://example.com/image.jpg",
        publishedAt: String = "2026-07-08T10:30:00Z",
        author: String? = "John Doe",
        sourceName: String = "Test Source",
        sourceId: String? = "test-source"
    ): Article {
        return Article(
            id = id,
            title = title,
            description = description,
            content = content,
            url = url,
            urlToImage = urlToImage,
            publishedAt = publishedAt,
            author = author,
            sourceName = sourceName,
            sourceId = sourceId
        )
    }

    fun createArticleList(count: Int = 3): List<Article> {
        return (1..count).map { index ->
            createArticle(
                id = "article-$index",
                title = "Test Article $index",
                description = "Description for article $index",
                url = "https://example.com/article-$index",
                sourceName = "Source $index"
            )
        }
    }


    fun createArticleResponse(
        source: ArticleSourceResponse = createArticleSourceResponse(),
        author: String? = "John Doe",
        title: String = "Test Article",
        description: String? = "Test description",
        url: String = "https://example.com/article-1",
        urlToImage: String? = "https://example.com/image.jpg",
        publishedAt: String = "2026-07-08T10:30:00Z",
        content: String? = "Test content"
    ): ArticleResponse {
        return ArticleResponse(
            source = source,
            author = author,
            title = title,
            description = description,
            url = url,
            urlToImage = urlToImage,
            publishedAt = publishedAt,
            content = content
        )
    }

    fun createArticleSourceResponse(
        id: String? = "test-source",
        name: String = "Test Source"
    ): ArticleSourceResponse {
        return ArticleSourceResponse(
            id = id,
            name = name
        )
    }

    fun createArticlesResponse(
        status: String = "ok",
        totalResults: Int = 10,
        articles: List<ArticleResponse> = listOf(
            createArticleResponse(title = "Article 1"),
            createArticleResponse(title = "Article 2"),
            createArticleResponse(title = "Article 3")
        ),
        message: String? = null,
        code: String? = null
    ): ArticlesResponse {
        return ArticlesResponse(
            status = status,
            totalResults = totalResults,
            articles = articles,
            message = message,
            code = code
        )
    }

    fun createErrorResponse(
        message: String = "API key invalid",
        code: String = "apiKeyInvalid"
    ): ArticlesResponse {
        return ArticlesResponse(
            status = "error",
            totalResults = 0,
            articles = emptyList(),
            message = message,
            code = code
        )
    }
}