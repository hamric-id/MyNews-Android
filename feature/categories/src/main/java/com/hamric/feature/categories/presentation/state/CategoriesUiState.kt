package com.hamric.feature.categories.presentation.state


import android.os.Parcelable
import com.hamric.core.model.Category
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoriesUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false
) : Parcelable

sealed class CategoriesEvent {
    object Retry : CategoriesEvent()
    data class NavigateToSources(val category: Category) : CategoriesEvent()
}