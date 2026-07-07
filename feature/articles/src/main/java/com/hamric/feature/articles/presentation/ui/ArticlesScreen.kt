package com.hamric.feature.articles.presentation.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.hamric.core.designsystem.components.EmptyState
import com.hamric.core.designsystem.components.ErrorState
import com.hamric.core.designsystem.components.LoadingIndicator
import com.hamric.core.designsystem.ui.theme.MyNewsTheme
import com.hamric.core.model.Article
import com.hamric.feature.articles.presentation.ui.components.ArticleItem
import com.hamric.feature.articles.presentation.ui.components.CompactSortDropdown
import com.hamric.feature.articles.presentation.viewmodel.ArticlesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreen(
    sourceId: String,
    sourceName: String,
    viewModel: ArticlesViewModel = hiltViewModel(),
    onArticleClick: (Article) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val articles by viewModel.articles.collectAsState()

    LaunchedEffect(sourceId) {
        viewModel.loadArticles(sourceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sourceName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.searchArticles(it) },
                onClear = { viewModel.clearSearch() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactSortDropdown(
                    sortBy = uiState.sortBy,
                    onSortByChange = { viewModel.updateSortBy(it) },
                    hasActiveFilter = uiState.sortBy != "publishedAt",
                    onClearFilter = { viewModel.clearSortFilter() },
                    modifier = Modifier
                                .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.weight(1f)  )
                if (articles != null && uiState.searchQuery.isNotBlank()) {
                    val pagingItems = articles?.collectAsLazyPagingItems()
                    val totalItems = pagingItems?.itemCount ?: 0
                    Text(
                        modifier = Modifier.padding(end = 18.dp),
                        text = "Search results: $totalItems articles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (uiState.isLoading) {
                LoadingIndicator(modifier = Modifier.fillMaxSize())
            } else if (uiState.error != null) {
                ErrorState(
                    message = uiState.error ?: "Unknown error",
                    onRetry = { viewModel.retry() },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (articles != null) {
                val pagingItems = articles?.collectAsLazyPagingItems()

                if (pagingItems?.loadState?.refresh is LoadState.Error) {
                    val error = pagingItems.loadState.refresh as LoadState.Error
                    ErrorState(
                        message = error.error.message ?: "Failed to load articles",
                        onRetry = { pagingItems.retry() },
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (pagingItems?.itemCount == 0) {
                    val message = if (uiState.searchQuery.isNotBlank()) {
                        "No articles match your search: \"${uiState.searchQuery}\""
                    } else {
                        "No articles available from this source"
                    }
                    EmptyState(
                        message = message,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        if (pagingItems != null) {
                            items(
                                count = pagingItems.itemCount,
                                key = { index -> pagingItems[index]?.id ?: index }
                            ) { index ->
                                val article = pagingItems[index]
                                article?.let {
                                    ArticleItem(
                                        article = it,
                                        onClick = onArticleClick
                                    )
                                }
                            }
                        }

                        if (pagingItems?.loadState?.append is LoadState.Loading) {
                            item {
                                LoadingIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            }
                        }

                        if (pagingItems?.loadState?.append is LoadState.Error) {
                            item {
                                val error = pagingItems.loadState.append as LoadState.Error
                                ErrorState(
                                    message = error.error.message ?: "Failed to load more articles",
                                    onRetry = { pagingItems.retry() },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            } else {
                LoadingIndicator(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search articles...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(
    name = "Articles Screen - Search 'hello'",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5,
    heightDp = 850,
    widthDp = 400
)
@Composable
fun PreviewArticlesScreenWithSearch() {
    MyNewsTheme {
        ArticlesScreen(
            sourceId = "techcrunch",
            sourceName = "TechCrunch",
            viewModel = PreviewArticlesViewModel(
                searchQuery = "hello"
            ),
            onArticleClick = {  },
            onBack = {  }
        )
    }
}

@Suppress("UNUSED_PARAMETER")
class PreviewArticlesViewModel(
    private val isLoading: Boolean = false,
    private val searchQuery: String = "",
    private val showError: Boolean = false,
    private val errorMessage: String? = null,
    private val showEmptyState: Boolean = false
) : ArticlesViewModel(
    getArticlesBySourceUseCase = mockGetArticlesBySourceUseCase(),
    savedStateHandle = androidx.lifecycle.SavedStateHandle()
) {
    init {
        val state = com.hamric.feature.articles.presentation.state.ArticlesUiState(
            isLoading = isLoading,
            searchQuery = searchQuery,
            sortBy = "publishedAt",
            error = if (showError) errorMessage else null,
            isRefreshing = false,
            isInitialLoad = isLoading
        )
    }
}


@Suppress("UNUSED_PARAMETER")
private fun mockGetArticlesBySourceUseCase(): com.hamric.feature.articles.domain.usecase.GetArticlesBySourceUseCase {
    return object : com.hamric.feature.articles.domain.usecase.GetArticlesBySourceUseCase(
        repository = object : com.hamric.feature.articles.domain.repository.ArticleRepository {
            override fun getArticlesBySource(
                sourceId: String,
                searchKeyword: String?,
                sortBy: String
            ): kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<Article>> {
                val articles = createSampleArticlesWithSearch()
                return kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.from(articles))
            }
        }
    ) {
    }
}

private fun createSampleArticlesWithSearch(): List<Article> {
    return listOf(
        Article(
            id = "1",
            title = "Hello, World! A New Era of Programming",
            description = "The classic 'Hello World' program celebrates its 50th anniversary with a look at how programming languages have evolved.",
            url = "https://example.com/hello-world",
            urlToImage = "https://picsum.photos/seed/hello/200/150",
            publishedAt = "2026-07-08T10:30:00Z",
            author = "Sarah Lee",
            sourceName = "TechCrunch",
            sourceId = "techcrunch"
        ),
        Article(
            id = "2",
            title = "Hello AI: How Voice Assistants Are Changing Our Lives",
            description = "From 'Hello Siri' to 'Hey Google', voice assistants are becoming an integral part of our daily routines.",
            url = "https://example.com/hello-ai",
            urlToImage = "https://picsum.photos/seed/voice/200/150",
            publishedAt = "2026-07-07T14:20:00Z",
            author = "John Doe",
            sourceName = "Tech Insider",
            sourceId = "tech-insider"
        ),
        Article(
            id = "3",
            title = "Hello Quantum: The Future of Computing",
            description = "Scientists say 'hello' to a new era of quantum computing that could revolutionize everything from medicine to finance.",
            url = "https://example.com/hello-quantum",
            urlToImage = "https://picsum.photos/seed/quantum/200/150",
            publishedAt = "2026-07-06T09:15:00Z",
            author = "Jane Smith",
            sourceName = "Wired",
            sourceId = "wired"
        ),
        Article(
            id = "4",
            title = "Hello Blockchain: Beyond Cryptocurrency",
            description = "Blockchain technology says hello to new applications beyond digital currency, from supply chain to voting systems.",
            url = "https://example.com/hello-blockchain",
            urlToImage = "https://picsum.photos/seed/blockchain/200/150",
            publishedAt = "2026-07-05T16:45:00Z",
            author = "Mike Wilson",
            sourceName = "TechCrunch",
            sourceId = "techcrunch"
        ),
        Article(
            id = "5",
            title = "Hello IoT: Smart Homes Are Getting Smarter",
            description = "The Internet of Things is expanding rapidly, with smart devices saying hello to every corner of our homes.",
            url = "https://example.com/hello-iot",
            urlToImage = "https://picsum.photos/seed/iot/200/150",
            publishedAt = "2026-07-04T11:00:00Z",
            author = "Emily Chen",
            sourceName = "Wired",
            sourceId = "wired"
        )
    )
}
