package com.hamric.feature.categories.data.repository


import com.hamric.core.model.Category
import com.hamric.core.network.api.NewsApi
import com.hamric.core.network.mapper.toCategories
import com.hamric.feature.categories.domain.repository.CategoryRepository
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val api: NewsApi
) : CategoryRepository {

    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = api.getSources()

            if (response.status == "ok") {
                val categories = response.sources.toCategories()
                Result.success(categories)
            } else {
                val errorMessage = when {
                    response.message != null -> response.message
                    else -> "Failed to fetch categories"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is UnknownHostException -> "No internet connection. Please check your network."
                is SocketTimeoutException -> "Connection timeout. Please try again."
                is IOException -> "Network error. Please check your connection."
                else -> e.message ?: "Failed to load categories"
            }
            Result.failure(Exception(errorMessage))
        }
    }
}