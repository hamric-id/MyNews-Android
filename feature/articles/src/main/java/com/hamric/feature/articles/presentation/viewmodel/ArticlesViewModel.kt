package com.hamric.feature.articles.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hamric.core.model.Article
import com.hamric.feature.articles.domain.usecase.GetArticlesBySourceUseCase
import com.hamric.feature.articles.presentation.state.ArticlesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class ArticlesViewModel @Inject constructor(
    private val getArticlesBySourceUseCase: GetArticlesBySourceUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val SEARCH_QUERY_KEY = "search_query"
    private val SOURCE_ID_KEY = "source_id"
    private val SORT_BY_KEY = "sort_by"

    private val _uiState = MutableStateFlow(
        ArticlesUiState(
            isLoading = true,
            isInitialLoad = true
        )
    )
    val uiState: StateFlow<ArticlesUiState> = _uiState.asStateFlow()

    private val _articles = MutableStateFlow<Flow<PagingData<Article>>?>(null)
    val articles: StateFlow<Flow<PagingData<Article>>?> = _articles.asStateFlow()

    private var currentSourceId: String? = null
    private var currentSearchQuery: String? = null
    private var currentSortBy: String = "publishedAt"

    init {
        savedStateHandle.get<String>(SEARCH_QUERY_KEY)?.let { query ->
            _uiState.update { it.copy(searchQuery = query) }
        }
        savedStateHandle.get<String>(SORT_BY_KEY)?.let { sortBy ->
            _uiState.update { it.copy(sortBy = sortBy) }
            currentSortBy = sortBy
        }
        savedStateHandle.get<String>(SOURCE_ID_KEY)?.let { sourceId ->
            currentSourceId = sourceId
            loadArticles(sourceId)
        }
    }

    fun loadArticles(
        sourceId: String,
        forceRefresh: Boolean = false,
        searchKeyword: String? = null,
        sortBy: String = currentSortBy
    ) {
        if (!forceRefresh && currentSourceId == sourceId &&
            currentSearchQuery == searchKeyword &&
            currentSortBy == sortBy) {
            if (_articles.value != null) {
                _uiState.update { it.copy(isLoading = false, isInitialLoad = false) }
            }
            return
        }

        viewModelScope.launch {
            currentSourceId = sourceId
            currentSearchQuery = searchKeyword
            currentSortBy = sortBy

            savedStateHandle.set(SOURCE_ID_KEY, sourceId)
            savedStateHandle.set(SORT_BY_KEY, sortBy)
            if (searchKeyword != null) savedStateHandle.set(SEARCH_QUERY_KEY, searchKeyword)

            _articles.value = null

            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    searchQuery = searchKeyword ?: "",
                    sortBy = sortBy,
                    isRefreshing = forceRefresh,
                    isInitialLoad = true
                )
            }

            try {
                val pagingFlow = getArticlesBySourceUseCase(
                    sourceId = sourceId,
                    searchKeyword = searchKeyword,
                    sortBy = sortBy
                ).cachedIn(viewModelScope)
                _articles.value = pagingFlow
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        isRefreshing = false,
                        isInitialLoad = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load articles",
                        isRefreshing = false,
                        isInitialLoad = false
                    )
                }
            }
        }
    }

    fun updateSortBy(sortBy: String) {
        _uiState.update { it.copy(sortBy = sortBy) }
        currentSourceId?.let {
            loadArticles(
                sourceId = it,
                forceRefresh = true,
                searchKeyword = currentSearchQuery,
                sortBy = sortBy
            )
        }
    }

    fun searchArticles(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            currentSourceId?.let {
                loadArticles(
                    sourceId = it,
                    forceRefresh = true,
                    searchKeyword = null,
                    sortBy = currentSortBy
                )
            }
            return
        }
        currentSourceId?.let {
            loadArticles(
                sourceId = it,
                forceRefresh = true,
                searchKeyword = trimmedQuery,
                sortBy = currentSortBy
            )
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
        savedStateHandle.remove<String>(SEARCH_QUERY_KEY)
        currentSearchQuery = null
        currentSourceId?.let {
            loadArticles(
                sourceId = it,
                forceRefresh = true,
                searchKeyword = null,
                sortBy = currentSortBy
            )
        }
    }

    fun clearSortFilter() {
        _uiState.update { it.copy(sortBy = "publishedAt") }
        savedStateHandle.remove<String>(SORT_BY_KEY)
        currentSourceId?.let {
            loadArticles(
                sourceId = it,
                forceRefresh = true,
                searchKeyword = currentSearchQuery,
                sortBy = "publishedAt"
            )
        }
    }

    fun refresh() {
        if (currentSearchQuery != null) {
            searchArticles(currentSearchQuery!!)
        } else {
            currentSourceId?.let {
                loadArticles(
                    sourceId = it,
                    forceRefresh = true,
                    searchKeyword = null,
                    sortBy = currentSortBy
                )
            }
        }
    }

    fun retry() {
        if (currentSearchQuery != null) {
            searchArticles(currentSearchQuery!!)
        } else {
            currentSourceId?.let {
                loadArticles(
                    sourceId = it,
                    forceRefresh = true,
                    searchKeyword = null,
                    sortBy = currentSortBy
                )
            }
        }
    }
}