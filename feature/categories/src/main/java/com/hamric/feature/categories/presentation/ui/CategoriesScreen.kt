package com.hamric.feature.categories.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hamric.core.designsystem.components.EmptyState
import com.hamric.core.designsystem.components.ErrorState
import com.hamric.core.designsystem.components.LoadingIndicator
import com.hamric.core.designsystem.ui.theme.MyNewsTheme
import com.hamric.core.model.Category
import com.hamric.feature.categories.presentation.viewmodel.CategoriesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel = hiltViewModel(),
    onCategoryClick: (Category) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "News Categories",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingIndicator(modifier = Modifier.padding(paddingValues))
            }
            uiState.error != null -> {
                ErrorState(
                    message = uiState.error ?: "Unknown error",
                    onRetry = { viewModel.retry() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            uiState.categories.isEmpty() -> {
                EmptyState(
                    message = "No categories available",
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                CategoryList(
                    categories = uiState.categories,
                    onCategoryClick = onCategoryClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun CategoryList(
    categories: List<Category>,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories, key = { it.id }) { category ->
            CategoryItem(
                category = category,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            if (category.headlineCount > 0) {
                Text(
                    text = "${category.headlineCount} News on Headline",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(
    name = "Categories Screen Preview",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5,
    heightDp = 800,
    widthDp = 400
)
@Composable
fun PreviewCategoriesScreen() {
    MyNewsTheme {
        CategoriesScreenPreviewContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreenPreviewContent() {
    val sampleCategories = listOf(
        Category(id = "business", name = "Business",  headlineCount = 15),
        Category(id = "technology", name = "Technology",  headlineCount = 25),
        Category(id = "sports", name = "Sports",  headlineCount = 18),
        Category(id = "entertainment", name = "Entertainment", headlineCount = 12),
        Category(id = "science", name = "Science", headlineCount = 10),
        Category(id = "health", name = "Health",headlineCount = 8)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("News Categories", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        CategoryList(
            categories = sampleCategories,
            onCategoryClick = {},
            modifier = Modifier.padding(paddingValues)
        )
    }
}