import Foundation

class CategoryService {
    private let apiClient = APIClient.shared

    func getCategories() async throws -> [Category] {
        return try await apiClient.request(endpoint: "/categories")
    }

    func getCategory(id: UUID) async throws -> Category {
        return try await apiClient.request(endpoint: "/categories/\(id.uuidString)")
    }

    func createCategory(request: CategoryCreateRequest) async throws -> Category {
        return try await apiClient.request(
            endpoint: "/categories",
            method: .post,
            body: request
        )
    }

    func updateCategory(id: UUID, request: CategoryUpdateRequest) async throws -> Category {
        return try await apiClient.request(
            endpoint: "/categories/\(id.uuidString)",
            method: .put,
            body: request
        )
    }

    func deleteCategory(id: UUID) async throws {
        let _: EmptyResponse = try await apiClient.request(
            endpoint: "/categories/\(id.uuidString)",
            method: .delete
        )
    }

    func getCategoryStatistics(startDate: Date, endDate: Date) async throws -> [CategoryStatistics] {
        let formatter = ISO8601DateFormatter()
        let start = formatter.string(from: startDate)
        let end = formatter.string(from: endDate)
        return try await apiClient.request(
            endpoint: "/categories/statistics?startDate=\(start)&endDate=\(end)"
        )
    }
}
