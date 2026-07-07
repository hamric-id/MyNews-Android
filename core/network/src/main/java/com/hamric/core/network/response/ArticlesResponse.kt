package com.hamric.core.network.response

import com.google.gson.annotations.SerializedName

data class ArticlesResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("totalResults")
    val totalResults: Int = 0,
    @SerializedName("articles")
    val articles: List<ArticleResponse> = emptyList(),
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("code")
    val code: String? = null
)

data class ArticleResponse(
    @SerializedName("source")
    val source: ArticleSourceResponse,
    @SerializedName("author")
    val author: String? = null,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("url")
    val url: String,
    @SerializedName("urlToImage")
    val urlToImage: String? = null,
    @SerializedName("publishedAt")
    val publishedAt: String,
    @SerializedName("content")
    val content: String? = null
)

data class ArticleSourceResponse(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("name")
    val name: String
)