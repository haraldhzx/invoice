import Foundation
import SwiftUI

@MainActor
class MCPViewModel: ObservableObject {
    @Published var queryHistory: [MCPQueryResponse] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let mcpService = MCPService()

    func askQuestion(_ question: String) async {
        isLoading = true
        errorMessage = nil

        do {
            let response = try await mcpService.query(question: question)
            queryHistory.insert(response, at: 0)
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func loadHistory() async {
        isLoading = true
        errorMessage = nil

        do {
            queryHistory = try await mcpService.getHistory()
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }
}
