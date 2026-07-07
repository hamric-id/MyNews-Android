package com.hamric.feature.articles.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hamric.core.model.Article
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ArticleItem(
    article: Article,
    onClick: (Article) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(article) },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (article.urlToImage != null) {
                AsyncImage(
                    model = article.urlToImage,
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp)
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = if (article.urlToImage != null) 12.dp else 0.dp)
                    .weight(1f)
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                article.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Text(
                    text = " • ${formatDate(article.publishedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = when {
            dateString.contains("T") -> {
                if (dateString.contains(".")) {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                } else {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                }
            }
            else -> {
                SimpleDateFormat("yyyy-MM-dd", Locale.US)
            }
        }

        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: java.util.Date())
    } catch (e: Exception) {
        dateString
    }
}

@Preview(
    name = "Article Item - With Image",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
fun PreviewArticleItemWithImage() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            ArticleItem(
                article = Article(
                    id = "1",
                    title = "Tesla Announces New Battery Technology That Could Revolutionize EVs",
                    description = "Tesla has unveiled a new battery technology that could significantly extend the range of electric vehicles while reducing costs.",
                    url = "https://example.com/tesla-battery",
                    urlToImage = "https://picsum.photos/seed/tesla/200/150",
                    publishedAt = "2026-07-08T10:30:00Z",
                    author = "John Doe",
                    sourceName = "Tech News",
                    sourceId = "tech-news"
                ),
                onClick = {  }
            )
        }
    }
}