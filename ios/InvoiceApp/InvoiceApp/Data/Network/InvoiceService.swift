import Foundation

class InvoiceService {
    private let apiClient = APIClient.shared

    func getInvoices(page: Int = 0, size: Int = 20) async throws -> PageResponse<Invoice> {
        return try await apiClient.request(
            endpoint: "/invoices?page=\(page)&size=\(size)&sort=date,desc"
        )
    }

    func getInvoice(id: UUID) async throws -> Invoice {
        return try await apiClient.request(endpoint: "/invoices/\(id.uuidString)")
    }

    func createInvoice(request: InvoiceCreateRequest) async throws -> Invoice {
        return try await apiClient.request(
            endpoint: "/invoices",
            method: .post,
            body: request
        )
    }

    func updateInvoice(id: UUID, request: InvoiceUpdateRequest) async throws -> Invoice {
        return try await apiClient.request(
            endpoint: "/invoices/\(id.uuidString)",
            method: .put,
            body: request
        )
    }

    func deleteInvoice(id: UUID) async throws {
        let _: EmptyResponse = try await apiClient.request(
            endpoint: "/invoices/\(id.uuidString)",
            method: .delete
        )
    }

    func uploadInvoice(imageData: Data) async throws -> Invoice {
        return try await apiClient.uploadImage(
            endpoint: "/invoices/upload",
            imageData: imageData
        )
    }

    func searchInvoices(query: String, page: Int = 0, size: Int = 20) async throws -> PageResponse<Invoice> {
        let encodedQuery = query.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        return try await apiClient.request(
            endpoint: "/invoices/search?query=\(encodedQuery)&page=\(page)&size=\(size)"
        )
    }

    func getInvoicesByCategory(categoryId: UUID, page: Int = 0, size: Int = 20) async throws -> PageResponse<Invoice> {
        return try await apiClient.request(
            endpoint: "/invoices/category/\(categoryId.uuidString)?page=\(page)&size=\(size)"
        )
    }

    func getInvoicesByDateRange(startDate: Date, endDate: Date) async throws -> [Invoice] {
        let formatter = ISO8601DateFormatter()
        let start = formatter.string(from: startDate)
        let end = formatter.string(from: endDate)
        return try await apiClient.request(
            endpoint: "/invoices/date-range?startDate=\(start)&endDate=\(end)"
        )
    }
}
