package com.hamric.feature.sources.domain.usecase

import com.hamric.core.model.Source
import com.hamric.feature.sources.domain.repository.SourcesRepository
import javax.inject.Inject

class GetSourcesByCategoryUseCase @Inject constructor(
    private val repository: SourcesRepository
) {
    suspend operator fun invoke(categoryId: String): Result<List<Source>> {
        return repository.getSourcesByCategory(categoryId)
    }
}