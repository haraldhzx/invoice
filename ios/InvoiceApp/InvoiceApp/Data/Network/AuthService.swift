import Foundation

class AuthService {
    private let apiClient = APIClient.shared

    func login(email: String, password: String) async throws -> AuthResponse {
        let request = LoginRequest(email: email, password: password)
        return try await apiClient.request(
            endpoint: "/auth/login",
            method: .post,
            body: request,
            requiresAuth: false
        )
    }

    func register(email: String, password: String) async throws -> AuthResponse {
        let request = RegisterRequest(email: email, password: password)
        return try await apiClient.request(
            endpoint: "/auth/register",
            method: .post,
            body: request,
            requiresAuth: false
        )
    }

    func logout() async throws {
        let _: EmptyResponse = try await apiClient.request(
            endpoint: "/auth/logout",
            method: .post
        )
    }

    func refreshToken(refreshToken: String) async throws -> AuthResponse {
        let request = ["refreshToken": refreshToken]
        return try await apiClient.request(
            endpoint: "/auth/refresh",
            method: .post,
            body: request,
            requiresAuth: false
        )
    }
}

struct EmptyResponse: Codable {}
