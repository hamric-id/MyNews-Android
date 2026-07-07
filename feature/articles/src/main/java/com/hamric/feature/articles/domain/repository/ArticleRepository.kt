package com.hamric.feature.articles.domain.repository

import androidx.paging.PagingData
import com.hamric.core.model.Article
import kotlinx.coroutines.flow.Flow

interface ArticleRepository {
    fun getArticlesBySource(
        sourceId: String,
        searchKeyword: String? = null,
        sortBy: String = "publishedAt"
    ): Flow<PagingData<Article>>
}