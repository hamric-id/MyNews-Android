package com.hamric.feature.categories.domain.usecase

import com.hamric.core.model.Category
import com.hamric.feature.categories.domain.repository.CategoryRepository
import com.hamric.feature.categories.utils.CoroutineTestRule
import com.hamric.feature.categories.utils.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class GetCategoriesUseCaseTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var useCase: GetCategoriesUseCase
    private val mockRepository: CategoryRepository = mock()

    @Before
    fun setup() {
        useCase = GetCategoriesUseCase(mockRepository)
    }

    @Test
    fun `invoke should return categories when repository returns success`() = runTest {

        val expectedCategories = TestDataFactory.createCategoryList(3)
        whenever(mockRepository.getCategories()).thenReturn(Result.success(expectedCategories))


        val result = useCase()


        assertThat(result.isSuccess).isTrue()
        val categories = result.getOrNull()
        assertThat(categories).isEqualTo(expectedCategories)

        verify(mockRepository).getCategories()
    }

    @Test
    fun `invoke should return empty list when repository returns empty list`() = runTest {

        val emptyList = emptyList<Category>()
        whenever(mockRepository.getCategories()).thenReturn(Result.success(emptyList))


        val result = useCase()


        assertThat(result.isSuccess).isTrue()
        val categories = result.getOrNull()
        assertThat(categories).isEmpty()

        verify(mockRepository).getCategories()
    }


    @Test
    fun `invoke should return failure when repository returns error`() = runTest {

        val exception = RuntimeException("Repository error")
        whenever(mockRepository.getCategories()).thenReturn(Result.failure(exception))


        val result = useCase()


        assertThat(result.isFailure).isTrue()
        val caughtException = result.exceptionOrNull()
        assertThat(caughtException).isEqualTo(exception)

        verify(mockRepository).getCategories()
    }

    @Test
    fun `invoke should propagate specific error messages from repository`() = runTest {

        val errorMessage = "No internet connection"
        val exception = Exception(errorMessage)
        whenever(mockRepository.getCategories()).thenReturn(Result.failure(exception))


        val result = useCase()


        assertThat(result.isFailure).isTrue()
        val caughtException = result.exceptionOrNull()
        assertThat(caughtException?.message).isEqualTo(errorMessage)

        verify(mockRepository).getCategories()
    }
}