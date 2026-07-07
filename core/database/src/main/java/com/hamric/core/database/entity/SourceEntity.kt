package com.hamric.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hamric.core.model.Source

@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val url: String?,
    val category: String,
    val language: String?,
    val country: String?,
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis()
)

fun Source.toEntity(categoryId: String): SourceEntity {
    return SourceEntity(
        id = id,
        name = name,
        description = description,
        url = url,
        category = category ?: "general",
        language = language,
        country = country,
        categoryId = categoryId
    )
}

fun SourceEntity.toDomain(): Source {
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

fun List<SourceEntity>.toDomain(): List<Source> {
    return this.map { it.toDomain() }
}