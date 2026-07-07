package com.hamric.feature.categories.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamric.feature.categories.domain.usecase.GetCategoriesUseCase
import com.hamric.feature.categories.presentation.state.CategoriesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val UI_STATE_KEY = "ui_state"

    private val _uiState = MutableStateFlow(
        savedStateHandle.get<CategoriesUiState>(UI_STATE_KEY)
            ?: CategoriesUiState(isLoading = true)
    )
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()


    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = getCategoriesUseCase()
            result.fold(
                onSuccess = { categories ->
                    val newState = if (categories.isEmpty()) {
                        CategoriesUiState(
                            isLoading = false,
                            categories = emptyList(),
                            error = null
                        )
                    } else {
                        CategoriesUiState(
                            isLoading = false,
                            categories = categories,
                            error = null
                        )
                    }
                    _uiState.value = newState
                    savedStateHandle.set(UI_STATE_KEY, newState)
                },
                onFailure = { exception ->
                    val newState = CategoriesUiState(
                        isLoading = false,
                        categories = emptyList(),
                        error = exception.message ?: "Failed to load categories"
                    )
                    _uiState.value = newState
                    savedStateHandle.set(UI_STATE_KEY, newState)
                }
            )
        }
    }

    fun retry() {
        savedStateHandle.remove<CategoriesUiState>(UI_STATE_KEY)
        loadCategories()
    }
}