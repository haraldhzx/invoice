import Foundation

struct Invoice: Codable, Identifiable {
    let id: UUID
    let userId: UUID
    let vendorName: String
    let totalAmount: Decimal
    let date: Date
    let categoryId: UUID?
    let category: Category?
    let payerId: UUID?
    let payerName: String?
    let description: String?
    let invoiceNumber: String?
    let imageUrl: String?
    let aiAnalysis: AIAnalysis?
    let status: InvoiceStatus
    let createdAt: Date
    let updatedAt: Date

    enum CodingKeys: String, CodingKey {
        case id, userId, vendorName, totalAmount, date, categoryId, category
        case payerId, payerName, description, invoiceNumber, imageUrl, aiAnalysis, status, createdAt, updatedAt
    }
}

enum InvoiceStatus: String, Codable {
    case draft = "DRAFT"
    case pending = "PENDING"
    case processed = "PROCESSED"
    case rejected = "REJECTED"
}

struct AIAnalysis: Codable {
    let extractedText: String?
    let confidence: Double?
    let detectedFields: DetectedFields?
    let suggestions: [String]?
}

struct DetectedFields: Codable {
    let vendor: String?
    let amount: Decimal?
    let date: String?
    let invoiceNumber: String?
    let items: [String]?
}

struct InvoiceCreateRequest: Codable {
    let vendorName: String
    let totalAmount: Decimal
    let date: Date
    let categoryId: UUID?
    let payerId: UUID?
    let description: String?
    let invoiceNumber: String?
}

struct InvoiceUpdateRequest: Codable {
    let vendorName: String?
    let totalAmount: Decimal?
    let date: Date?
    let categoryId: UUID?
    let payerId: UUID?
    let description: String?
    let invoiceNumber: String?
    let status: InvoiceStatus?
}

struct PageResponse<T: Codable>: Codable {
    let content: [T]
    let totalPages: Int
    let totalElements: Int
    let size: Int
    let number: Int
}
