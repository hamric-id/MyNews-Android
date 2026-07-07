package com.hamric.feature.categories.data.repository

import com.hamric.core.network.api.NewsApi
import com.hamric.core.network.response.SourcesResponse
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
class CategoryRepositoryImplTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var repository: CategoryRepositoryImpl
    private val mockApi: NewsApi = mock()

    @Before
    fun setup() {
        repository = CategoryRepositoryImpl(mockApi)
    }

    @Test
    fun `getCategories should return success with categories when API call is successful`() = runTest {
        val mockResponse = TestDataFactory.createSourcesResponse()
        whenever(mockApi.getSources()).thenReturn(mockResponse)

        val result = repository.getCategories()
        
        assertThat(result.isSuccess).isTrue()
        val categories = result.getOrNull()
        assertThat(categories).isNotNull()
        assertThat(categories).hasSize(3)
        assertThat(categories?.get(0)?.id).isEqualTo("business")
        assertThat(categories?.get(0)?.name).isEqualTo("Business")
        assertThat(categories?.get(0)?.headlineCount).isEqualTo(2)

        verify(mockApi).getSources()
    }

    @Test
    fun `getCategories should return empty list when API returns empty sources`() = runTest {

        val mockResponse = TestDataFactory.createSourcesResponse(sources = emptyList())
        whenever(mockApi.getSources()).thenReturn(mockResponse)


        val result = repository.getCategories()


        assertThat(result.isSuccess).isTrue()
        val categories = result.getOrNull()
        assertThat(categories).isNotNull()
        assertThat(categories).isEmpty()

        verify(mockApi).getSources()
    }

    @Test
    fun `getCategories should group sources by category correctly`() = runTest {

        val sources = listOf(
            TestDataFactory.createSourceResponse(category = "business", id = "b1", name = "B1"),
            TestDataFactory.createSourceResponse(category = "business", id = "b2", name = "B2"),
            TestDataFactory.createSourceResponse(category = "technology", id = "t1", name = "T1"),
            TestDataFactory.createSourceResponse(category = "sports", id = "s1", name = "S1"),
            TestDataFactory.createSourceResponse(category = "sports", id = "s2", name = "S2"),
            TestDataFactory.createSourceResponse(category = "sports", id = "s3", name = "S3")
        )
        val mockResponse = TestDataFactory.createSourcesResponse(sources = sources)
        whenever(mockApi.getSources()).thenReturn(mockResponse)


        val result = repository.getCategories()


        assertThat(result.isSuccess).isTrue()
        val categories = result.getOrNull()
        assertThat(categories).isNotNull()
        assertThat(categories).hasSize(3)

        val business = categories?.find { it.id == "business" }
        assertThat(business).isNotNull()
        assertThat(business?.headlineCount).isEqualTo(2)

        val sports = categories?.find { it.id == "sports" }
        assertThat(sports).isNotNull()
        assertThat(sports?.headlineCount).isEqualTo(3)

        val tech = categories?.find { it.id == "technology" }
        assertThat(tech).isNotNull()
        assertThat(tech?.headlineCount).isEqualTo(1)

        verify(mockApi).getSources()
    }

    @Test
    fun `getCategories should return sorted categories by name`() = runTest {

        val categories = listOf(
            "sports", "business", "technology", "entertainment", "health"
        )
        val sources = categories.map { category ->
            TestDataFactory.createSourceResponse(
                category = category,
                id = "${category}-1",
                name = "${category}-source"
            )
        }
        val mockResponse = TestDataFactory.createSourcesResponse(sources = sources)
        whenever(mockApi.getSources()).thenReturn(mockResponse)


        val result = repository.getCategories()


        assertThat(result.isSuccess).isTrue()
        val resultCategories = result.getOrNull()
        assertThat(resultCategories).isNotNull()
        assertThat(resultCategories?.get(0)?.id).isEqualTo("business")
        assertThat(resultCategories?.get(1)?.id).isEqualTo("entertainment")
        assertThat(resultCategories?.get(2)?.id).isEqualTo("health")
        assertThat(resultCategories?.get(3)?.id).isEqualTo("sports")
        assertThat(resultCategories?.get(4)?.id).isEqualTo("technology")
    }


    @Test
    fun `getCategories should return failure with error message when API returns error status`() = runTest {

        val errorMessage = "API key invalid"
        val mockResponse = TestDataFactory.createErrorResponse(message = errorMessage)
        whenever(mockApi.getSources()).thenReturn(mockResponse)


        val result = repository.getCategories()


        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception).isNotNull()
        assertThat(exception?.message).contains(errorMessage)

        verify(mockApi).getSources()
    }

    @Test
    fun `getCategories should return failure with default message when API error has no message`() = runTest {

        val mockResponse = SourcesResponse(
            status = "error",
            sources = emptyList(),
            message = null,
            code = "unknown"
        )
        whenever(mockApi.getSources()).thenReturn(mockResponse)


        val result = repository.getCategories()


        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception).isNotNull()
        assertThat(exception?.message).isEqualTo("Failed to fetch categories")

        verify(mockApi).getSources()
    }


    @Test
    fun `getCategories should return failure with network error when UnknownHostException occurs`() = runTest {
        val exception = RuntimeException("No internet connection. Please check your network.")
        whenever(mockApi.getSources()).thenThrow(exception)


        val result = repository.getCategories()


        assertThat(result.isFailure).isTrue()
        val caughtException = result.exceptionOrNull()
        assertThat(caughtException).isNotNull()
        assertThat(caughtException?.message).isEqualTo("No internet connection. Please check your network.")

        verify(mockApi).getSources()
    }

    @Test
    fun `getCategories should return failure with timeout error when SocketTimeoutException occurs`() = runTest {
        val exception = RuntimeException("Connection timeout. Please try again.")
        whenever(mockApi.getSources()).thenThrow(exception)


        val result = repository.getCategories()


        assertThat(result.isFailure).isTrue()
        val caughtException = result.exceptionOrNull()
        assertThat(caughtException).isNotNull()
        assertThat(caughtException?.message).isEqualTo("Connection timeout. Please try again.")

        verify(mockApi).getSources()
    }

    @Test
    fun `getCategories should return failure with IO error when IOException occurs`() = runTest {
        val exception = RuntimeException("Network error. Please check your connection.")
        whenever(mockApi.getSources()).thenThrow(exception)


        val result = repository.getCategories()


        assertThat(result.isFailure).isTrue()
        val caughtException = result.exceptionOrNull()
        assertThat(caughtException).isNotNull()
        assertThat(caughtException?.message).isEqualTo("Network error. Please check your connection.")

        verify(mockApi).getSources()
    }

    @Test
    fun `getCategories should return failure with generic error for unknown exceptions`() = runTest {

        val exception = RuntimeException("Something went wrong")
        whenever(mockApi.getSources()).thenThrow(exception)


        val result = repository.getCategories()


        assertThat(result.isFailure).isTrue()
        val caughtException = result.exceptionOrNull()
        assertThat(caughtException).isNotNull()
        assertThat(caughtException?.message).isEqualTo("Something went wrong")

        verify(mockApi).getSources()
    }
}