import Foundation
import SwiftUI

@MainActor
class AnalyticsViewModel: ObservableObject {
    @Published var monthlyTrends: [SpendingTrend] = []
    @Published var topVendors: [VendorAnalytics] = []
    @Published var recurringExpenses: [VendorAnalytics] = []
    @Published var forecast: SpendingForecast?
    @Published var budgets: [Budget] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let analyticsService = AnalyticsService()
    private let budgetService = BudgetService()

    func loadMonthlyTrends() async {
        isLoading = true
        errorMessage = nil

        let endDate = Date()
        let startDate = Calendar.current.date(byAdding: .month, value: -12, to: endDate) ?? endDate

        do {
            monthlyTrends = try await analyticsService.getMonthlyTrends(startDate: startDate, endDate: endDate)
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func loadTopVendors() async {
        isLoading = true
        errorMessage = nil

        do {
            topVendors = try await analyticsService.getTopVendors(limit: 10)
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func loadRecurringExpenses() async {
        isLoading = true
        errorMessage = nil

        do {
            recurringExpenses = try await analyticsService.getRecurringExpenses()
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func loadForecast() async {
        isLoading = true
        errorMessage = nil

        do {
            forecast = try await analyticsService.getForecast(months: 3)
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func loadBudgets() async {
        isLoading = true
        errorMessage = nil

        do {
            budgets = try await budgetService.getBudgets()
        } catch let error as APIError {
            errorMessage = error.errorDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func loadAllData() async {
        await loadMonthlyTrends()
        await loadTopVendors()
        await loadBudgets()
    }
}
