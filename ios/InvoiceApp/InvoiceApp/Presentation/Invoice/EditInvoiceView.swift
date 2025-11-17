import SwiftUI

struct EditInvoiceView: View {
    let invoice: Invoice
    @ObservedObject var viewModel: InvoiceViewModel
    @Environment(\.dismiss) var dismiss
    @StateObject private var categoryViewModel = CategoryViewModel()

    @State private var vendorName: String
    @State private var amount: String
    @State private var date: Date
    @State private var selectedCategory: Category?
    @State private var description: String
    @State private var invoiceNumber: String
    @State private var status: InvoiceStatus

    init(invoice: Invoice, viewModel: InvoiceViewModel) {
        self.invoice = invoice
        self.viewModel = viewModel
        _vendorName = State(initialValue: invoice.vendorName)
        _amount = State(initialValue: "\(invoice.totalAmount)")
        _date = State(initialValue: invoice.date)
        _selectedCategory = State(initialValue: invoice.category)
        _description = State(initialValue: invoice.description ?? "")
        _invoiceNumber = State(initialValue: invoice.invoiceNumber ?? "")
        _status = State(initialValue: invoice.status)
    }

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

                Section("Status") {
                    Picker("Status", selection: $status) {
                        Text("Draft").tag(InvoiceStatus.draft)
                        Text("Pending").tag(InvoiceStatus.pending)
                        Text("Processed").tag(InvoiceStatus.processed)
                        Text("Rejected").tag(InvoiceStatus.rejected)
                    }
                }

                Section("Additional Details") {
                    TextField("Invoice Number (Optional)", text: $invoiceNumber)
                    TextField("Description (Optional)", text: $description, axis: .vertical)
                        .lineLimit(3...6)
                }
            }
            .navigationTitle("Edit Invoice")
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
                            await updateInvoice()
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

    private func updateInvoice() async {
        guard let amountDecimal = Decimal(string: amount) else { return }

        let request = InvoiceUpdateRequest(
            vendorName: vendorName,
            totalAmount: amountDecimal,
            date: date,
            categoryId: selectedCategory?.id,
            description: description.isEmpty ? nil : description,
            invoiceNumber: invoiceNumber.isEmpty ? nil : invoiceNumber,
            status: status
        )

        await viewModel.updateInvoice(id: invoice.id, request: request)
        dismiss()
    }
}
