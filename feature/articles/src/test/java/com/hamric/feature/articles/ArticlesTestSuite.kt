package com.hamric.feature.categories


import com.hamric.feature.articles.data.paging.ArticlePagingSourceTest
import com.hamric.feature.articles.data.repository.ArticleRepositoryImplTest
import com.hamric.feature.articles.domain.usecase.GetArticlesBySourceUseCaseTest
import com.hamric.feature.articles.presentation.viewmodel.ArticlesViewModelTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    ArticlePagingSourceTest::class,
    ArticleRepositoryImplTest::class,
    GetArticlesBySourceUseCaseTest::class,
    ArticlesViewModelTest::class
)
class ArticlesTestSuite