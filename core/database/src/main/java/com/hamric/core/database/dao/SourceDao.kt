package com.hamric.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hamric.core.database.entity.SourceEntity

@Dao
interface SourceDao {

    @Query("SELECT * FROM sources WHERE category_id = :categoryId")
    suspend fun getSourcesByCategory(categoryId: String): List<SourceEntity>

    @Query("SELECT * FROM sources WHERE category_id = :categoryId ORDER BY name ASC")
    suspend fun getSourcesByCategorySorted(categoryId: String): List<SourceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSources(sources: List<SourceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: SourceEntity)

    @Query("DELETE FROM sources WHERE category_id = :categoryId")
    suspend fun clearCategory(categoryId: String)

    @Query("DELETE FROM sources")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM sources WHERE category_id = :categoryId")
    suspend fun getCountByCategory(categoryId: String): Int

    @Query("SELECT * FROM sources WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    suspend fun searchSources(query: String): List<SourceEntity>

    @Query("SELECT DISTINCT category_id FROM sources")
    suspend fun getCachedCategories(): List<String>
}