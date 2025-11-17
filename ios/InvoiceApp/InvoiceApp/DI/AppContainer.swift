import Foundation
import SwiftUI

@MainActor
class AppContainer: ObservableObject {
    let authViewModel: AuthViewModel

    init() {
        self.authViewModel = AuthViewModel()
    }
}
