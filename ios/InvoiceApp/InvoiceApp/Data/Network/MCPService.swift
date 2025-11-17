import Foundation

class MCPService {
    private let apiClient = APIClient.shared

    func query(question: String) async throws -> MCPQueryResponse {
        let request = MCPQueryRequest(question: question)
        return try await apiClient.request(
            endpoint: "/mcp/query",
            method: .post,
            body: request
        )
    }

    func getHistory() async throws -> [MCPQueryResponse] {
        return try await apiClient.request(endpoint: "/mcp/history")
    }
}
