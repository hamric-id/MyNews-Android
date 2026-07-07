package com.hamric.core.network.mapper

import com.hamric.core.model.Category
import com.hamric.core.network.response.SourceResponse

fun List<SourceResponse>.toCategories(): List<Category> {
    return this.groupBy { it.category }
        .map { (category, sources) ->
            Category(
                id = category,
                name = category.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString()
                },
                sourcesCount = sources.size
            )
        }
        .sortedBy { it.name }
}