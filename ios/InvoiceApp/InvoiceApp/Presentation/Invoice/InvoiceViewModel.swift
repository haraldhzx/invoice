import Foundation
import SwiftUI

@MainActor
class InvoiceViewModel: ObservableObject {
    @Published var invoices: [Invoice] = []
    @Published var selectedInvoice: Invoice?
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var hasMorePages = true

    private let invoiceService = InvoiceService()
    private var currentPage = 0
    private let pageSize = 20

    func loadInvoices(refresh: Bool = false) async {
        if refresh {
            currentPage = 0
            invoices = []
            hasMorePages = true
        }

        guard !isLoading && hasMorePages else { return }

        isLoading = true
        errorMessage = nil

        do {
            let response = try await invoiceService.getInvoices(page: currentPage, size: pageSize)
            if refresh {
                invoices = response.content
            } else {
                invoices.append(contentsOf: response.content)
            }
            currentPage += 1
            hasMorePages = currentPage < response.totalPages
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func loadInvoice(id: UUID) async {
        isLoading = true
        errorMessage = nil

        do {
            selectedInvoice = try await invoiceService.getInvoice(id: id)
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func uploadInvoice(imageData: Data) async -> Invoice? {
        isLoading = true
        errorMessage = nil

        do {
            let invoice = try await invoiceService.uploadInvoice(imageData: imageData)
            invoices.insert(invoice, at: 0)
            isLoading = false
            return invoice
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
        return nil
    }

    func deleteInvoice(id: UUID) async {
        isLoading = true
        errorMessage = nil

        do {
            try await invoiceService.deleteInvoice(id: id)
            invoices.removeAll { $0.id == id }
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func updateInvoice(id: UUID, request: InvoiceUpdateRequest) async {
        isLoading = true
        errorMessage = nil

        do {
            let updated = try await invoiceService.updateInvoice(id: id, request: request)
            if let index = invoices.firstIndex(where: { $0.id == id }) {
                invoices[index] = updated
            }
            selectedInvoice = updated
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func searchInvoices(query: String) async {
        isLoading = true
        errorMessage = nil
        currentPage = 0

        do {
            let response = try await invoiceService.searchInvoices(query: query, page: 0, size: pageSize)
            invoices = response.content
            hasMorePages = false
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }
}
