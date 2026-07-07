package com.hamric.core.network.api

import com.hamric.core.network.BuildConfig
import com.hamric.core.network.response.ArticlesResponse
import com.hamric.core.network.response.SourcesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("top-headlines/sources")
    suspend fun getSources(
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY,
        @Query("language") language: String = "en"
    ): SourcesResponse

    @GET("top-headlines")
    suspend fun getArticlesBySource(
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY,
        @Query("sources") sourceId: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ArticlesResponse

    @GET("everything")
    suspend fun searchArticles(
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY,
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ArticlesResponse
}