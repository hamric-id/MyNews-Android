package com.hamric.feature.articles.data.paging

import androidx.paging.PagingSource
import com.hamric.core.network.api.NewsApi
import com.hamric.feature.articles.utils.CoroutineTestRule
import com.hamric.feature.articles.utils.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.google.common.truth.Truth.assertThat
import com.hamric.core.model.Article

@OptIn(ExperimentalCoroutinesApi::class)
class ArticlePagingSourceTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var pagingSource: ArticlePagingSource
    private val mockApi: NewsApi = mock()
    private val sourceId = "bbc-news"
    private val searchQuery = "tesla"

    @Before
    fun setup() {
        pagingSource = ArticlePagingSource(
            api = mockApi,
            sourceId = sourceId
        )
    }


    @Test
    fun `load should return success with articles for first page`() = runTest {

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
                pageSize = 20
            )
        ).thenReturn(mockResponse)


        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )


        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
        val pageResult = result as PagingSource.LoadResult.Page<Int, Article>
        assertThat(pageResult.data).hasSize(2)
        assertThat(pageResult.data[0].title).isEqualTo("Article 1")
        assertThat(pageResult.prevKey).isNull()
    }

    @Test
    fun `load should return success with search results`() = runTest {

        val searchPagingSource = ArticlePagingSource(
            api = mockApi,
            searchQuery = searchQuery
        )
        val mockArticles = listOf(
            TestDataFactory.createArticleResponse(title = "Tesla News"),
            TestDataFactory.createArticleResponse(title = "Tesla Stock")
        )
        val mockResponse = TestDataFactory.createArticlesResponse(
            totalResults = 5,
            articles = mockArticles
        )
        whenever(
            mockApi.getArticles(
                sourceIDs = "",
                searchKeyword = searchQuery,
                sortBy = "publishedAt",
                page = 1,
                pageSize = 20
            )
        ).thenReturn(mockResponse)


        val result = searchPagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )


        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
        val pageResult = result as PagingSource.LoadResult.Page<Int, Article>
        assertThat(pageResult.data).hasSize(2)
        assertThat(pageResult.data[0].title).contains("Tesla")
    }

    @Test
    fun `load should return success with sortBy parameter`() = runTest {

        val sortPagingSource = ArticlePagingSource(
            api = mockApi,
            sourceId = sourceId,
            sortBy = "popularity"
        )
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
                pageSize = 20
            )
        ).thenReturn(mockResponse)


        val result = sortPagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )


        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
        val pageResult = result as PagingSource.LoadResult.Page<Int, Article>
        assertThat(pageResult.data).hasSize(1)
    }

    @Test
    fun `load should return empty page when no articles`() = runTest {

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
                pageSize = 20
            )
        ).thenReturn(mockResponse)


        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )


        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
        val pageResult = result as PagingSource.LoadResult.Page<Int, Article>
        assertThat(pageResult.data).isEmpty()
    }


    @Test
    fun `load should return error when API returns error status`() = runTest {

        val errorMessage = "API key invalid"
        val mockResponse = TestDataFactory.createErrorResponse(message = errorMessage)
        whenever(
            mockApi.getArticles(
                sourceIDs = sourceId,
                searchKeyword = "",
                sortBy = "publishedAt",
                page = 1,
                pageSize = 20
            )
        ).thenReturn(mockResponse)


        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )


        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
        val errorResult = result as PagingSource.LoadResult.Error<Int, Article>
        assertThat(errorResult.throwable.message).contains(errorMessage)
    }

    @Test
    fun `load should return error when API throws RuntimeException`() = runTest {

        val exception = RuntimeException("Network error")
        whenever(
            mockApi.getArticles(
                sourceIDs = sourceId,
                searchKeyword = "",
                sortBy = "publishedAt",
                page = 1,
                pageSize = 20
            )
        ).thenThrow(exception)


        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )


        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
        val errorResult = result as PagingSource.LoadResult.Error<Int, Article>
        assertThat(errorResult.throwable).isEqualTo(exception)
    }

    @Test
    fun `load should return error when API throws generic exception`() = runTest {

        val exception = RuntimeException("Server error")
        whenever(
            mockApi.getArticles(
                sourceIDs = sourceId,
                searchKeyword = "",
                sortBy = "publishedAt",
                page = 1,
                pageSize = 20
            )
        ).thenThrow(exception)


        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )


        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
        val errorResult = result as PagingSource.LoadResult.Error<Int, Article>
        assertThat(errorResult.throwable).isEqualTo(exception)
    }

    @Test
    fun `load should return error when API throws HttpException style error`() = runTest {

        val exception = RuntimeException("HTTP 429 Rate limit exceeded")
        whenever(
            mockApi.getArticles(
                sourceIDs = sourceId,
                searchKeyword = "",
                sortBy = "publishedAt",
                page = 1,
                pageSize = 20
            )
        ).thenThrow(exception)


        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )


        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
        val errorResult = result as PagingSource.LoadResult.Error<Int, Article>
        assertThat(errorResult.throwable.message).contains("429")
    }
}