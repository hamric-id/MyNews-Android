package com.hamric.feature.articles.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.hamric.core.model.Article
import com.hamric.core.network.api.NewsApi
import com.hamric.feature.articles.data.paging.ArticlePagingSource
import com.hamric.feature.articles.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ArticleRepositoryImpl @Inject constructor(
    private val api: NewsApi
) : ArticleRepository {

    override fun getArticlesBySource(
        sourceId: String,
        searchKeyword: String?,
        sortBy: String
    ): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                prefetchDistance = 2,
                enablePlaceholders = false,
                initialLoadSize = 30
            ),
            pagingSourceFactory = {
                ArticlePagingSource(
                    api = api,
                    sourceId = sourceId,
                    searchQuery = searchKeyword,
                    sortBy = sortBy
                )
            }
        ).flow
    }
}