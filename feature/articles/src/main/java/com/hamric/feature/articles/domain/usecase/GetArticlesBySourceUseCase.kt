package com.hamric.feature.articles.domain.usecase

import androidx.paging.PagingData
import com.hamric.core.model.Article
import com.hamric.feature.articles.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class GetArticlesBySourceUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    operator fun invoke(
        sourceId: String,
        searchKeyword: String? = null,
        sortBy: String = "publishedAt"
    ): Flow<PagingData<Article>> {
        return repository.getArticlesBySource(
            sourceId = sourceId,
            searchKeyword = searchKeyword,
            sortBy = sortBy
        )
    }
}