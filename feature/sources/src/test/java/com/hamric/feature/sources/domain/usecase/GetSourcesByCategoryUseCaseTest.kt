package com.hamric.feature.sources.domain.usecase

import com.hamric.feature.sources.domain.repository.SourcesRepository
import com.hamric.feature.sources.utils.CoroutineTestRule
import com.hamric.feature.sources.utils.TestDataFactory
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
class GetSourcesByCategoryUseCaseTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var useCase: GetSourcesByCategoryUseCase
    private val mockRepository: SourcesRepository = mock()
    private val categoryId = "general"

    @Before
    fun setup() {
        useCase = GetSourcesByCategoryUseCase(mockRepository)
    }



    @Test
    fun `invoke should return sources when repository returns success`() = runTest {

        val expectedSources = TestDataFactory.createSourceList(3)
        whenever(mockRepository.getSourcesByCategory(categoryId))
            .thenReturn(Result.success(expectedSources))


        val result = useCase(categoryId)


        assertThat(result.isSuccess).isTrue()
        val sources = result.getOrNull()
        assertThat(sources).isEqualTo(expectedSources)
        verify(mockRepository).getSourcesByCategory(categoryId)
    }

    @Test
    fun `invoke should return empty list when repository returns empty list`() = runTest {

        whenever(mockRepository.getSourcesByCategory(categoryId))
            .thenReturn(Result.success(emptyList()))


        val result = useCase(categoryId)


        assertThat(result.isSuccess).isTrue()
        val sources = result.getOrNull()
        assertThat(sources).isEmpty()
        verify(mockRepository).getSourcesByCategory(categoryId)
    }


    @Test
    fun `invoke should return failure when repository returns error`() = runTest {

        val exception = Exception("Repository error")
        whenever(mockRepository.getSourcesByCategory(categoryId))
            .thenReturn(Result.failure(exception))


        val result = useCase(categoryId)


        assertThat(result.isFailure).isTrue()
        val caughtException = result.exceptionOrNull()
        assertThat(caughtException).isEqualTo(exception)
        verify(mockRepository).getSourcesByCategory(categoryId)
    }

    @Test
    fun `invoke should propagate specific error messages from repository`() = runTest {

        val errorMessage = "Failed to load sources"
        val exception = Exception(errorMessage)
        whenever(mockRepository.getSourcesByCategory(categoryId))
            .thenReturn(Result.failure(exception))


        val result = useCase(categoryId)


        assertThat(result.isFailure).isTrue()
        val caughtException = result.exceptionOrNull()
        assertThat(caughtException?.message).isEqualTo(errorMessage)
        verify(mockRepository).getSourcesByCategory(categoryId)
    }
}