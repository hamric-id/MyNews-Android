package com.hamric.feature.categories.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.hamric.feature.categories.domain.usecase.GetCategoriesUseCase
import com.hamric.feature.categories.presentation.state.CategoriesUiState
import com.hamric.feature.categories.utils.CoroutineTestRule
import com.hamric.feature.categories.utils.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var viewModel: CategoriesViewModel
    private val mockUseCase: GetCategoriesUseCase = mock()
    private val savedStateHandle = SavedStateHandle()

    @Before
    fun setup() {
        viewModel = CategoriesViewModel(mockUseCase, savedStateHandle)
    }


    @Test
    fun `initial state should be Loading`() = runTest {

        val initialState = viewModel.uiState.first()


        assertThat(initialState.isLoading).isTrue()
        assertThat(initialState.error).isNull()
        assertThat(initialState.categories).isEmpty()
    }

    @Test
    fun `loadCategories should update state to Success with categories when use case succeeds`() = runTest {

        val expectedCategories = TestDataFactory.createCategoryList(3)
        whenever(mockUseCase()).thenReturn(Result.success(expectedCategories))


        viewModel.loadCategories()
        advanceUntilIdle()


        val finalState = viewModel.uiState.first { !it.isLoading }


        assertThat(finalState.isLoading).isFalse()
        assertThat(finalState.error).isNull()
        assertThat(finalState.categories).isEqualTo(expectedCategories)

        verify(mockUseCase).invoke()
    }

    @Test
    fun `loadCategories should save state to SavedStateHandle on success`() = runTest {

        val expectedCategories = TestDataFactory.createCategoryList(3)
        whenever(mockUseCase()).thenReturn(Result.success(expectedCategories))


        viewModel.loadCategories()
        advanceUntilIdle()
        viewModel.uiState.first { !it.isLoading }


        val savedState = savedStateHandle.get<CategoriesUiState>("ui_state")
        assertThat(savedState).isNotNull()
        assertThat(savedState?.categories).isEqualTo(expectedCategories)
        assertThat(savedState?.isLoading).isFalse()
        assertThat(savedState?.error).isNull()
    }

    @Test
    fun `loadCategories should handle empty list and show empty state`() = runTest {

        whenever(mockUseCase()).thenReturn(Result.success(emptyList()))


        viewModel.loadCategories()
        advanceUntilIdle()
        val finalState = viewModel.uiState.first { !it.isLoading }


        assertThat(finalState.isLoading).isFalse()
        assertThat(finalState.error).isNull()
        assertThat(finalState.categories).isEmpty()
    }

    @Test
    fun `retry should clear error and reload categories`() = runTest {

        val errorMessage = "Network error"
        val exception = Exception(errorMessage)
        val expectedCategories = TestDataFactory.createCategoryList(2)

        whenever(mockUseCase())
            .thenReturn(Result.failure(exception))
            .thenReturn(Result.success(expectedCategories))


        viewModel.loadCategories()
        advanceUntilIdle()
        val errorState = viewModel.uiState.first { it.error != null }
        assertThat(errorState.error).contains(errorMessage)

        viewModel.retry()
        advanceUntilIdle()
        val successState = viewModel.uiState.first { !it.isLoading && it.error == null }


        assertThat(successState.error).isNull()
        assertThat(successState.categories).isEqualTo(expectedCategories)
    }

    @Test
    fun `loadCategories should update state to Error when use case fails`() = runTest {

        val errorMessage = "Failed to load categories"
        val exception = Exception(errorMessage)
        whenever(mockUseCase()).thenReturn(Result.failure(exception))


        viewModel.loadCategories()
        advanceUntilIdle()
        val finalState = viewModel.uiState.first { !it.isLoading }


        assertThat(finalState.isLoading).isFalse()
        assertThat(finalState.error).contains(errorMessage)
        assertThat(finalState.categories).isEmpty()

        verify(mockUseCase).invoke()
    }

    @Test
    fun `loadCategories should save error state to SavedStateHandle on failure`() = runTest {

        val errorMessage = "Network error"
        val exception = Exception(errorMessage)
        whenever(mockUseCase()).thenReturn(Result.failure(exception))


        viewModel.loadCategories()
        advanceUntilIdle()
        viewModel.uiState.first { !it.isLoading }


        val savedState = savedStateHandle.get<CategoriesUiState>("ui_state")
        assertThat(savedState).isNotNull()
        assertThat(savedState?.error).contains(errorMessage)
        assertThat(savedState?.isLoading).isFalse()
    }

    @Test
    fun `retry should remove error from SavedStateHandle`() = runTest {

        val errorMessage = "Network error"
        val exception = Exception(errorMessage)
        val expectedCategories = TestDataFactory.createCategoryList(2)

        whenever(mockUseCase())
            .thenReturn(Result.failure(exception))
            .thenReturn(Result.success(expectedCategories))

        viewModel.loadCategories()
        advanceUntilIdle()
        viewModel.uiState.first { it.error != null }

        assertThat(savedStateHandle.get<CategoriesUiState>("ui_state")?.error).contains(errorMessage)

        viewModel.retry()
        advanceUntilIdle()
        viewModel.uiState.first { !it.isLoading && it.error == null }

        val savedState = savedStateHandle.get<CategoriesUiState>("ui_state")
        assertThat(savedState?.error).isNull()
    }

    @Test
    fun `loadCategories should handle multiple rapid calls correctly`() = runTest {

        val categories1 = TestDataFactory.createCategoryList(2)
        val categories2 = TestDataFactory.createCategoryList(4)

        whenever(mockUseCase())
            .thenReturn(Result.success(categories1))
            .thenReturn(Result.success(categories2))


        viewModel.loadCategories()
        advanceUntilIdle()
        val state1 = viewModel.uiState.first { !it.isLoading && it.error == null }
        assertThat(state1.categories).isEqualTo(categories1)

        viewModel.loadCategories()
        advanceUntilIdle()
        val state2 = viewModel.uiState.first { !it.isLoading && it.error == null }

        assertThat(state2.categories).isEqualTo(categories2)
    }

    @Test
    fun `loadCategories should restore state from SavedStateHandle on ViewModel creation`() = runTest {

        val savedCategories = TestDataFactory.createCategoryList(3)
        val savedState = CategoriesUiState(
            isLoading = false,
            categories = savedCategories,
            error = null
        )
        savedStateHandle.set("ui_state", savedState)

        val newViewModel = CategoriesViewModel(mockUseCase, savedStateHandle)

        val restoredState = newViewModel.uiState.first()
        assertThat(restoredState.categories).isEqualTo(savedCategories)
        assertThat(restoredState.isLoading).isFalse()
        assertThat(restoredState.error).isNull()

        verify(mockUseCase, never()).invoke()
    }
}