package com.invoiceapp.data.repository

import com.invoiceapp.data.api.ApiService
import com.invoiceapp.data.api.McpQueryRequest
import com.invoiceapp.data.api.McpQueryResponse
import com.invoiceapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueryRepository @Inject constructor(
    private val apiService: ApiService
) {

    fun submitQuery(request: McpQueryRequest): Flow<Resource<McpQueryResponse>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.queryMcp(request)

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Query failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }.flowOn(Dispatchers.IO)

    fun getExamples(): Flow<Resource<List<String>>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.getMcpExamples()

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch examples"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }.flowOn(Dispatchers.IO)
}
