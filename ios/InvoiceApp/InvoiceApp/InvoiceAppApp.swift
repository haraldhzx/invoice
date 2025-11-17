import SwiftUI

@main
struct InvoiceAppApp: App {
    @StateObject private var appContainer = AppContainer()
    @StateObject private var authViewModel: AuthViewModel

    init() {
        let container = AppContainer()
        _appContainer = StateObject(wrappedValue: container)
        _authViewModel = StateObject(wrappedValue: container.authViewModel)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(appContainer)
                .environmentObject(authViewModel)
        }
    }
}
