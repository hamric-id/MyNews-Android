package com.hamric.core.network.mapper

import com.hamric.core.model.Article
import com.hamric.core.network.response.ArticleResponse

fun ArticleResponse.toDomainModel(): Article {
    return Article(
        id = url,
        title = title,
        description = description,
        content = content,
        url = url,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        author = author,
        sourceName = source.name,
        sourceId = source.id
    )
}

fun List<ArticleResponse>.toDomainModels(): List<Article> {
    return this.map { it.toDomainModel() }
}