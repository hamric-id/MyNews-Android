package com.hamric.feature.articles.presentation.state

import com.hamric.core.model.Article

data class ArticlesUiState(
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    val searchQuery: String = "",
    val sortBy: String = "publishedAt",
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val isInitialLoad: Boolean = true
)