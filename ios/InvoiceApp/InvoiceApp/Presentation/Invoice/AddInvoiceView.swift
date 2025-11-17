import SwiftUI

struct AddInvoiceView: View {
    @ObservedObject var viewModel: InvoiceViewModel
    @Environment(\.dismiss) var dismiss
    @StateObject private var categoryViewModel = CategoryViewModel()

    @State private var vendorName = ""
    @State private var amount = ""
    @State private var date = Date()
    @State private var selectedCategory: Category?
    @State private var description = ""
    @State private var invoiceNumber = ""

    var body: some View {
        NavigationStack {
            Form {
                Section("Invoice Information") {
                    TextField("Vendor Name", text: $vendorName)
                    TextField("Amount", text: $amount)
                        .keyboardType(.decimalPad)
                    DatePicker("Date", selection: $date, displayedComponents: .date)
                }

                Section("Category") {
                    Picker("Category", selection: $selectedCategory) {
                        Text("None").tag(nil as Category?)
                        ForEach(categoryViewModel.categories) { category in
                            Text(category.name).tag(category as Category?)
                        }
                    }
                }

                Section("Additional Details") {
                    TextField("Invoice Number (Optional)", text: $invoiceNumber)
                    TextField("Description (Optional)", text: $description, axis: .vertical)
                        .lineLimit(3...6)
                }
            }
            .navigationTitle("Add Invoice")
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
                            await saveInvoice()
                        }
                    }
                    .disabled(!isValid)
                }
            }
            .task {
                await categoryViewModel.loadCategories()
            }
        }
    }

    private var isValid: Bool {
        !vendorName.isEmpty && !amount.isEmpty && Decimal(string: amount) != nil
    }

    private func saveInvoice() async {
        guard let amountDecimal = Decimal(string: amount) else { return }

        let request = InvoiceCreateRequest(
            vendorName: vendorName,
            totalAmount: amountDecimal,
            date: date,
            categoryId: selectedCategory?.id,
            description: description.isEmpty ? nil : description,
            invoiceNumber: invoiceNumber.isEmpty ? nil : invoiceNumber
        )

        // Note: InvoiceViewModel needs a createInvoice method
        // For now, we'll just dismiss
        dismiss()
    }
}

#Preview {
    AddInvoiceView(viewModel: InvoiceViewModel())
}
