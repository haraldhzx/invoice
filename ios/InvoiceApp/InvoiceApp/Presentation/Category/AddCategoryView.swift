import SwiftUI

struct AddCategoryView: View {
    @ObservedObject var viewModel: CategoryViewModel
    @Environment(\.dismiss) var dismiss

    @State private var name = ""
    @State private var description = ""
    @State private var selectedIcon = "tag"
    @State private var selectedColor = "blue"

    private let icons = ["tag", "cart", "house", "car", "fork.knife", "heart.text.square",
                        "book", "bag", "lightbulb", "gift", "airplane", "music.note"]
    private let colors = ["blue", "red", "green", "orange", "purple", "pink", "yellow"]

    var body: some View {
        NavigationStack {
            Form {
                Section("Category Details") {
                    TextField("Name", text: $name)
                    TextField("Description (Optional)", text: $description)
                }

                Section("Icon") {
                    LazyVGrid(columns: [GridItem(.adaptive(minimum: 60))], spacing: 16) {
                        ForEach(icons, id: \.self) { icon in
                            Button(action: { selectedIcon = icon }) {
                                Image(systemName: icon)
                                    .font(.title2)
                                    .frame(width: 50, height: 50)
                                    .background(selectedIcon == icon ? Color.blue.opacity(0.2) : Color(.systemGray6))
                                    .foregroundColor(selectedIcon == icon ? .blue : .primary)
                                    .cornerRadius(8)
                            }
                        }
                    }
                    .padding(.vertical, 8)
                }

                Section("Color") {
                    LazyVGrid(columns: [GridItem(.adaptive(minimum: 60))], spacing: 16) {
                        ForEach(colors, id: \.self) { color in
                            Button(action: { selectedColor = color }) {
                                Circle()
                                    .fill(colorFromString(color))
                                    .frame(width: 40, height: 40)
                                    .overlay(
                                        Circle()
                                            .stroke(selectedColor == color ? Color.primary : Color.clear, lineWidth: 3)
                                    )
                            }
                        }
                    }
                    .padding(.vertical, 8)
                }
            }
            .navigationTitle("Add Category")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        Task {
                            await saveCategory()
                        }
                    }
                    .disabled(name.isEmpty)
                }
            }
        }
    }

    private func saveCategory() async {
        let request = CategoryCreateRequest(
            name: name,
            description: description.isEmpty ? nil : description,
            icon: selectedIcon,
            color: selectedColor
        )

        await viewModel.createCategory(request: request)
        dismiss()
    }

    private func colorFromString(_ colorString: String) -> Color {
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
    AddCategoryView(viewModel: CategoryViewModel())
}
