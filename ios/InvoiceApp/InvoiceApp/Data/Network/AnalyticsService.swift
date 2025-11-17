import Foundation

class AnalyticsService {
    private let apiClient = APIClient.shared

    func getMonthlyTrends(startDate: Date, endDate: Date) async throws -> [SpendingTrend] {
        let formatter = ISO8601DateFormatter()
        let start = formatter.string(from: startDate)
        let end = formatter.string(from: endDate)
        return try await apiClient.request(
            endpoint: "/analytics/trends/monthly?startDate=\(start)&endDate=\(end)"
        )
    }

    func getWeeklyTrends(startDate: Date, endDate: Date) async throws -> [SpendingTrend] {
        let formatter = ISO8601DateFormatter()
        let start = formatter.string(from: startDate)
        let end = formatter.string(from: endDate)
        return try await apiClient.request(
            endpoint: "/analytics/trends/weekly?startDate=\(start)&endDate=\(end)"
        )
    }

    func getTopVendors(limit: Int = 10) async throws -> [VendorAnalytics] {
        return try await apiClient.request(
            endpoint: "/analytics/vendors/top?limit=\(limit)"
        )
    }

    func getRecurringExpenses() async throws -> [VendorAnalytics] {
        return try await apiClient.request(
            endpoint: "/analytics/recurring-expenses"
        )
    }

    func comparePeriods(
        startDate1: Date,
        endDate1: Date,
        startDate2: Date,
        endDate2: Date
    ) async throws -> PeriodComparison {
        let formatter = ISO8601DateFormatter()
        let start1 = formatter.string(from: startDate1)
        let end1 = formatter.string(from: endDate1)
        let start2 = formatter.string(from: startDate2)
        let end2 = formatter.string(from: endDate2)
        return try await apiClient.request(
            endpoint: "/analytics/compare?startDate1=\(start1)&endDate1=\(end1)&startDate2=\(start2)&endDate2=\(end2)"
        )
    }

    func getForecast(months: Int = 3) async throws -> SpendingForecast {
        return try await apiClient.request(
            endpoint: "/analytics/forecast?months=\(months)"
        )
    }
}
