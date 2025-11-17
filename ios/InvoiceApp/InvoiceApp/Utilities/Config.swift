import Foundation

enum Config {
    static let apiBaseURL: String = {
        #if DEBUG
        return "http://localhost:8080/api"
        #else
        return "https://api.invoice.com/api"
        #endif
    }()

    static let authTokenKey = "auth_token"
    static let refreshTokenKey = "refresh_token"
    static let userIdKey = "user_id"
}
