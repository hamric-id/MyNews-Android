package com.hamric.feature.sources.data.cache

import com.hamric.core.model.Source
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceCache @Inject constructor(
    private val diskCache: SourceDiskCache
) {

    private val _memoryCache = MutableStateFlow<Map<String, List<Source>>>(emptyMap())
    private val _cacheInUse = MutableStateFlow<Set<String>>(emptySet())
    private val failureCounts = mutableMapOf<String, Int>()
    private val FAILURE_THRESHOLD = 3

    suspend fun get(categoryId: String): Pair<List<Source>?, Boolean> {
        _memoryCache.value[categoryId]?.let { sources ->
            return if (sources.isEmpty()) Pair(null, false) else Pair(sources, true)
        }

        diskCache.get(categoryId)?.let { sources ->
            if (sources.isNotEmpty()) {
                _memoryCache.value = _memoryCache.value + (categoryId to sources)
                return Pair(sources, true)
            }
            return Pair(null, false)
        }

        return Pair(null, false)
    }

    suspend fun put(categoryId: String, sources: List<Source>) {
        _memoryCache.value = _memoryCache.value + (categoryId to sources)
        diskCache.put(categoryId, sources)
        resetFailures(categoryId)
        markCacheNotInUse(categoryId)
    }

    suspend fun getFromDisk(categoryId: String): List<Source>? {
        return diskCache.get(categoryId)
    }

    fun getFromMemory(categoryId: String): List<Source>? {
        return _memoryCache.value[categoryId]
    }

    suspend fun clearCategory(categoryId: String) {
        _memoryCache.value = _memoryCache.value - categoryId
        diskCache.clearCategory(categoryId)
        resetFailures(categoryId)
        _cacheInUse.value = _cacheInUse.value - categoryId
    }

    suspend fun clearAll() {
        _memoryCache.value = emptyMap()
        diskCache.clearAll()
        failureCounts.clear()
        _cacheInUse.value = emptySet()
    }

    suspend fun isCached(categoryId: String): Boolean {
        val memoryCached = _memoryCache.value.containsKey(categoryId)
        val diskCached = diskCache.isCached(categoryId)
        return memoryCached || diskCached
    }

    fun recordFailure(categoryId: String) {
        val currentCount = failureCounts[categoryId] ?: 0
        failureCounts[categoryId] = currentCount + 1
    }

    fun shouldUseCache(categoryId: String): Boolean {
        val failureCount = failureCounts[categoryId] ?: 0
        return failureCount >= FAILURE_THRESHOLD && isCachedSync(categoryId)
    }

    private fun isCachedSync(categoryId: String): Boolean {
        return _memoryCache.value.containsKey(categoryId)
    }

    fun resetFailures(categoryId: String) {
        failureCounts.remove(categoryId)
    }

    fun getFailureCount(categoryId: String): Int {
        return failureCounts[categoryId] ?: 0
    }


    fun markCacheInUse(categoryId: String) {
        _cacheInUse.value = _cacheInUse.value + categoryId
    }

    fun markCacheNotInUse(categoryId: String) {
        _cacheInUse.value = _cacheInUse.value - categoryId
    }

    fun isCacheInUse(categoryId: String): Boolean {
        return _cacheInUse.value.contains(categoryId)
    }
}