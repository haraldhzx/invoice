import Foundation

class TokenManager {
    static let shared = TokenManager()

    private let userDefaults = UserDefaults.standard

    private init() {}

    func saveTokens(accessToken: String, refreshToken: String) {
        userDefaults.set(accessToken, forKey: Config.authTokenKey)
        userDefaults.set(refreshToken, forKey: Config.refreshTokenKey)
    }

    func getAccessToken() -> String? {
        return userDefaults.string(forKey: Config.authTokenKey)
    }

    func getRefreshToken() -> String? {
        return userDefaults.string(forKey: Config.refreshTokenKey)
    }

    func saveUserId(_ userId: UUID) {
        userDefaults.set(userId.uuidString, forKey: Config.userIdKey)
    }

    func getUserId() -> UUID? {
        guard let uuidString = userDefaults.string(forKey: Config.userIdKey) else {
            return nil
        }
        return UUID(uuidString: uuidString)
    }

    func clearTokens() {
        userDefaults.removeObject(forKey: Config.authTokenKey)
        userDefaults.removeObject(forKey: Config.refreshTokenKey)
        userDefaults.removeObject(forKey: Config.userIdKey)
    }

    func hasValidToken() -> Bool {
        return getAccessToken() != nil
    }
}
