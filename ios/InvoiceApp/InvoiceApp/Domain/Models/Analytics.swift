import Foundation

struct SpendingTrend: Codable {
    let period: String
    let amount: Decimal
    let transactionCount: Int
    let averageAmount: Decimal
    let change: Decimal?
}

struct VendorAnalytics: Codable {
    let vendorName: String
    let categoryName: String?
    let averageAmount: Decimal
    let transactionCount: Int
    let averageDaysBetween: Double?
    let isRecurring: Bool
}

struct PeriodComparison: Codable {
    let currentPeriod: PeriodData
    let previousPeriod: PeriodData
    let change: Decimal
    let percentageChange: Double
}

struct PeriodData: Codable {
    let startDate: Date
    let endDate: Date
    let totalAmount: Decimal
    let transactionCount: Int
    let averageAmount: Decimal
}

struct SpendingForecast: Codable {
    let forecastedAmount: Decimal
    let confidence: String
    let basedOnMonths: Int
    let trend: String
}

struct Budget: Codable, Identifiable {
    let id: UUID
    let userId: UUID
    let categoryId: UUID?
    let category: Category?
    let name: String
    let amount: Decimal
    let period: BudgetPeriod
    let startDate: Date
    let endDate: Date
    let alertThreshold: Double?
    let createdAt: Date
    let updatedAt: Date
    let spent: Decimal?
    let remaining: Decimal?
    let percentageUsed: Double?
    let isExceeded: Bool?
}

enum BudgetPeriod: String, Codable {
    case weekly = "WEEKLY"
    case monthly = "MONTHLY"
    case quarterly = "QUARTERLY"
    case yearly = "YEARLY"
    case custom = "CUSTOM"
}

struct BudgetCreateRequest: Codable {
    let categoryId: UUID?
    let name: String
    let amount: Decimal
    let period: BudgetPeriod
    let startDate: Date?
    let endDate: Date?
    let alertThreshold: Double?
}
