package com.hamric.mynews

import android.os.Bundle
import android.util.Log
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
import com.hamric.feature.categories.presentation.ui.CategoriesScreen
import com.hamric.feature.sources.presentation.ui.SourcesScreen
import dagger.hilt.android.AndroidEntryPoint

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
                    Log.d("test","trigger to open article screen of ${source.id} = ${source.name}")
                    // navController.navigate("articles/${source.id}/${source.name}")
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}