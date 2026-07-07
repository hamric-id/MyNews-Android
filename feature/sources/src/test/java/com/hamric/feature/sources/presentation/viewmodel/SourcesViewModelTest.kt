package com.hamric.feature.sources.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.hamric.feature.sources.data.cache.SourceCache
import com.hamric.feature.sources.domain.usecase.GetSourcesByCategoryUseCase
import com.hamric.feature.sources.utils.CoroutineTestRule
import com.hamric.feature.sources.utils.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class SourcesViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var viewModel: SourcesViewModel
    private val mockUseCase: GetSourcesByCategoryUseCase = mock()
    private val mockCache: SourceCache = mock()
    private val savedStateHandle = SavedStateHandle()
    private val categoryId = "general"

    @Before
    fun setup() {
        viewModel = SourcesViewModel(mockUseCase, mockCache, savedStateHandle)
    }


    @Test
    fun `initial state should be Loading`() = runTest {

        val initialState = viewModel.uiState.first()


        assertThat(initialState.isLoading).isTrue()
        assertThat(initialState.error).isNull()
        assertThat(initialState.sources).isEmpty()
        assertThat(initialState.isUsingCache).isFalse()
        assertThat(initialState.isFromDiskCache).isFalse()
    }

    @Test
    fun `loadSources should update state to Success with sources when use case succeeds`() = runTest {

        val expectedSources = TestDataFactory.createSourceList(3)
        whenever(mockUseCase(categoryId)).thenReturn(Result.success(expectedSources))
        whenever(mockCache.isCacheInUse(categoryId)).thenReturn(false)


        viewModel.loadSources(categoryId)
        advanceUntilIdle()

        val finalState = viewModel.uiState.first { !it.isLoading }


        assertThat(finalState.isLoading).isFalse()
        assertThat(finalState.error).isNull()
        assertThat(finalState.sources).isEqualTo(expectedSources)
        assertThat(finalState.isUsingCache).isFalse()
        assertThat(finalState.isFromDiskCache).isFalse()
        verify(mockUseCase).invoke(categoryId)
    }

    @Test
    fun `loadSources should update search query after loading`() = runTest {

        val expectedSources = TestDataFactory.createSourceList(3)
        whenever(mockUseCase(categoryId)).thenReturn(Result.success(expectedSources))
        whenever(mockCache.isCacheInUse(categoryId)).thenReturn(false)


        viewModel.loadSources(categoryId)
        advanceUntilIdle()
        viewModel.updateSearchQuery("BBC")


        val state = viewModel.uiState.first { !it.isLoading }
        assertThat(state.searchQuery).isEqualTo("BBC")
        assertThat(state.filteredSources).hasSize(1)
    }

    @Test
    fun `loadSources should not reload same category`() = runTest {

        val sources = TestDataFactory.createSourceList(3)
        whenever(mockUseCase(categoryId)).thenReturn(Result.success(sources))
        whenever(mockCache.isCacheInUse(categoryId)).thenReturn(false)

        viewModel.loadSources(categoryId)
        advanceUntilIdle()
        viewModel.uiState.first { !it.isLoading }

        viewModel.loadSources(categoryId)
        advanceUntilIdle()

        verify(mockUseCase, times(1)).invoke(categoryId)
    }

    @Test
    fun `updateSearchQuery should filter sources by name`() = runTest {

        val sources = TestDataFactory.createSourceList(4)
        whenever(mockUseCase(categoryId)).thenReturn(Result.success(sources))
        whenever(mockCache.isCacheInUse(categoryId)).thenReturn(false)

        viewModel.loadSources(categoryId)
        advanceUntilIdle()
        viewModel.uiState.first { !it.isLoading }


        viewModel.updateSearchQuery("BBC")


        val state = viewModel.uiState.value
        assertThat(state.searchQuery).isEqualTo("BBC")
        assertThat(state.filteredSources).hasSize(1)
        assertThat(state.filteredSources[0].name).contains("BBC")
    }

    @Test
    fun `updateSearchQuery should filter sources by description`() = runTest {

        val sources = listOf(
            TestDataFactory.createSource(name = "Source 1", description = "Tech news"),
            TestDataFactory.createSource(name = "Source 2", description = "Sports news"),
            TestDataFactory.createSource(name = "Source 3", description = "Business news")
        )
        whenever(mockUseCase(categoryId)).thenReturn(Result.success(sources))
        whenever(mockCache.isCacheInUse(categoryId)).thenReturn(false)

        viewModel.loadSources(categoryId)
        advanceUntilIdle()
        viewModel.uiState.first { !it.isLoading }


        viewModel.updateSearchQuery("tech")


        val state = viewModel.uiState.value
        assertThat(state.filteredSources).hasSize(1)
        assertThat(state.filteredSources[0].description).contains("Tech")
    }

    @Test
    fun `clearSearch should reset search query and show all sources`() = runTest {

        val sources = TestDataFactory.createSourceList(4)
        whenever(mockUseCase(categoryId)).thenReturn(Result.success(sources))
        whenever(mockCache.isCacheInUse(categoryId)).thenReturn(false)

        viewModel.loadSources(categoryId)
        advanceUntilIdle()
        viewModel.uiState.first { !it.isLoading }

        viewModel.updateSearchQuery("BBC")
        assertThat(viewModel.uiState.value.searchQuery).isEqualTo("BBC")
        assertThat(viewModel.uiState.value.filteredSources).hasSize(1)


        viewModel.clearSearch()


        val state = viewModel.uiState.value
        assertThat(state.searchQuery).isEmpty()
        assertThat(state.filteredSources).hasSize(4)
    }

    @Test
    fun `refresh should force reload sources`() = runTest {

        val sources = TestDataFactory.createSourceList(3)
        whenever(mockUseCase(categoryId)).thenReturn(Result.success(sources))
        whenever(mockCache.isCacheInUse(categoryId)).thenReturn(false)

        viewModel.loadSources(categoryId)
        advanceUntilIdle()
        viewModel.uiState.first { !it.isLoading }

        viewModel.refresh()
        advanceUntilIdle()

        verify(mockUseCase, times(2)).invoke(categoryId)
    }

    @Test
    fun `retry should reload sources`() = runTest {

        val sources = TestDataFactory.createSourceList(3)
        whenever(mockUseCase(categoryId)).thenReturn(Result.success(sources))
        whenever(mockCache.isCacheInUse(categoryId)).thenReturn(false)

        viewModel.loadSources(categoryId)
        advanceUntilIdle()
        viewModel.uiState.first { !it.isLoading }

        viewModel.retry()
        advanceUntilIdle()

        verify(mockUseCase, times(2)).invoke(categoryId)
    }

    @Test
    fun `loadSources should update state to Error when use case fails`() = runTest {

        val errorMessage = "Failed to load sources"
        val exception = Exception(errorMessage)
        whenever(mockUseCase(categoryId)).thenReturn(Result.failure(exception))
        whenever(mockCache.isCacheInUse(categoryId)).thenReturn(false)


        viewModel.loadSources(categoryId)
        advanceUntilIdle()
        val finalState = viewModel.uiState.first { !it.isLoading }


        assertThat(finalState.isLoading).isFalse()
        assertThat(finalState.error).contains(errorMessage)
        assertThat(finalState.sources).isEmpty()
        assertThat(finalState.isUsingCache).isFalse()
        verify(mockUseCase).invoke(categoryId)
    }

    @Test
    fun `loadSources should show cache status when using cache after failures`() = runTest {

        val cachedSources = TestDataFactory.createSourceList(3)
        whenever(mockUseCase(categoryId)).thenReturn(Result.success(cachedSources))
        whenever(mockCache.isCacheInUse(categoryId)).thenReturn(true)


        viewModel.loadSources(categoryId)
        advanceUntilIdle()
        val finalState = viewModel.uiState.first { !it.isLoading }


        assertThat(finalState.isLoading).isFalse()
        assertThat(finalState.isUsingCache).isTrue()
        assertThat(finalState.isFromDiskCache).isTrue()
        assertThat(finalState.sources).isEqualTo(cachedSources)
    }

    @Test
    fun `loadSources should not save error state to SavedStateHandle`() = runTest {

        val errorMessage = "Network error"
        val exception = Exception(errorMessage)
        whenever(mockUseCase(categoryId)).thenReturn(Result.failure(exception))


        viewModel.loadSources(categoryId)
        advanceUntilIdle()
        viewModel.uiState.first { !it.isLoading }


        val savedSearch = savedStateHandle.get<String>("search_query")
        assertThat(savedSearch).isNull()
    }

    @Test
    fun `search query should be restored from SavedStateHandle on ViewModel creation`() = runTest {

        savedStateHandle.set("search_query", "BBC")

        val newViewModel = SourcesViewModel(mockUseCase, mockCache, savedStateHandle)


        val state = newViewModel.uiState.first()
        assertThat(state.searchQuery).isEqualTo("BBC")
    }
}