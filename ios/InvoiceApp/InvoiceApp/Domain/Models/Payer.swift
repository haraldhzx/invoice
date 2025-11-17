import Foundation

struct Payer: Codable, Identifiable, Hashable {
    let id: UUID
    let userId: UUID
    let name: String
    let email: String?
    let phoneNumber: String?
    let description: String?
    let color: String?
    let icon: String?
    let isDefault: Bool
    let active: Bool
    let createdAt: Date
    let updatedAt: Date

    enum CodingKeys: String, CodingKey {
        case id, userId, name, email, phoneNumber, description
        case color, icon, isDefault, active, createdAt, updatedAt
    }
}

struct PayerCreateRequest: Codable {
    let name: String
    let email: String?
    let phoneNumber: String?
    let description: String?
    let color: String?
    let icon: String?
    let isDefault: Bool?
}

struct PayerUpdateRequest: Codable {
    let name: String?
    let email: String?
    let phoneNumber: String?
    let description: String?
    let color: String?
    let icon: String?
    let isDefault: Bool?
    let active: Bool?
}
