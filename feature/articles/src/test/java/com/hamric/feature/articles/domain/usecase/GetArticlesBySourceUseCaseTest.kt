package com.hamric.feature.articles.domain.usecase

import androidx.paging.PagingData
import com.hamric.core.model.Article
import com.hamric.feature.articles.domain.repository.ArticleRepository
import com.hamric.feature.articles.utils.CoroutineTestRule
import com.hamric.feature.articles.utils.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class GetArticlesBySourceUseCaseTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var useCase: GetArticlesBySourceUseCase
    private val mockRepository: ArticleRepository = mock()
    private val sourceId = "bbc-news"
    private val searchKeyword = "tesla"

    @Before
    fun setup() {
        useCase = GetArticlesBySourceUseCase(mockRepository)
    }

    @Test
    fun `invoke should return PagingData from repository`() = runTest {

        val expectedArticles = TestDataFactory.createArticleList(3)
        val pagingData = PagingData.from(expectedArticles)
        whenever(
            mockRepository.getArticlesBySource(
                sourceId = sourceId,
                searchKeyword = null,
                sortBy = "publishedAt"
            )
        ).thenReturn(flowOf(pagingData))


        val result = useCase(sourceId)


        assertThat(result).isNotNull()
        verify(mockRepository).getArticlesBySource(
            sourceId = sourceId,
            searchKeyword = null,
            sortBy = "publishedAt"
        )
    }

    @Test
    fun `invoke should pass searchKeyword to repository`() = runTest {

        val expectedArticles = TestDataFactory.createArticleList(2)
        val pagingData = PagingData.from(expectedArticles)
        whenever(
            mockRepository.getArticlesBySource(
                sourceId = sourceId,
                searchKeyword = searchKeyword,
                sortBy = "publishedAt"
            )
        ).thenReturn(flowOf(pagingData))


        val result = useCase(sourceId, searchKeyword)


        assertThat(result).isNotNull()
        verify(mockRepository).getArticlesBySource(
            sourceId = sourceId,
            searchKeyword = searchKeyword,
            sortBy = "publishedAt"
        )
    }

    @Test
    fun `invoke should pass sortBy to repository`() = runTest {

        val expectedArticles = TestDataFactory.createArticleList(2)
        val pagingData = PagingData.from(expectedArticles)
        whenever(
            mockRepository.getArticlesBySource(
                sourceId = sourceId,
                searchKeyword = null,
                sortBy = "popularity"
            )
        ).thenReturn(flowOf(pagingData))


        val result = useCase(sourceId, sortBy = "popularity")


        assertThat(result).isNotNull()
        verify(mockRepository).getArticlesBySource(
            sourceId = sourceId,
            searchKeyword = null,
            sortBy = "popularity"
        )
    }

    @Test
    fun `invoke should handle empty result from repository`() = runTest {

        val pagingData = PagingData.from(emptyList<Article>())
        whenever(
            mockRepository.getArticlesBySource(
                sourceId = sourceId,
                searchKeyword = null,
                sortBy = "publishedAt"
            )
        ).thenReturn(flowOf(pagingData))


        val result = useCase(sourceId)


        assertThat(result).isNotNull()
        verify(mockRepository).getArticlesBySource(
            sourceId = sourceId,
            searchKeyword = null,
            sortBy = "publishedAt"
        )
    }
}