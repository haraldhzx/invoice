import Foundation

class PayerService {
    private let apiClient = APIClient.shared

    func getPayers() async throws -> [Payer] {
        return try await apiClient.request(endpoint: "/payers")
    }

    func getPayer(id: UUID) async throws -> Payer {
        return try await apiClient.request(endpoint: "/payers/\(id.uuidString)")
    }

    func createPayer(request: PayerCreateRequest) async throws -> Payer {
        return try await apiClient.request(
            endpoint: "/payers",
            method: .post,
            body: request
        )
    }

    func updatePayer(id: UUID, request: PayerUpdateRequest) async throws -> Payer {
        return try await apiClient.request(
            endpoint: "/payers/\(id.uuidString)",
            method: .put,
            body: request
        )
    }

    func deletePayer(id: UUID) async throws {
        let _: EmptyResponse = try await apiClient.request(
            endpoint: "/payers/\(id.uuidString)",
            method: .delete
        )
    }

    func getDefaultPayer() async throws -> Payer? {
        do {
            return try await apiClient.request(endpoint: "/payers/default")
        } catch {
            // Return nil if no default payer exists (204 No Content)
            return nil
        }
    }
}
