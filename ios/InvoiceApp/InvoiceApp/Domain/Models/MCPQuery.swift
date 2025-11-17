import Foundation

struct MCPQueryRequest: Codable {
    let question: String
}

struct MCPQueryResponse: Codable {
    let answer: String
    let data: MCPData?
    let query: String
    let timestamp: Date
}

struct MCPData: Codable {
    let totalAmount: Decimal?
    let count: Int?
    let categoryBreakdown: [String: Decimal]?
    let topVendors: [String]?
    let dateRange: DateRange?
}

struct DateRange: Codable {
    let start: Date?
    let end: Date?
}
