package com.hamric.feature.sources.data.cache

import com.hamric.core.database.dao.SourceDao
import com.hamric.feature.sources.utils.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class SourceCacheTest {

    private lateinit var cache: SourceCache
    private lateinit var diskCache: SourceDiskCache
    private val categoryId = "general"
    private val sources = TestDataFactory.createSourceList(3)

    @Before
    fun setup() {
        diskCache = mock()
        cache = SourceCache(diskCache)
    }

    @Test
    fun `put should store sources in cache`() = runTest {

        whenever(diskCache.isCached(any())).thenReturn(false)


        cache.put(categoryId, sources)


        val (cached, fromCache) = cache.get(categoryId)
        assertThat(cached).isNotNull()
        assertThat(cached).hasSize(3)
        assertThat(cached).isEqualTo(sources)
        assertThat(fromCache).isTrue()
    }

    @Test
    fun `get should return cached sources when available`() = runTest {

        whenever(diskCache.isCached(any())).thenReturn(false)
        cache.put(categoryId, sources)


        val (result, fromCache) = cache.get(categoryId)


        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(sources)
        assertThat(fromCache).isTrue()
    }

    @Test
    fun `get should return sources from disk cache when not in memory`() = runTest {

        whenever(diskCache.get(categoryId)).thenReturn(sources)
        whenever(diskCache.isCached(any())).thenReturn(true)


        val (result, fromCache) = cache.get(categoryId)


        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(sources)
        assertThat(fromCache).isTrue()
    }

    @Test
    fun `isCached should return true when category is cached`() = runTest {

        whenever(diskCache.isCached(any())).thenReturn(true)
        cache.put(categoryId, sources)


        val result = cache.isCached(categoryId)


        assertThat(result).isTrue()
    }

    @Test
    fun `isCached should return false when category is not cached`() = runTest {

        whenever(diskCache.isCached(any())).thenReturn(false)


        val result = cache.isCached("unknown")


        assertThat(result).isFalse()
    }

    @Test
    fun `shouldUseCache should return true after 3 failures with cached data`() = runTest {

        whenever(diskCache.isCached(any())).thenReturn(true)
        cache.put(categoryId, sources)


        cache.recordFailure(categoryId)
        cache.recordFailure(categoryId)
        cache.recordFailure(categoryId)
        val result = cache.shouldUseCache(categoryId)


        assertThat(result).isTrue()
    }

    @Test
    fun `shouldUseCache should return false before 3 failures`() = runTest {

        whenever(diskCache.isCached(any())).thenReturn(true)
        cache.put(categoryId, sources)


        cache.recordFailure(categoryId)
        cache.recordFailure(categoryId)
        val result = cache.shouldUseCache(categoryId)


        assertThat(result).isFalse()
    }

    @Test
    fun `shouldUseCache should return false when no cached data`() = runTest {

        whenever(diskCache.isCached(any())).thenReturn(false)


        cache.recordFailure(categoryId)
        cache.recordFailure(categoryId)
        cache.recordFailure(categoryId)
        val result = cache.shouldUseCache(categoryId)


        assertThat(result).isFalse()
    }

    @Test
    fun `resetFailures should clear failure count`() = runTest {

        whenever(diskCache.isCached(any())).thenReturn(false)
        cache.recordFailure(categoryId)
        cache.recordFailure(categoryId)
        assertThat(cache.getFailureCount(categoryId)).isEqualTo(2)


        cache.resetFailures(categoryId)


        assertThat(cache.getFailureCount(categoryId)).isEqualTo(0)
        assertThat(cache.shouldUseCache(categoryId)).isFalse()
    }

    @Test
    fun `put should reset failure count on successful fetch`() = runTest {

        whenever(diskCache.isCached(any())).thenReturn(false)
        cache.recordFailure(categoryId)
        cache.recordFailure(categoryId)
        assertThat(cache.getFailureCount(categoryId)).isEqualTo(2)


        cache.put(categoryId, sources)


        assertThat(cache.getFailureCount(categoryId)).isEqualTo(0)
    }

    @Test
    fun `clearAll should remove all cached data`() = runTest {

        val dao: SourceDao = mock()
        whenever(dao.getSourcesByCategory(any())).thenReturn(emptyList())
        whenever(dao.getCountByCategory(any())).thenReturn(0)
        whenever(dao.getCachedCategories()).thenReturn(emptyList())
        whenever(dao.searchSources(any())).thenReturn(emptyList())

        val realDiskCache = SourceDiskCache(dao)
        cache = SourceCache(realDiskCache)

        cache.put(categoryId, sources)
        cache.put("technology", sources)
        assertThat(cache.isCached(categoryId)).isTrue()
        assertThat(cache.isCached("technology")).isTrue()


        cache.clearAll()


        val (result1, _) = cache.get(categoryId)
        val (result2, _) = cache.get("technology")
        assertThat(result1).isNull()
        assertThat(result2).isNull()
        assertThat(cache.isCached(categoryId)).isFalse()
        assertThat(cache.isCached("technology")).isFalse()

        verify(dao, times(1)).clearAll()
    }

    @Test
    fun `clearCategory should remove specific category from cache`() = runTest {

        val dao: SourceDao = mock()
        whenever(dao.getSourcesByCategory(any())).thenReturn(emptyList())
        whenever(dao.getCountByCategory(any())).thenReturn(0)
        whenever(dao.getCachedCategories()).thenReturn(emptyList())
        whenever(dao.searchSources(any())).thenReturn(emptyList())

        val realDiskCache = SourceDiskCache(dao)
        cache = SourceCache(realDiskCache)

        cache.put(categoryId, sources)
        cache.put("technology", sources)
        assertThat(cache.isCached(categoryId)).isTrue()
        assertThat(cache.isCached("technology")).isTrue()


        cache.clearCategory(categoryId)


        val (result1, _) = cache.get(categoryId)
        val (result2, _) = cache.get("technology")
        assertThat(result1).isNull()
        assertThat(result2).isNotNull()
        assertThat(cache.isCached(categoryId)).isFalse()
        assertThat(cache.isCached("technology")).isTrue()

    }

    @Test
    fun `getFailureCount should return correct failure count`() = runTest {

        cache.recordFailure(categoryId)
        cache.recordFailure(categoryId)


        val result = cache.getFailureCount(categoryId)


        assertThat(result).isEqualTo(2)
    }

    @Test
    fun `getFailureCount should return 0 for unknown category`() = runTest {

        val result = cache.getFailureCount("unknown")


        assertThat(result).isEqualTo(0)
    }

    @Test
    fun `markCacheInUse should track cache usage`() = runTest {

        assertThat(cache.isCacheInUse(categoryId)).isFalse()


        cache.markCacheInUse(categoryId)


        assertThat(cache.isCacheInUse(categoryId)).isTrue()
    }

    @Test
    fun `markCacheNotInUse should clear cache usage tracking`() = runTest {

        cache.markCacheInUse(categoryId)
        assertThat(cache.isCacheInUse(categoryId)).isTrue()


        cache.markCacheNotInUse(categoryId)


        assertThat(cache.isCacheInUse(categoryId)).isFalse()
    }

    @Test
    fun `getFromDisk should return sources from disk cache`() = runTest {

        whenever(diskCache.get(categoryId)).thenReturn(sources)
        whenever(diskCache.isCached(any())).thenReturn(true)


        val result = cache.getFromDisk(categoryId)


        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(sources)
    }

    @Test
    fun `getFromMemory should return sources from memory cache`() = runTest {

        whenever(diskCache.isCached(any())).thenReturn(false)
        cache.put(categoryId, sources)


        val result = cache.getFromMemory(categoryId)


        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(sources)
    }
}