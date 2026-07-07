package com.hamric.feature.sources.domain.repository

import com.hamric.core.model.Source

interface SourcesRepository {
    suspend fun getSourcesByCategory(categoryId: String): Result<List<Source>>
}