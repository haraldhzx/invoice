import SwiftUI

struct SettingsView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    @State private var showingLogoutAlert = false

    var body: some View {
        NavigationStack {
            List {
                Section("Account") {
                    if let user = authViewModel.currentUser {
                        LabeledContent("Email", value: user.email)
                        LabeledContent("User ID", value: user.id.uuidString)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }

                    Button(role: .destructive, action: { showingLogoutAlert = true }) {
                        Label("Logout", systemImage: "rectangle.portrait.and.arrow.right")
                    }
                }

                Section("App Information") {
                    LabeledContent("Version", value: "1.0.0")
                    LabeledContent("Build", value: "1")
                }

                Section("Support") {
                    Link(destination: URL(string: "https://invoice.com/help")!) {
                        Label("Help Center", systemImage: "questionmark.circle")
                    }
                    Link(destination: URL(string: "https://invoice.com/privacy")!) {
                        Label("Privacy Policy", systemImage: "hand.raised")
                    }
                    Link(destination: URL(string: "https://invoice.com/terms")!) {
                        Label("Terms of Service", systemImage: "doc.text")
                    }
                }
            }
            .navigationTitle("Settings")
            .alert("Logout", isPresented: $showingLogoutAlert) {
                Button("Cancel", role: .cancel) {}
                Button("Logout", role: .destructive) {
                    Task {
                        await authViewModel.logout()
                    }
                }
            } message: {
                Text("Are you sure you want to logout?")
            }
        }
    }
}

#Preview {
    SettingsView()
        .environmentObject(AppContainer().authViewModel)
}
