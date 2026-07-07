package com.hamric.feature.articles.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hamric.core.model.Article
import com.hamric.core.network.api.NewsApi
import com.hamric.core.network.mapper.toDomainModels
import retrofit2.HttpException
import java.io.IOException

class ArticlePagingSource(
    private val api: NewsApi,
    private val sourceId: String? = null,
    private val searchQuery: String? = null,
    private val sortBy: String = "publishedAt"
) : PagingSource<Int, Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        return try {
            val page = params.key ?: 1
            val pageSize = params.loadSize

            val response = api.getArticles(
                sourceIDs = sourceId ?: "",
                searchKeyword = searchQuery ?: "",
                sortBy = sortBy,
                page = page,
                pageSize = pageSize
            )

            if (response.status != "ok") {
                val errorMessage = response.message ?: "Unknown API error"
                return LoadResult.Error(Exception(errorMessage))
            }

            val totalPages = response.getTotalPages(pageSize)

            LoadResult.Page(
                data = response.articles.toDomainModels(),
                prevKey = if (page > 1) page - 1 else null,
                nextKey = if (page < totalPages) page + 1 else null
            )
        } catch (e: IOException) {
            LoadResult.Error(Exception("Network error. Please check your connection."))
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                429 -> "Rate limit exceeded. Please try again later."
                401 -> "Invalid API key. Please check your configuration."
                else -> "Server error (${e.code()}). Please try again."
            }
            LoadResult.Error(Exception(errorMessage))
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}