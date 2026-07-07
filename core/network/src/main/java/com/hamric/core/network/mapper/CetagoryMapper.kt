package com.hamric.core.network.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.hamric.core.model.Category
import com.hamric.core.network.response.SourceResponse

fun SourceResponse.toCategory(): Category {
    return Category(
        id = category,
        name = category.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString()
        },
        headlineCount = 0
    )
}

fun List<SourceResponse>.toCategories(): List<Category> {
    return this.groupBy { it.category }
        .map { (category, sources) ->
            Category(
                id = category,
                name = category.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString()
                },
                headlineCount = sources.size
            )
        }
        .sortedBy { it.name }
}