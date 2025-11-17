import Foundation

class BudgetService {
    private let apiClient = APIClient.shared

    func getBudgets() async throws -> [Budget] {
        return try await apiClient.request(endpoint: "/budgets")
    }

    func getBudget(id: UUID) async throws -> Budget {
        return try await apiClient.request(endpoint: "/budgets/\(id.uuidString)")
    }

    func createBudget(request: BudgetCreateRequest) async throws -> Budget {
        return try await apiClient.request(
            endpoint: "/budgets",
            method: .post,
            body: request
        )
    }

    func updateBudget(id: UUID, request: BudgetCreateRequest) async throws -> Budget {
        return try await apiClient.request(
            endpoint: "/budgets/\(id.uuidString)",
            method: .put,
            body: request
        )
    }

    func deleteBudget(id: UUID) async throws {
        let _: EmptyResponse = try await apiClient.request(
            endpoint: "/budgets/\(id.uuidString)",
            method: .delete
        )
    }

    func getExceededBudgets() async throws -> [Budget] {
        return try await apiClient.request(endpoint: "/budgets/exceeded")
    }

    func getBudgetsNearingLimit(threshold: Double = 0.8) async throws -> [Budget] {
        return try await apiClient.request(
            endpoint: "/budgets/nearing-limit?threshold=\(threshold)"
        )
    }
}
