package com.invoiceapp.data.repository

import com.invoiceapp.data.api.ApiService
import com.invoiceapp.data.dto.AuthResponse
import com.invoiceapp.data.dto.LoginRequest
import com.invoiceapp.data.dto.LogoutRequest
import com.invoiceapp.data.dto.RefreshTokenRequest
import com.invoiceapp.data.dto.RegisterRequest
import com.invoiceapp.data.local.TokenManager
import com.invoiceapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    fun register(
        email: String,
        password: String,
        firstName: String? = null,
        lastName: String? = null
    ): Flow<Resource<AuthResponse>> = flow {
        try {
            emit(Resource.Loading())
            val request = RegisterRequest(email, password, firstName, lastName)
            val response = apiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveTokens(
                    authResponse.accessToken,
                    authResponse.refreshToken,
                    authResponse.tokenType
                )
                emit(Resource.Success(authResponse))
            } else {
                emit(Resource.Error(response.message() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred during registration"))
        }
    }.flowOn(Dispatchers.IO)

    fun login(email: String, password: String): Flow<Resource<AuthResponse>> = flow {
        try {
            emit(Resource.Loading())
            val request = LoginRequest(email, password)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveTokens(
                    authResponse.accessToken,
                    authResponse.refreshToken,
                    authResponse.tokenType
                )
                emit(Resource.Success(authResponse))
            } else {
                emit(Resource.Error(response.message() ?: "Login failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred during login"))
        }
    }.flowOn(Dispatchers.IO)

    fun logout(): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken != null) {
                val request = LogoutRequest(refreshToken)
                apiService.logout(request)
            }
            tokenManager.clearTokens()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            // Clear tokens even if API call fails
            tokenManager.clearTokens()
            emit(Resource.Success(Unit))
        }
    }.flowOn(Dispatchers.IO)

    fun refreshToken(): Flow<Resource<AuthResponse>> = flow {
        try {
            emit(Resource.Loading())
            val refreshToken = tokenManager.getRefreshToken()
                ?: throw IllegalStateException("No refresh token available")

            val request = RefreshTokenRequest(refreshToken)
            val response = apiService.refreshToken(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveTokens(
                    authResponse.accessToken,
                    authResponse.refreshToken,
                    authResponse.tokenType
                )
                emit(Resource.Success(authResponse))
            } else {
                tokenManager.clearTokens()
                emit(Resource.Error(response.message() ?: "Token refresh failed"))
            }
        } catch (e: Exception) {
            tokenManager.clearTokens()
            emit(Resource.Error(e.localizedMessage ?: "Token refresh failed"))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
}
