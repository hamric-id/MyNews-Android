package com.hamric.feature.sources.data.repository

import com.hamric.core.network.api.NewsApi
import com.hamric.feature.sources.data.cache.SourceCache
import com.hamric.feature.sources.data.cache.SourceDiskCache
import com.hamric.feature.sources.utils.CoroutineTestRule
import com.hamric.feature.sources.utils.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class SourcesRepositoryImplTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var repository: SourcesRepositoryImpl
    private val mockApi: NewsApi = mock()
    private lateinit var cache: SourceCache
    private lateinit var diskCache: SourceDiskCache
    private val categoryId = "general"

    @Before
    fun setup() = runTest {
        diskCache = mock()
        whenever(diskCache.isCached(any())).thenReturn(false)
        whenever(diskCache.get(any())).thenReturn(null)
        whenever(diskCache.getCount(any())).thenReturn(0)
        whenever(diskCache.getCachedCategories()).thenReturn(emptyList())
        whenever(diskCache.search(any())).thenReturn(emptyList())

        cache = SourceCache(diskCache)
        repository = SourcesRepositoryImpl(mockApi, cache)
    }

    @Test
    fun `getSourcesByCategory should return sources when API call is successful`() = runTest {

        val mockResponse = TestDataFactory.createSourcesResponse()
        whenever(mockApi.getSourcesByCategory(category = categoryId)).thenReturn(mockResponse)


        val result = repository.getSourcesByCategory(categoryId)


        assertThat(result.isSuccess).isTrue()
        val sources = result.getOrNull()
        assertThat(sources).isNotNull()
        assertThat(sources).hasSize(5)
        assertThat(sources?.get(0)?.name).isEqualTo("BBC News")

        assertThat(cache.isCached(categoryId)).isTrue()
        val (cached, _) = cache.get(categoryId)
        assertThat(cached).isNotNull()
        assertThat(cached).hasSize(5)

        verify(mockApi).getSourcesByCategory(category = categoryId)
    }

    @Test
    fun `getSourcesByCategory should cache sources after successful API call`() = runTest {

        val mockResponse = TestDataFactory.createSourcesResponse()
        whenever(mockApi.getSourcesByCategory(category = categoryId)).thenReturn(mockResponse)


        repository.getSourcesByCategory(categoryId)
        advanceUntilIdle()


        assertThat(cache.isCached(categoryId)).isTrue()
        val (cached, _) = cache.get(categoryId)
        assertThat(cached).isNotNull()
        assertThat(cached).hasSize(5)
    }

    @Test
    fun `getSourcesByCategory should reset failure count on successful API call`() = runTest {

        cache.recordFailure(categoryId)
        cache.recordFailure(categoryId)
        assertThat(cache.getFailureCount(categoryId)).isEqualTo(2)

        val mockResponse = TestDataFactory.createSourcesResponse()
        whenever(mockApi.getSourcesByCategory(category = categoryId)).thenReturn(mockResponse)


        repository.getSourcesByCategory(categoryId)


        assertThat(cache.getFailureCount(categoryId)).isEqualTo(0)
    }


    @Test
    fun `getSourcesByCategory should record failure when API returns error`() = runTest {

        val errorResponse = TestDataFactory.createErrorResponse(message = "API error")
        whenever(mockApi.getSourcesByCategory(category = categoryId)).thenReturn(errorResponse)


        val result = repository.getSourcesByCategory(categoryId)


        assertThat(result.isFailure).isTrue()
        assertThat(cache.getFailureCount(categoryId)).isEqualTo(1)
        verify(mockApi).getSourcesByCategory(category = categoryId)
    }

    @Test
    fun `getSourcesByCategory should use cache after 3 failures`() = runTest {

        val cachedSources = TestDataFactory.createSourceList(3)
        cache.put(categoryId, cachedSources)

        val errorResponse = TestDataFactory.createErrorResponse(message = "API error")
        whenever(mockApi.getSourcesByCategory(category = categoryId)).thenReturn(errorResponse)

        repository.getSourcesByCategory(categoryId) 
        repository.getSourcesByCategory(categoryId) 
        val result = repository.getSourcesByCategory(categoryId)


        assertThat(result.isSuccess).isTrue()
        val sources = result.getOrNull()
        assertThat(sources).isEqualTo(cachedSources)
        assertThat(cache.getFailureCount(categoryId)).isEqualTo(3)
        assertThat(cache.isCacheInUse(categoryId)).isTrue()

        verify(mockApi, times(3)).getSourcesByCategory(category = categoryId)
    }

    @Test
    fun `getSourcesByCategory should return error when cache not available after 3 failures`() = runTest {

        val errorResponse = TestDataFactory.createErrorResponse(message = "API error")
        whenever(mockApi.getSourcesByCategory(category = categoryId)).thenReturn(errorResponse)

        repository.getSourcesByCategory(categoryId) 
        repository.getSourcesByCategory(categoryId) 
        val result = repository.getSourcesByCategory(categoryId) 


        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception?.message).contains("0 attempts remaining before using cached data")
    }

    @Test
    fun `getSourcesByCategory should handle RuntimeException and record failure`() = runTest {
        val exception = RuntimeException("Network error")
        whenever(mockApi.getSourcesByCategory(category = categoryId)).thenThrow(exception)


        val result = repository.getSourcesByCategory(categoryId)


        assertThat(result.isFailure).isTrue()
        assertThat(cache.getFailureCount(categoryId)).isEqualTo(1)
        verify(mockApi).getSourcesByCategory(category = categoryId)
    }

    @Test
    fun `getSourcesByCategory should show remaining attempts count in error message`() = runTest {

        val errorResponse = TestDataFactory.createErrorResponse(message = "API error")
        whenever(mockApi.getSourcesByCategory(category = categoryId)).thenReturn(errorResponse)
        
        val result = repository.getSourcesByCategory(categoryId)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception?.message).contains("2 attempts remaining")
    }

    @Test
    fun `getSourcesByCategory should show 0 attempts remaining on 3rd failure`() = runTest {

        val errorResponse = TestDataFactory.createErrorResponse(message = "API error")
        whenever(mockApi.getSourcesByCategory(category = categoryId)).thenReturn(errorResponse)

        repository.getSourcesByCategory(categoryId) 
        repository.getSourcesByCategory(categoryId) 
        val result = repository.getSourcesByCategory(categoryId) 


        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception?.message).contains("0 attempts remaining before using cached data")
    }

    @Test
    fun `refreshSources should clear cache and fetch fresh data`() = runTest {

        val cachedSources = TestDataFactory.createSourceList(3)
        cache.put(categoryId, cachedSources)
        assertThat(cache.isCached(categoryId)).isTrue()

        val mockResponse = TestDataFactory.createSourcesResponse()
        whenever(mockApi.getSourcesByCategory(category = categoryId)).thenReturn(mockResponse)


        val result = repository.refreshSources(categoryId)


        assertThat(result.isSuccess).isTrue()
        val sources = result.getOrNull()
        assertThat(sources).isNotNull()
        val (cached, _) = cache.get(categoryId)
        assertThat(cached).isNotNull()
        assertThat(cached).isNotEqualTo(cachedSources)
        assertThat(cache.isCacheInUse(categoryId)).isFalse()
        verify(mockApi).getSourcesByCategory(category = categoryId)
    }
}