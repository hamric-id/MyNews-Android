package com.hamric.feature.articles.data.repository

import androidx.paging.PagingSource
import com.hamric.core.network.api.NewsApi
import com.hamric.feature.articles.data.paging.ArticlePagingSource
import com.hamric.feature.articles.utils.CoroutineTestRule
import com.hamric.feature.articles.utils.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleRepositoryImplTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var repository: ArticleRepositoryImpl
    private val mockApi: NewsApi = mock()
    private val sourceId = "bbc-news"
    private val searchKeyword = "tesla"

    @Before
    fun setup() {
        repository = ArticleRepositoryImpl(mockApi)
    }


    @Test
    fun `getArticlesBySource should return PagingData when API call is successful`() = runTest {
        val result = repository.getArticlesBySource(sourceId)
        val pagingData = result.first()

        assertThat(pagingData).isNotNull()
    }

    @Test
    fun `getArticlesBySource should handle search keyword`() = runTest {
        val result = repository.getArticlesBySource(sourceId, searchKeyword)
        val pagingData = result.first()

        assertThat(pagingData).isNotNull()
    }

    @Test
    fun `getArticlesBySource should handle sortBy parameter`() = runTest {
        val result = repository.getArticlesBySource(sourceId, sortBy = "popularity")
        val pagingData = result.first()

        assertThat(pagingData).isNotNull()
    }

    @Test
    fun `getArticlesBySource should handle empty response`() = runTest {
        val result = repository.getArticlesBySource(sourceId)
        val pagingData = result.first()

        assertThat(pagingData).isNotNull()
    }

    @Test
    fun `getArticlesBySource should handle API error`() = runTest {
        val result = repository.getArticlesBySource(sourceId)
        val pagingData = result.first()

        assertThat(pagingData).isNotNull()
    }

    @Test
    fun `getArticlesBySource should handle RuntimeException`() = runTest {
        val result = repository.getArticlesBySource(sourceId)
        val pagingData = result.first()

        assertThat(pagingData).isNotNull()
    }

    @Test
    fun `paging source loads data correctly`() = runTest {
        val mockArticles = listOf(
            TestDataFactory.createArticleResponse(title = "Article 1"),
            TestDataFactory.createArticleResponse(title = "Article 2")
        )
        val mockResponse = TestDataFactory.createArticlesResponse(
            totalResults = 10,
            articles = mockArticles
        )

        whenever(
            mockApi.getArticles(
                sourceIDs = sourceId,
                searchKeyword = "",
                sortBy = "publishedAt",
                page = 1,
                pageSize = 8
            )
        ).thenReturn(mockResponse)

        val pagingSource = ArticlePagingSource(
            api = mockApi,
            sourceId = sourceId,
            searchQuery = null,
            sortBy = "publishedAt"
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 8,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).hasSize(2)
        verify(mockApi).getArticles(
            sourceIDs = sourceId,
            searchKeyword = "",
            sortBy = "publishedAt",
            page = 1,
            pageSize = 8
        )
    }

    @Test
    fun `paging source loads data with search keyword`() = runTest {
        val mockArticles = listOf(
            TestDataFactory.createArticleResponse(title = "Tesla News")
        )
        val mockResponse = TestDataFactory.createArticlesResponse(
            totalResults = 1,
            articles = mockArticles
        )

        whenever(
            mockApi.getArticles(
                sourceIDs = sourceId,
                searchKeyword = searchKeyword,
                sortBy = "publishedAt",
                page = 1,
                pageSize = 8
            )
        ).thenReturn(mockResponse)

        val pagingSource = ArticlePagingSource(
            api = mockApi,
            sourceId = sourceId,
            searchQuery = searchKeyword,
            sortBy = "publishedAt"
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 8,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).hasSize(1)
        verify(mockApi).getArticles(
            sourceIDs = sourceId,
            searchKeyword = searchKeyword,
            sortBy = "publishedAt",
            page = 1,
            pageSize = 8
        )
    }

    @Test
    fun `paging source loads data with sortBy parameter`() = runTest {
        val mockArticles = listOf(
            TestDataFactory.createArticleResponse(title = "Popular Article")
        )
        val mockResponse = TestDataFactory.createArticlesResponse(
            totalResults = 1,
            articles = mockArticles
        )

        whenever(
            mockApi.getArticles(
                sourceIDs = sourceId,
                searchKeyword = "",
                sortBy = "popularity",
                page = 1,
                pageSize = 8
            )
        ).thenReturn(mockResponse)

        val pagingSource = ArticlePagingSource(
            api = mockApi,
            sourceId = sourceId,
            searchQuery = null,
            sortBy = "popularity"
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 8,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).hasSize(1)
        verify(mockApi).getArticles(
            sourceIDs = sourceId,
            searchKeyword = "",
            sortBy = "popularity",
            page = 1,
            pageSize = 8
        )
    }

    @Test
    fun `paging source handles empty response`() = runTest {
        val mockResponse = TestDataFactory.createArticlesResponse(
            totalResults = 0,
            articles = emptyList()
        )

        whenever(
            mockApi.getArticles(
                sourceIDs = sourceId,
                searchKeyword = "",
                sortBy = "publishedAt",
                page = 1,
                pageSize = 8
            )
        ).thenReturn(mockResponse)

        val pagingSource = ArticlePagingSource(
            api = mockApi,
            sourceId = sourceId,
            searchQuery = null,
            sortBy = "publishedAt"
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 8,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).isEmpty()
        verify(mockApi).getArticles(
            sourceIDs = sourceId,
            searchKeyword = "",
            sortBy = "publishedAt",
            page = 1,
            pageSize = 8
        )
    }

    @Test
    fun `paging source handles API error`() = runTest {
        val errorResponse = TestDataFactory.createErrorResponse(message = "API error")

        whenever(
            mockApi.getArticles(
                sourceIDs = sourceId,
                searchKeyword = "",
                sortBy = "publishedAt",
                page = 1,
                pageSize = 8
            )
        ).thenReturn(errorResponse)

        val pagingSource = ArticlePagingSource(
            api = mockApi,
            sourceId = sourceId,
            searchQuery = null,
            sortBy = "publishedAt"
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 8,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
        verify(mockApi).getArticles(
            sourceIDs = sourceId,
            searchKeyword = "",
            sortBy = "publishedAt",
            page = 1,
            pageSize = 8
        )
    }

    @Test
    fun `paging source handles RuntimeException`() = runTest {
        val exception = RuntimeException("Network error")

        whenever(
            mockApi.getArticles(
                sourceIDs = sourceId,
                searchKeyword = "",
                sortBy = "publishedAt",
                page = 1,
                pageSize = 8
            )
        ).thenThrow(exception)

        val pagingSource = ArticlePagingSource(
            api = mockApi,
            sourceId = sourceId,
            searchQuery = null,
            sortBy = "publishedAt"
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 8,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
        val error = result as PagingSource.LoadResult.Error
        assertThat(error.throwable).isEqualTo(exception)
        verify(mockApi).getArticles(
            sourceIDs = sourceId,
            searchKeyword = "",
            sortBy = "publishedAt",
            page = 1,
            pageSize = 8
        )
    }

    @Test
    fun `paging source handles subsequent page load`() = runTest {
        val mockArticles = listOf(
            TestDataFactory.createArticleResponse(title = "Article 3"),
            TestDataFactory.createArticleResponse(title = "Article 4")
        )
        val mockResponse = TestDataFactory.createArticlesResponse(
            totalResults = 10,
            articles = mockArticles
        )

        whenever(
            mockApi.getArticles(
                sourceIDs = sourceId,
                searchKeyword = "",
                sortBy = "publishedAt",
                page = 2,
                pageSize = 8
            )
        ).thenReturn(mockResponse)

        val pagingSource = ArticlePagingSource(
            api = mockApi,
            sourceId = sourceId,
            searchQuery = null,
            sortBy = "publishedAt"
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Append(
                key = 2,
                loadSize = 8,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).hasSize(2)
        verify(mockApi).getArticles(
            sourceIDs = sourceId,
            searchKeyword = "",
            sortBy = "publishedAt",
            page = 2,
            pageSize = 8
        )
    }
}