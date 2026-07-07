package com.hamric.feature.categories.domain.usecase

import com.hamric.core.model.Category
import com.hamric.feature.categories.domain.repository.CategoryRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(): Result<List<Category>> {
        return repository.getCategories()
    }
}