import SwiftUI

struct MainTabView: View {
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            InvoiceListView()
                .tabItem {
                    Label("Invoices", systemImage: "doc.text.fill")
                }
                .tag(0)

            AnalyticsView()
                .tabItem {
                    Label("Analytics", systemImage: "chart.bar.fill")
                }
                .tag(1)

            CategoryListView()
                .tabItem {
                    Label("Categories", systemImage: "folder.fill")
                }
                .tag(2)

            MCPQueryView()
                .tabItem {
                    Label("Ask AI", systemImage: "sparkles")
                }
                .tag(3)

            SettingsView()
                .tabItem {
                    Label("Settings", systemImage: "gearshape.fill")
                }
                .tag(4)
        }
    }
}

#Preview {
    MainTabView()
        .environmentObject(AppContainer())
}
