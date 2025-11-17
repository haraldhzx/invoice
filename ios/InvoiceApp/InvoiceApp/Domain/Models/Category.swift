import Foundation

struct Category: Codable, Identifiable, Hashable {
    let id: UUID
    let name: String
    let description: String?
    let icon: String?
    let color: String?
    let isDefault: Bool
    let userId: UUID?
    let createdAt: Date
    let updatedAt: Date

    enum CodingKeys: String, CodingKey {
        case id, name, description, icon, color, isDefault, userId, createdAt, updatedAt
    }
}

struct CategoryCreateRequest: Codable {
    let name: String
    let description: String?
    let icon: String?
    let color: String?
}

struct CategoryUpdateRequest: Codable {
    let name: String?
    let description: String?
    let icon: String?
    let color: String?
}

struct CategoryStatistics: Codable {
    let categoryId: UUID
    let categoryName: String
    let totalAmount: Decimal
    let invoiceCount: Int
    let percentage: Double
}
