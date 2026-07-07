package com.hamric.feature.categories.presentation.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hamric.core.model.Category
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.google.common.truth.Truth.assertThat
import com.hamric.core.designsystem.components.EmptyState
import com.hamric.core.designsystem.components.ErrorState
import com.hamric.core.designsystem.components.LoadingIndicator
import com.hamric.core.designsystem.ui.theme.MyNewsTheme

@RunWith(AndroidJUnit4::class)
class CategoriesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleCategories = listOf(
        Category(id = "business", name = "Business", sourcesCount = 15),
        Category(id = "technology", name = "Technology", sourcesCount = 25),
        Category(id = "sports", name = "Sports", sourcesCount = 18),
        Category(id = "entertainment", name = "Entertainment", sourcesCount = 12),
        Category(id = "science", name = "Science", sourcesCount = 10),
        Category(id = "health", name = "Health", sourcesCount = 8)
    )

    private var clickedCategory: Category? = null

    @Before
    fun setup() {
        clickedCategory = null
    }

    // ========== RENDERING TESTS ==========

    @Test
    fun `categories screen displays title correctly`() {
        composeTestRule.setContent {
            MyNewsTheme {
                CategoriesScreenPreviewContent()
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("News Categories").assertIsDisplayed()
    }

    @Test
    fun `categories screen displays all categories`() {
        composeTestRule.setContent {
            MyNewsTheme {
                CategoryList(
                    categories = sampleCategories,
                    onCategoryClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        sampleCategories.forEach { category ->
            composeTestRule.onNodeWithText(category.name).assertIsDisplayed()
        }
    }

    @Test
    fun `categories screen displays headline count for each category`() {
        composeTestRule.setContent {
            MyNewsTheme {
                CategoryList(
                    categories = sampleCategories,
                    onCategoryClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        sampleCategories.forEach { category ->
            val countText = "${category.sourcesCount} Source available"
            composeTestRule.onNodeWithText(countText).assertIsDisplayed()
        }
    }

    @Test
    fun `categories screen shows correct number of items`() {
        composeTestRule.setContent {
            MyNewsTheme {
                CategoryList(
                    categories = sampleCategories,
                    onCategoryClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        sampleCategories.forEach { category ->
            composeTestRule.onAllNodesWithText(category.name).assertCountEquals(1)
        }
    }


    // ========== INTERACTION TESTS ==========

    @Test
    fun `clicking on a category triggers onCategoryClick`() {
        var clickedCategoryId: String? = null

        composeTestRule.setContent {
            MyNewsTheme {
                CategoryList(
                    categories = sampleCategories,
                    onCategoryClick = { category ->
                        clickedCategoryId = category.id
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Business").assertIsDisplayed()
        composeTestRule.onNodeWithText("Business").performClick()

        assertThat(clickedCategoryId).isEqualTo("business")
    }

    @Test
    fun `clicking on different categories triggers different callbacks`() {
        var clickedCategoryId: String? = null

        composeTestRule.setContent {
            MyNewsTheme {
                CategoryList(
                    categories = sampleCategories,
                    onCategoryClick = { category ->
                        clickedCategoryId = category.id
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Technology").performClick()
        assertThat(clickedCategoryId).isEqualTo("technology")

        composeTestRule.onNodeWithText("Sports").performClick()
        assertThat(clickedCategoryId).isEqualTo("sports")
    }


    // ========== STATE TESTS ==========

    @Test
    fun `loading state displays loading indicator`() {
        composeTestRule.setContent {
            MyNewsTheme {
                LoadingIndicator()
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }

    @Test
    fun `error state displays error message and retry button`() {
        val errorMessage = "Failed to load categories"
        var retryCalled = false

        composeTestRule.setContent {
            MyNewsTheme {
                ErrorState(
                    message = errorMessage,
                    onRetry = { retryCalled = true }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("❌ $errorMessage").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed().performClick()

        assertThat(retryCalled).isTrue()
    }

    @Test
    fun `empty state displays empty message`() {
        val emptyMessage = "No categories available"

        composeTestRule.setContent {
            MyNewsTheme {
                EmptyState(message = emptyMessage)
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("📭 $emptyMessage").assertIsDisplayed()
    }


    // ========== SCROLLING TESTS ==========

    @Test
    fun `category list is scrollable`() {
        composeTestRule.setContent {
            MyNewsTheme {
                CategoryList(
                    categories = sampleCategories,
                    onCategoryClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Health").performScrollTo()
        composeTestRule.onNodeWithText("Health").assertIsDisplayed()
    }

    @Test
    fun `all categories are visible after scrolling`() {
        composeTestRule.setContent {
            MyNewsTheme {
                CategoryList(
                    categories = sampleCategories,
                    onCategoryClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        // Scroll to bottom
        composeTestRule.onNodeWithText("Health").performScrollTo()
        composeTestRule.onNodeWithText("Health").assertIsDisplayed()

        // Scroll to top
        composeTestRule.onNodeWithText("Business").performScrollTo()
        composeTestRule.onNodeWithText("Business").assertIsDisplayed()
    }
}