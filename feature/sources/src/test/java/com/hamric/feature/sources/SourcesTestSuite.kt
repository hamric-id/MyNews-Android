package com.hamric.feature.sources

import com.hamric.feature.sources.data.cache.SourceCacheTest
import com.hamric.feature.sources.data.repository.SourcesRepositoryImplTest
import com.hamric.feature.sources.domain.usecase.GetSourcesByCategoryUseCaseTest
import com.hamric.feature.sources.presentation.viewmodel.SourcesViewModelTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    SourceCacheTest::class,
    SourcesRepositoryImplTest::class,
    GetSourcesByCategoryUseCaseTest::class,
    SourcesViewModelTest::class
)
class SourcesTestSuite