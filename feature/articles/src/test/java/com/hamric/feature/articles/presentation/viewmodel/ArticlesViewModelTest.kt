package com.hamric.feature.articles.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.hamric.feature.articles.domain.usecase.GetArticlesBySourceUseCase
import com.hamric.feature.articles.utils.CoroutineTestRule
import com.hamric.feature.articles.utils.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalCoroutinesApi::class)
class ArticlesViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var viewModel: ArticlesViewModel
    private val mockUseCase: GetArticlesBySourceUseCase = mock()
    private val savedStateHandle = SavedStateHandle()
    private val sourceId = "bbc-news"
    private val searchKeyword = "tesla"

    @Before
    fun setup() {
        viewModel = ArticlesViewModel(mockUseCase, savedStateHandle)
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        val initialState = viewModel.uiState.first()
        assertThat(initialState.isLoading).isTrue()
        assertThat(initialState.isInitialLoad).isTrue()
        assertThat(initialState.error).isNull()
        assertThat(initialState.articles).isEmpty()
        assertThat(initialState.searchQuery).isEmpty()
        assertThat(initialState.sortBy).isEqualTo("publishedAt")
    }

    @Test
    fun `loadArticles should update state to Success and set articles`() = runTest {
        val expectedArticles = TestDataFactory.createArticleList(3)
        val pagingData = PagingData.from(expectedArticles)
        whenever(
            mockUseCase.invoke(sourceId, null, "publishedAt")
        ).thenReturn(flowOf(pagingData))

        viewModel.loadArticles(sourceId)
        advanceUntilIdle()
        val finalState = viewModel.uiState.first { !it.isLoading }

        assertThat(finalState.isLoading).isFalse()
        assertThat(finalState.isInitialLoad).isFalse()
        assertThat(finalState.error).isNull()
        assertThat(viewModel.articles.value).isNotNull()
        verify(mockUseCase, times(1)).invoke(sourceId, null, "publishedAt")
    }

    @Test
    fun `loadArticles should handle search keyword`() = runTest {
        val expectedArticles = TestDataFactory.createArticleList(2)
        val pagingData = PagingData.from(expectedArticles)
        whenever(
            mockUseCase.invoke(sourceId, searchKeyword, "publishedAt")
        ).thenReturn(flowOf(pagingData))

        viewModel.loadArticles(sourceId, searchKeyword = searchKeyword)
        advanceUntilIdle()
        val finalState = viewModel.uiState.first { !it.isLoading }

        assertThat(finalState.searchQuery).isEqualTo(searchKeyword)
        assertThat(viewModel.articles.value).isNotNull()
        verify(mockUseCase, times(1)).invoke(sourceId, searchKeyword, "publishedAt")
    }

    @Test
    fun `loadArticles should handle sortBy change`() = runTest {
        val expectedArticles = TestDataFactory.createArticleList(2)
        val pagingData = PagingData.from(expectedArticles)
        whenever(
            mockUseCase.invoke(sourceId, null, "popularity")
        ).thenReturn(flowOf(pagingData))

        viewModel.loadArticles(sourceId, sortBy = "popularity")
        advanceUntilIdle()
        val finalState = viewModel.uiState.first { !it.isLoading }

        assertThat(finalState.sortBy).isEqualTo("popularity")
        assertThat(viewModel.articles.value).isNotNull()
        verify(mockUseCase, times(1)).invoke(sourceId, null, "popularity")
    }

    @Test
    fun `searchArticles should trigger load with search keyword`() = runTest {
        val expectedArticles = TestDataFactory.createArticleList(2)
        val pagingData = PagingData.from(expectedArticles)

        whenever(
            mockUseCase.invoke(sourceId, null, "publishedAt")
        ).thenReturn(flowOf(pagingData))
        whenever(
            mockUseCase.invoke(sourceId, searchKeyword, "publishedAt")
        ).thenReturn(flowOf(pagingData))

        viewModel.loadArticles(sourceId)
        advanceUntilIdle()
        viewModel.searchArticles(searchKeyword)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.searchQuery).isEqualTo(searchKeyword)

        verify(mockUseCase, times(1)).invoke(sourceId, null, "publishedAt")
        verify(mockUseCase, times(1)).invoke(sourceId, searchKeyword, "publishedAt")
    }

    @Test
    fun `clearSearch should reset search query and reload`() = runTest {
        val expectedArticles = TestDataFactory.createArticleList(3)
        val pagingData = PagingData.from(expectedArticles)
        whenever(
            mockUseCase.invoke(sourceId, null, "publishedAt")
        ).thenReturn(flowOf(pagingData))

        viewModel.loadArticles(sourceId)
        advanceUntilIdle()
        viewModel.searchArticles(searchKeyword)
        advanceUntilIdle()
        viewModel.clearSearch()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.searchQuery).isEmpty()

        verify(mockUseCase, times(2)).invoke(sourceId, null, "publishedAt")
    }

    @Test
    fun `refresh should reload with current filters`() = runTest {
        val expectedArticles = TestDataFactory.createArticleList(2)
        val pagingData = PagingData.from(expectedArticles)
        whenever(
            mockUseCase.invoke(sourceId, null, "publishedAt")
        ).thenReturn(flowOf(pagingData))

        viewModel.loadArticles(sourceId)
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        verify(mockUseCase, times(2)).invoke(sourceId, null, "publishedAt")
    }

    @Test
    fun `retry should reload with current filters`() = runTest {
        val expectedArticles = TestDataFactory.createArticleList(2)
        val pagingData = PagingData.from(expectedArticles)
        whenever(
            mockUseCase.invoke(sourceId, null, "publishedAt")
        ).thenReturn(flowOf(pagingData))

        viewModel.loadArticles(sourceId)
        advanceUntilIdle()
        viewModel.retry()
        advanceUntilIdle()

        verify(mockUseCase, times(2)).invoke(sourceId, null, "publishedAt")
    }


    @Test
    fun `loadArticles should update state to Error when use case fails`() = runTest {
        val errorMessage = "Failed to load articles"
        val exception = RuntimeException(errorMessage)
        whenever(
            mockUseCase.invoke(sourceId, null, "publishedAt")
        ).thenThrow(exception)

        viewModel.loadArticles(sourceId)
        advanceUntilIdle()
        val finalState = viewModel.uiState.first { !it.isLoading }

        assertThat(finalState.isLoading).isFalse()
        assertThat(finalState.error).contains(errorMessage)
        assertThat(viewModel.articles.value).isNull()
        verify(mockUseCase, times(1)).invoke(sourceId, null, "publishedAt")
    }

    @Test
    fun `loadArticles should not reload if same parameters`() = runTest {
        val expectedArticles = TestDataFactory.createArticleList(2)
        val pagingData = PagingData.from(expectedArticles)
        whenever(
            mockUseCase.invoke(sourceId, null, "publishedAt")
        ).thenReturn(flowOf(pagingData))

        viewModel.loadArticles(sourceId)
        advanceUntilIdle()
        viewModel.loadArticles(sourceId)
        advanceUntilIdle()

        verify(mockUseCase, times(1)).invoke(sourceId, null, "publishedAt")
    }

    @Test
    fun `searchArticles with empty query should load source articles`() = runTest {
        val expectedArticles = TestDataFactory.createArticleList(2)
        val pagingData = PagingData.from(expectedArticles)
        whenever(
            mockUseCase.invoke(sourceId, null, "publishedAt")
        ).thenReturn(flowOf(pagingData))

        viewModel.loadArticles(sourceId)
        advanceUntilIdle()
        viewModel.searchArticles("")
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.searchQuery).isEmpty()
        verify(mockUseCase, times(2)).invoke(sourceId, null, "publishedAt")
    }

    @Test
    fun `updateSortBy should change sort and reload`() = runTest {
        val expectedArticles = TestDataFactory.createArticleList(2)
        val pagingData = PagingData.from(expectedArticles)

        whenever(
            mockUseCase.invoke(sourceId, null, "publishedAt")
        ).thenReturn(flowOf(pagingData))
        whenever(
            mockUseCase.invoke(sourceId, null, "popularity")
        ).thenReturn(flowOf(pagingData))

        viewModel.loadArticles(sourceId)
        advanceUntilIdle()
        viewModel.updateSortBy("popularity")
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.sortBy).isEqualTo("popularity")

        verify(mockUseCase, times(1)).invoke(sourceId, null, "publishedAt")
        verify(mockUseCase, times(1)).invoke(sourceId, null, "popularity")
    }

    @Test
    fun `clearSortFilter should reset sort to publishedAt`() = runTest {
        val expectedArticles = TestDataFactory.createArticleList(2)
        val pagingData = PagingData.from(expectedArticles)

        whenever(
            mockUseCase.invoke(sourceId, null, "publishedAt")
        ).thenReturn(flowOf(pagingData))
        whenever(
            mockUseCase.invoke(sourceId, null, "popularity")
        ).thenReturn(flowOf(pagingData))

        viewModel.loadArticles(sourceId)
        advanceUntilIdle()
        viewModel.updateSortBy("popularity")
        advanceUntilIdle()
        viewModel.clearSortFilter()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.sortBy).isEqualTo("publishedAt")

        verify(mockUseCase, times(2)).invoke(sourceId, null, "publishedAt")
        verify(mockUseCase, times(1)).invoke(sourceId, null, "popularity")
    }
}