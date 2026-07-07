package com.hamric.feature.sources.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamric.core.model.Source
import com.hamric.feature.sources.data.cache.SourceCache
import com.hamric.feature.sources.domain.usecase.GetSourcesByCategoryUseCase
import com.hamric.feature.sources.presentation.state.SourcesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SourcesViewModel @Inject constructor(
    private val getSourcesByCategoryUseCase: GetSourcesByCategoryUseCase,
    private val cache: SourceCache,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val SEARCH_QUERY_KEY = "search_query"
    private val CATEGORY_ID_KEY = "category_id"

    private val _uiState = MutableStateFlow(SourcesUiState(isLoading = true))
    val uiState: StateFlow<SourcesUiState> = _uiState.asStateFlow()

    private var currentCategoryId: String? = null

    init {
        savedStateHandle.get<String>(SEARCH_QUERY_KEY)?.let { query ->
            _uiState.update { it.copy(searchQuery = query) }
        }
        savedStateHandle.get<String>(CATEGORY_ID_KEY)?.let { categoryId ->
            currentCategoryId = categoryId
            loadSources(categoryId)
        }
    }

    fun loadSources(categoryId: String, forceRefresh: Boolean = false) {
        if (!forceRefresh && currentCategoryId == categoryId) return

        viewModelScope.launch {
            currentCategoryId = categoryId
            savedStateHandle.set(CATEGORY_ID_KEY, categoryId)

            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    isRefreshing = forceRefresh,
                    isUsingCache = false,
                    isFromDiskCache = false
                )
            }

            val result = getSourcesByCategoryUseCase(categoryId)
            result.fold(
                onSuccess = { sources ->
                    val isUsingCache = cache.isCacheInUse(categoryId)
                    val isFromDiskCache = isUsingCache

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sources = sources,
                            filteredSources = filterSources(sources, it.searchQuery),
                            error = null,
                            isRefreshing = false,
                            isUsingCache = isUsingCache,
                            isFromDiskCache = isFromDiskCache,
                            failureCount = 0
                        )
                    }
                    savedStateHandle.set(SEARCH_QUERY_KEY, _uiState.value.searchQuery)
                },
                onFailure = { exception ->
                    val isUsingCache = exception.message?.contains("using cached data") == true
                    val isFromDiskCache = isUsingCache

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load sources",
                            isRefreshing = false,
                            isUsingCache = isUsingCache,
                            isFromDiskCache = isFromDiskCache,
                            failureCount = getFailureCount(categoryId)
                        )
                    }
                }
            )
        }
    }

    private fun getFailureCount(categoryId: String): Int {
        return cache.getFailureCount(categoryId)
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            val filtered = filterSources(currentState.sources, query)
            currentState.copy(
                searchQuery = query,
                filteredSources = filtered
            )
        }
        savedStateHandle.set(SEARCH_QUERY_KEY, query)
    }

    fun clearSearch() {
        updateSearchQuery("")
    }

    fun refresh() {
        currentCategoryId?.let { categoryId ->
            loadSources(categoryId, forceRefresh = true)
        }
    }

    fun retry() {
        currentCategoryId?.let { categoryId ->
            loadSources(categoryId, forceRefresh = true)
        }
    }

    private fun filterSources(sources: List<Source>, query: String): List<Source> {
        if (query.isBlank()) return sources

        val lowerQuery = query.lowercase()
        return sources.filter { source ->
            source.name.lowercase().contains(lowerQuery) ||
                    source.description?.lowercase()?.contains(lowerQuery) == true ||
                    source.category?.lowercase()?.contains(lowerQuery) == true ||
                    source.country?.lowercase()?.contains(lowerQuery) == true ||
                    source.language?.lowercase()?.contains(lowerQuery) == true
        }
    }
}