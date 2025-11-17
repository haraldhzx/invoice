import SwiftUI

struct InvoiceDetailView: View {
    let invoice: Invoice
    @StateObject private var viewModel = InvoiceViewModel()
    @State private var showingDeleteAlert = false
    @State private var showingEditSheet = false
    @Environment(\.dismiss) var dismiss

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Invoice Image
                if let imageUrl = invoice.imageUrl {
                    AsyncImage(url: URL(string: imageUrl)) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .cornerRadius(10)
                    } placeholder: {
                        ProgressView()
                            .frame(height: 200)
                    }
                }

                // Basic Information
                VStack(alignment: .leading, spacing: 12) {
                    Text("Invoice Details")
                        .font(.title2)
                        .fontWeight(.bold)

                    DetailRow(label: "Vendor", value: invoice.vendorName)
                    DetailRow(label: "Amount", value: "$\(invoice.totalAmount as NSDecimalNumber, formatter: currencyFormatter)")
                    DetailRow(label: "Date", value: invoice.date.formatted(date: .long, time: .omitted))

                    if let invoiceNumber = invoice.invoiceNumber {
                        DetailRow(label: "Invoice #", value: invoiceNumber)
                    }

                    if let category = invoice.category {
                        HStack {
                            Text("Category")
                                .foregroundColor(.secondary)
                            Spacer()
                            Label(category.name, systemImage: category.icon ?? "tag")
                                .foregroundColor(.blue)
                        }
                    }

                    if let description = invoice.description {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Description")
                                .foregroundColor(.secondary)
                            Text(description)
                        }
                    }

                    HStack {
                        Text("Status")
                            .foregroundColor(.secondary)
                        Spacer()
                        Text(invoice.status.rawValue)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 4)
                            .background(statusColor(for: invoice.status).opacity(0.2))
                            .foregroundColor(statusColor(for: invoice.status))
                            .cornerRadius(8)
                    }
                }
                .padding()
                .background(Color(.systemBackground))
                .cornerRadius(10)
                .shadow(radius: 2)

                // AI Analysis
                if let aiAnalysis = invoice.aiAnalysis {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("AI Analysis")
                            .font(.title2)
                            .fontWeight(.bold)

                        if let confidence = aiAnalysis.confidence {
                            HStack {
                                Text("Confidence")
                                    .foregroundColor(.secondary)
                                Spacer()
                                Text("\(Int(confidence * 100))%")
                                    .foregroundColor(.blue)
                            }
                        }

                        if let extractedText = aiAnalysis.extractedText, !extractedText.isEmpty {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Extracted Text")
                                    .foregroundColor(.secondary)
                                Text(extractedText)
                                    .font(.caption)
                                    .padding(8)
                                    .background(Color(.systemGray6))
                                    .cornerRadius(8)
                            }
                        }

                        if let suggestions = aiAnalysis.suggestions, !suggestions.isEmpty {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Suggestions")
                                    .foregroundColor(.secondary)
                                ForEach(suggestions, id: \.self) { suggestion in
                                    Text("• \(suggestion)")
                                        .font(.caption)
                                }
                            }
                        }
                    }
                    .padding()
                    .background(Color(.systemBackground))
                    .cornerRadius(10)
                    .shadow(radius: 2)
                }
            }
            .padding()
        }
        .navigationTitle("Invoice")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Menu {
                    Button(action: { showingEditSheet = true }) {
                        Label("Edit", systemImage: "pencil")
                    }
                    Button(role: .destructive, action: { showingDeleteAlert = true }) {
                        Label("Delete", systemImage: "trash")
                    }
                } label: {
                    Image(systemName: "ellipsis.circle")
                }
            }
        }
        .alert("Delete Invoice", isPresented: $showingDeleteAlert) {
            Button("Cancel", role: .cancel) {}
            Button("Delete", role: .destructive) {
                Task {
                    await viewModel.deleteInvoice(id: invoice.id)
                    dismiss()
                }
            }
        } message: {
            Text("Are you sure you want to delete this invoice? This action cannot be undone.")
        }
        .sheet(isPresented: $showingEditSheet) {
            EditInvoiceView(invoice: invoice, viewModel: viewModel)
        }
    }

    private func statusColor(for status: InvoiceStatus) -> Color {
        switch status {
        case .draft: return .orange
        case .pending: return .blue
        case .processed: return .green
        case .rejected: return .red
        }
    }
}

struct DetailRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .foregroundColor(.secondary)
            Spacer()
            Text(value)
        }
    }
}

private let currencyFormatter: NumberFormatter = {
    let formatter = NumberFormatter()
    formatter.numberStyle = .currency
    formatter.maximumFractionDigits = 2
    return formatter
}()

#Preview {
    NavigationStack {
        InvoiceDetailView(invoice: Invoice(
            id: UUID(),
            userId: UUID(),
            vendorName: "Acme Corp",
            totalAmount: 199.99,
            date: Date(),
            categoryId: nil,
            category: nil,
            description: "Office supplies",
            invoiceNumber: "INV-001",
            imageUrl: nil,
            aiAnalysis: nil,
            status: .processed,
            createdAt: Date(),
            updatedAt: Date()
        ))
    }
}
