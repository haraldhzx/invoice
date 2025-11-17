import SwiftUI

struct CategoryListView: View {
    @StateObject private var viewModel = CategoryViewModel()
    @State private var showingAddCategory = false

    var body: some View {
        NavigationStack {
            List {
                ForEach(viewModel.categories) { category in
                    CategoryRowView(category: category)
                        .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                            if !category.isDefault {
                                Button(role: .destructive) {
                                    Task {
                                        await viewModel.deleteCategory(id: category.id)
                                    }
                                } label: {
                                    Label("Delete", systemImage: "trash")
                                }
                            }
                        }
                }
            }
            .navigationTitle("Categories")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { showingAddCategory = true }) {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showingAddCategory) {
                AddCategoryView(viewModel: viewModel)
            }
            .refreshable {
                await viewModel.loadCategories()
            }
            .task {
                if viewModel.categories.isEmpty {
                    await viewModel.loadCategories()
                }
            }
        }
    }
}

struct CategoryRowView: View {
    let category: Category

    var body: some View {
        HStack(spacing: 12) {
            if let icon = category.icon {
                Image(systemName: icon)
                    .font(.title2)
                    .foregroundColor(colorFromString(category.color))
                    .frame(width: 40, height: 40)
                    .background(colorFromString(category.color).opacity(0.2))
                    .cornerRadius(8)
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(category.name)
                    .font(.headline)

                if let description = category.description {
                    Text(description)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }

                if category.isDefault {
                    Text("Default Category")
                        .font(.caption2)
                        .foregroundColor(.blue)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.blue.opacity(0.1))
                        .cornerRadius(4)
                }
            }

            Spacer()
        }
        .padding(.vertical, 4)
    }

    private func colorFromString(_ colorString: String?) -> Color {
        guard let colorString = colorString else { return .blue }
        switch colorString.lowercased() {
        case "red": return .red
        case "blue": return .blue
        case "green": return .green
        case "orange": return .orange
        case "purple": return .purple
        case "pink": return .pink
        case "yellow": return .yellow
        default: return .blue
        }
    }
}

#Preview {
    CategoryListView()
}
