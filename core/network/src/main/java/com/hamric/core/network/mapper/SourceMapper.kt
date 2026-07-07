package com.hamric.core.network.mapper

import com.hamric.core.model.Source
import com.hamric.core.network.response.SourceResponse

fun SourceResponse.toDomainModel(): Source {
    return Source(
        id = id,
        name = name,
        description = description,
        url = url,
        category = category,
        language = language,
        country = country
    )
}

fun List<SourceResponse>.toDomainModels(): List<Source> {
    return this.map { it.toDomainModel() }
}

fun List<SourceResponse>.toSourcesByCategory(categoryId: String): List<Source> {
    return this.filter { it.category == categoryId }
        .map { it.toDomainModel() }
}