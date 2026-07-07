package com.hamric.feature.sources.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hamric.core.model.Source

@Composable
fun SourceItem(
    source: Source,
    onClick: (Source) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(source) },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = source.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            source.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            val countryText = source.country?.let { getCountryFlag(it) } ?: ""
            val languageText = source.language?.uppercase() ?: ""
            val infoText = buildString {
                if (countryText.isNotEmpty()) append(countryText)
                if (languageText.isNotEmpty()) {
                    if (countryText.isNotEmpty()) append(" ")
                    append(languageText)
                }
            }

            if (infoText.isNotEmpty()) {
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

fun getCountryFlag(countryCode: String): String {
    return when (countryCode.lowercase()) {
        "us" -> "🇺🇸"
        "gb" -> "🇬🇧"
        "de" -> "🇩🇪"
        "fr" -> "🇫🇷"
        "it" -> "🇮🇹"
        "es" -> "🇪🇸"
        "ca" -> "🇨🇦"
        "au" -> "🇦🇺"
        "jp" -> "🇯🇵"
        "cn" -> "🇨🇳"
        "in" -> "🇮🇳"
        "br" -> "🇧🇷"
        "ru" -> "🇷🇺"
        "za" -> "🇿🇦"
        "ng" -> "🇳🇬"
        "ae" -> "🇦🇪"
        "sa" -> "🇸🇦"
        "se" -> "🇸🇪"
        "no" -> "🇳🇴"
        "dk" -> "🇩🇰"
        "nl" -> "🇳🇱"
        "be" -> "🇧🇪"
        "ch" -> "🇨🇭"
        "at" -> "🇦🇹"
        "pl" -> "🇵🇱"
        "cz" -> "🇨🇿"
        "hu" -> "🇭🇺"
        "gr" -> "🇬🇷"
        "tr" -> "🇹🇷"
        "il" -> "🇮🇱"
        "eg" -> "🇪🇬"
        "ke" -> "🇰🇪"
        else -> "🌍"
    }
}

@Preview(
    name = "Source Item - With Description",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
private fun PreviewSourceItemWithDescription() {
    MaterialTheme {
        SourceItem(
            source = Source(
                id = "bbc-news",
                name = "BBC News",
                description = "BBC News is a British news service that provides comprehensive coverage of world events.",
                url = "https://www.bbc.com/news",
                category = "general",
                language = "en",
                country = "gb"
            ),
            onClick = {}
        )
    }
}