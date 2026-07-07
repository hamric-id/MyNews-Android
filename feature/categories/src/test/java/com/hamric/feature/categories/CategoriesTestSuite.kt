package com.hamric.feature.categories

import com.hamric.feature.categories.data.repository.CategoryRepositoryImplTest
import com.hamric.feature.categories.domain.usecase.GetCategoriesUseCaseTest
import com.hamric.feature.categories.presentation.viewmodel.CategoriesViewModelTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    CategoryRepositoryImplTest::class,
    GetCategoriesUseCaseTest::class,
    CategoriesViewModelTest::class
)
class CategoriessTestSuite