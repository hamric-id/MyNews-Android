package com.hamric.core.network.api

import com.hamric.core.network.response.ArticlesResponse
import com.hamric.core.network.response.SourcesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("top-headlines/sources")
    suspend fun getSources(
        @Query("language") language: String = "en"
    ): SourcesResponse

    @GET("sources")
    suspend fun getSourcesByCategory(
        @Query("category") category: String,
        @Query("language") language: String = "en"
    ): SourcesResponse

    @GET("everything")
    suspend fun getArticles(
        @Query("sources") sourceIDs: String,
        @Query("q") searchKeyword: String = "",
        @Query("sortBy") sortBy: String = "",
        @Query("language") language: String = "en",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): ArticlesResponse
}