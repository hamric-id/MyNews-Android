package com.hamric.feature.sources.data.repository

import com.hamric.core.model.Source
import com.hamric.core.network.api.NewsApi
import com.hamric.core.network.mapper.toDomainModels
import com.hamric.feature.sources.data.cache.SourceCache
import com.hamric.feature.sources.domain.repository.SourcesRepository
import javax.inject.Inject

class SourcesRepositoryImpl @Inject constructor(
    private val api: NewsApi,
    private val cache: SourceCache
) : SourcesRepository {

    override suspend fun getSourcesByCategory(categoryId: String): Result<List<Source>> {
        return try {
            val response = api.getSourcesByCategory(category = categoryId)

            if (response.status == "ok") {
                val sources = response.sources.toDomainModels()
                cache.put(categoryId, sources)
                Result.success(sources)
            } else {
                cache.recordFailure(categoryId)
                handleApiFailure(categoryId)
            }
        } catch (e: Exception) {
            cache.recordFailure(categoryId)
            handleApiFailure(categoryId)
        }
    }

    private suspend fun handleApiFailure(categoryId: String): Result<List<Source>> {
        return if (cache.shouldUseCache(categoryId)) {
            val (cachedSources, _) = cache.get(categoryId)
            if (cachedSources != null) {
                cache.markCacheInUse(categoryId)
                Result.success(cachedSources)
            } else {
                Result.failure(Exception("No data available. Please check your connection."))
            }
        } else {
            val remainingAttempts = 3 - cache.getFailureCount(categoryId)
            Result.failure(
                Exception(
                    "Failed to load sources. $remainingAttempts attempts remaining before using cached data."
                )
            )
        }
    }

    suspend fun refreshSources(categoryId: String): Result<List<Source>> {
        cache.clearCategory(categoryId)
        return getSourcesByCategory(categoryId)
    }
}