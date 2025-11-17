import Foundation
import SwiftUI

@MainActor
class CategoryViewModel: ObservableObject {
    @Published var categories: [Category] = []
    @Published var statistics: [CategoryStatistics] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let categoryService = CategoryService()

    func loadCategories() async {
        isLoading = true
        errorMessage = nil

        do {
            categories = try await categoryService.getCategories()
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func createCategory(request: CategoryCreateRequest) async {
        isLoading = true
        errorMessage = nil

        do {
            let category = try await categoryService.createCategory(request: request)
            categories.append(category)
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func updateCategory(id: UUID, request: CategoryUpdateRequest) async {
        isLoading = true
        errorMessage = nil

        do {
            let updated = try await categoryService.updateCategory(id: id, request: request)
            if let index = categories.firstIndex(where: { $0.id == id }) {
                categories[index] = updated
            }
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func deleteCategory(id: UUID) async {
        isLoading = true
        errorMessage = nil

        do {
            try await categoryService.deleteCategory(id: id)
            categories.removeAll { $0.id == id }
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func loadStatistics(startDate: Date, endDate: Date) async {
        isLoading = true
        errorMessage = nil

        do {
            statistics = try await categoryService.getCategoryStatistics(startDate: startDate, endDate: endDate)
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }
}
