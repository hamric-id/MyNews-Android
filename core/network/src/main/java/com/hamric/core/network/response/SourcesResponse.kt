package com.hamric.core.network.response

import com.google.gson.annotations.SerializedName


data class SourcesResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("sources")
    val sources: List<SourceResponse> = emptyList(),
    @SerializedName("totalResults")
    val totalResults: Int = 0,
    @SerializedName("page")
    val page: Int = 1,
    @SerializedName("totalPages")
    val totalPages: Int = 1,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("code")
    val code: String? = null
)

data class SourceResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("category")
    val category: String,
    @SerializedName("language")
    val language: String,
    @SerializedName("country")
    val country: String
)