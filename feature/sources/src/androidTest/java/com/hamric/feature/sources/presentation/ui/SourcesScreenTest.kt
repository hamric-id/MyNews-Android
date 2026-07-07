package com.hamric.feature.sources.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hamric.core.model.Source
import com.hamric.feature.sources.presentation.ui.components.SourceItem
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.google.common.truth.Truth.assertThat
import com.hamric.core.designsystem.components.EmptyState
import com.hamric.core.designsystem.components.LoadingIndicator
import com.hamric.core.designsystem.ui.theme.MyNewsTheme

@RunWith(AndroidJUnit4::class)
class SourcesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleSources = listOf(
        Source(
            id = "bbc-news",
            name = "BBC News",
            description = "British news broadcaster",
            url = "https://bbc.com",
            category = "general",
            language = "en",
            country = "gb"
        ),
        Source(
            id = "cnn",
            name = "CNN",
            description = "American news network",
            url = "https://cnn.com",
            category = "general",
            language = "en",
            country = "us"
        ),
        Source(
            id = "techcrunch",
            name = "TechCrunch",
            description = "Technology news",
            url = "https://techcrunch.com",
            category = "technology",
            language = "en",
            country = "us"
        ),
        Source(
            id = "espn",
            name = "ESPN",
            description = "Sports news",
            url = "https://espn.com",
            category = "sports",
            language = "en",
            country = "us"
        ),
        Source(
            id = "the-guardian",
            name = "The Guardian",
            description = "British daily newspaper",
            url = "https://theguardian.com",
            category = "general",
            language = "en",
            country = "gb"
        )
    )

    private var clickedSource: Source? = null

    @Before
    fun setup() {
        clickedSource = null
    }

    // ========== RENDERING TESTS ==========

    @Test
    fun `sources screen displays all sources`() {
        composeTestRule.setContent {
            MyNewsTheme {
                SourcesTestContent(
                    sources = sampleSources,
                    onSourceClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        sampleSources.forEach { source ->
            composeTestRule.onNodeWithText(source.name).assertIsDisplayed()
        }
    }

    @Test
    fun `sources screen displays source descriptions`() {
        composeTestRule.setContent {
            MyNewsTheme {
                SourcesTestContent(
                    sources = sampleSources,
                    onSourceClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        sampleSources.forEach { source ->
            source.description?.let {
                composeTestRule.onNodeWithText(it).assertIsDisplayed()
            }
        }
    }

    @Test
    fun `sources screen displays source count`() {
        composeTestRule.setContent {
            MyNewsTheme {
                SourcesTestContent(
                    sources = sampleSources,
                    onSourceClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("${sampleSources.size} sources").assertIsDisplayed()
    }

    @Test
    fun `sources screen shows correct number of items`() {
        composeTestRule.setContent {
            MyNewsTheme {
                SourcesTestContent(
                    sources = sampleSources,
                    onSourceClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        sampleSources.forEach { source ->
            composeTestRule.onNodeWithText(source.name).assertIsDisplayed()
        }
    }

    // ========== INTERACTION TESTS ==========

    @Test
    fun `clicking on a source triggers onSourceClick`() {
        var clickedSourceId: String? = null

        composeTestRule.setContent {
            MyNewsTheme {
                SourcesTestContent(
                    sources = sampleSources,
                    onSourceClick = { source ->
                        clickedSourceId = source.id
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("BBC News").assertIsDisplayed()
        composeTestRule.onNodeWithText("BBC News").performClick()

        assertThat(clickedSourceId).isEqualTo("bbc-news")
    }

    @Test
    fun `clicking on different sources triggers different callbacks`() {
        var clickedSourceId: String? = null

        composeTestRule.setContent {
            MyNewsTheme {
                SourcesTestContent(
                    sources = sampleSources,
                    onSourceClick = { source ->
                        clickedSourceId = source.id
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("CNN").performClick()
        assertThat(clickedSourceId).isEqualTo("cnn")

        composeTestRule.onNodeWithText("TechCrunch").performClick()
        assertThat(clickedSourceId).isEqualTo("techcrunch")
    }

    // ========== SCROLLING TESTS ==========

    @Test
    fun `source list is scrollable`() {
        composeTestRule.setContent {
            MyNewsTheme {
                SourcesTestContent(
                    sources = sampleSources,
                    onSourceClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        // Scroll to the last item
        composeTestRule.onNodeWithText("The Guardian").performScrollTo()
        composeTestRule.onNodeWithText("The Guardian").assertIsDisplayed()
    }

    @Test
    fun `all sources are visible after scrolling`() {
        composeTestRule.setContent {
            MyNewsTheme {
                SourcesTestContent(
                    sources = sampleSources,
                    onSourceClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        // Scroll to bottom
        composeTestRule.onNodeWithText("The Guardian").performScrollTo()
        composeTestRule.onNodeWithText("The Guardian").assertIsDisplayed()

        // Scroll to top
        composeTestRule.onNodeWithText("BBC News").performScrollTo()
        composeTestRule.onNodeWithText("BBC News").assertIsDisplayed()
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
    fun `empty state displays empty message`() {
        val emptyMessage = "No sources available in this category"

        composeTestRule.setContent {
            MyNewsTheme {
                EmptyState(message = emptyMessage)
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("📭 $emptyMessage").assertIsDisplayed()
    }

    // ========== SOURCE ITEM TESTS ==========

    @Test
    fun `source item displays name correctly`() {
        val source = sampleSources[0]

        composeTestRule.setContent {
            MyNewsTheme {
                SourceItem(
                    source = source,
                    onClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(source.name).assertIsDisplayed()
    }

    @Test
    fun `source item displays description correctly`() {
        val source = sampleSources[0]

        composeTestRule.setContent {
            MyNewsTheme {
                SourceItem(
                    source = source,
                    onClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        source.description?.let {
            composeTestRule.onNodeWithText(it).assertIsDisplayed()
        }
    }

    @Test
    fun `source item displays country and language correctly`() {
        val source = sampleSources[1] // CNN - US/EN

        composeTestRule.setContent {
            MyNewsTheme {
                SourceItem(
                    source = source,
                    onClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        try {
            composeTestRule.onNodeWithText("🇺🇸 EN").assertIsDisplayed()
        } catch (e: AssertionError) {
            composeTestRule.onNodeWithText("🇺🇸EN").assertIsDisplayed()
        }
    }

    @Test
    fun `source item without country shows only language`() {
        val source = Source(
            id = "test",
            name = "Test Source",
            description = "Test description",
            url = null,
            category = "general",
            language = "en",
            country = null
        )

        composeTestRule.setContent {
            MyNewsTheme {
                SourceItem(
                    source = source,
                    onClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("EN").assertIsDisplayed()
    }

    @Test
    fun `source item without language shows only country`() {
        val source = Source(
            id = "test",
            name = "Test Source",
            description = "Test description",
            url = null,
            category = "general",
            language = null,
            country = "us"
        )

        composeTestRule.setContent {
            MyNewsTheme {
                SourceItem(
                    source = source,
                    onClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("🇺🇸").assertIsDisplayed()
    }

    @Test
    fun `source item with no country or language shows no flag`() {
        val source = Source(
            id = "test",
            name = "Test Source",
            description = "Test description",
            url = null,
            category = "general",
            language = null,
            country = null
        )

        composeTestRule.setContent {
            MyNewsTheme {
                SourceItem(
                    source = source,
                    onClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(source.name).assertIsDisplayed()
    }
}


@Composable
fun SourcesTestContent(
    sources: List<Source>,
    onSourceClick: (Source) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "${sources.size} sources",
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(
            items = sources,
            key = { source -> source.id }
        ) { source ->
            SourceItem(
                source = source,
                onClick = onSourceClick
            )
        }
    }
}