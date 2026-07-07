package com.hamric.feature.sources.data.cache

import com.hamric.core.database.dao.SourceDao
import com.hamric.core.database.entity.toDomain
import com.hamric.core.database.entity.toEntity
import com.hamric.core.model.Source
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceDiskCache @Inject constructor(
    private val sourceDao: SourceDao
) {

    suspend fun get(categoryId: String): List<Source>? {
        return sourceDao.getSourcesByCategory(categoryId)?.toDomain()
    }

    suspend fun put(categoryId: String, sources: List<Source>) {
        sourceDao.clearCategory(categoryId)
        if (sources.isNotEmpty()) {
            sourceDao.insertSources(sources.map { it.toEntity(categoryId) })
        }
    }

    suspend fun clearCategory(categoryId: String) {
        sourceDao.clearCategory(categoryId)
    }

    suspend fun clearAll() {
        sourceDao.clearAll()
    }

    suspend fun getCount(categoryId: String): Int {
        return sourceDao.getCountByCategory(categoryId)
    }

    suspend fun search(query: String): List<Source> {
        return sourceDao.searchSources(query).toDomain()
    }

    suspend fun getCachedCategories(): List<String> {
        return sourceDao.getCachedCategories()
    }

    suspend fun isCached(categoryId: String): Boolean {
        return sourceDao.getCountByCategory(categoryId) > 0
    }
}