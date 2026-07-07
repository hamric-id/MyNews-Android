package com.hamric.mynews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hamric.core.designsystem.ui.theme.MyNewsTheme
import com.hamric.core.model.Article
import com.hamric.feature.articles.presentation.ui.ArticleDetailScreen
import com.hamric.feature.articles.presentation.ui.ArticlesScreen
import com.hamric.feature.categories.presentation.ui.CategoriesScreen
import com.hamric.feature.sources.presentation.ui.SourcesScreen
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyNewsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    MyNewsNavigation()
                }
            }
        }
    }
}

@Composable
fun MyNewsNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "categories"
    ) {
        composable("categories") {
            CategoriesScreen(
                onCategoryClick = { category ->
                    navController.navigate("sources/${category.id}/${category.name}")
                }
            )
        }

        composable(
            route = "sources/{categoryId}/{categoryName}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""

            SourcesScreen(
                categoryId = categoryId,
                categoryName = categoryName,
                onSourceClick = { source ->
                    navController.navigate("articles/${source.id}/${source.name}")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "articles/{sourceId}/{sourceName}",
            arguments = listOf(
                navArgument("sourceId") { type = NavType.StringType },
                navArgument("sourceName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sourceId = backStackEntry.arguments?.getString("sourceId") ?: ""
            val sourceName = backStackEntry.arguments?.getString("sourceName") ?: ""

            ArticlesScreen(
                sourceId = sourceId,
                sourceName = sourceName,
                onArticleClick = { article ->
                    val encodedUrl = URLEncoder.encode(article.url, StandardCharsets.UTF_8.toString())
                    navController.navigate("article/$encodedUrl")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "article/{articleUrl}",
            arguments = listOf(
                navArgument("articleUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("articleUrl") ?: ""
            val articleUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())

            val article = Article(
                id = articleUrl,
                title = "",
                description = null,
                content = null,
                url = articleUrl,
                urlToImage = null,
                publishedAt = "",
                author = null,
                sourceName = "",
                sourceId = null
            )

            ArticleDetailScreen(
                article = article,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
