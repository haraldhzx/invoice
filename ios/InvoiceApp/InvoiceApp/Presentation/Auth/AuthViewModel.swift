import Foundation
import SwiftUI

@MainActor
class AuthViewModel: ObservableObject {
    @Published var isAuthenticated = false
    @Published var currentUser: User?
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let authService = AuthService()
    private let tokenManager = TokenManager.shared

    func checkAuthStatus() {
        isAuthenticated = tokenManager.hasValidToken()
    }

    func login(email: String, password: String) async {
        isLoading = true
        errorMessage = nil

        do {
            let response = try await authService.login(email: email, password: password)
            tokenManager.saveTokens(
                accessToken: response.accessToken,
                refreshToken: response.refreshToken
            )
            tokenManager.saveUserId(response.user.id)
            currentUser = response.user
            isAuthenticated = true
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func register(email: String, password: String) async {
        isLoading = true
        errorMessage = nil

        do {
            let response = try await authService.register(email: email, password: password)
            tokenManager.saveTokens(
                accessToken: response.accessToken,
                refreshToken: response.refreshToken
            )
            tokenManager.saveUserId(response.user.id)
            currentUser = response.user
            isAuthenticated = true
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func logout() async {
        isLoading = true

        do {
            try await authService.logout()
        } catch {
            // Ignore logout errors, clear local tokens anyway
        }

        tokenManager.clearTokens()
        currentUser = nil
        isAuthenticated = false
        isLoading = false
    }
}
