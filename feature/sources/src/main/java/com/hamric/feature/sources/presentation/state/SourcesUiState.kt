package com.hamric.feature.sources.presentation.state

import android.os.Parcelable
import com.hamric.core.model.Source
import kotlinx.parcelize.Parcelize

@Parcelize
data class SourcesUiState(
    val isLoading: Boolean = false,
    val sources: List<Source> = emptyList(),
    val filteredSources: List<Source> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val isUsingCache: Boolean = false,
    val isFromDiskCache: Boolean = false,
    val failureCount: Int = 0
) : Parcelable