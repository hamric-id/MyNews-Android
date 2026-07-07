package com.hamric.feature.categories.domain.repository

import com.hamric.core.model.Category

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
}