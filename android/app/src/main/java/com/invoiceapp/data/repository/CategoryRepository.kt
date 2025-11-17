package com.invoiceapp.data.repository

import com.invoiceapp.data.api.ApiService
import com.invoiceapp.model.Category
import com.invoiceapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val apiService: ApiService
) {

    fun getCategories(): Flow<Resource<List<Category>>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.getCategories()

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch categories"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }.flowOn(Dispatchers.IO)

    fun getCategoryById(id: UUID): Flow<Resource<Category>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.getCategoryById(id)

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch category"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }.flowOn(Dispatchers.IO)

    fun createCategory(category: Category): Flow<Resource<Category>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.createCategory(category)

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to create category"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }.flowOn(Dispatchers.IO)

    fun updateCategory(id: UUID, category: Category): Flow<Resource<Category>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.updateCategory(id, category)

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update category"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }.flowOn(Dispatchers.IO)

    fun deleteCategory(id: UUID): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.deleteCategory(id)

            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to delete category"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }.flowOn(Dispatchers.IO)
}
