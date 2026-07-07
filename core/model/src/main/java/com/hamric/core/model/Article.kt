package com.hamric.core.model

data class Article(
    val id: String? = null,
    val title: String,
    val description: String? = null,
    val content: String? = null,
    val url: String,
    val urlToImage: String? = null,
    val publishedAt: String,
    val author: String? = null,
    val sourceName: String,
    val sourceId: String? = null
)