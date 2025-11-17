import SwiftUI

struct InvoiceListView: View {
    @StateObject private var viewModel = InvoiceViewModel()
    @State private var searchText = ""
    @State private var showingScanner = false
    @State private var showingAddInvoice = false

    var body: some View {
        NavigationStack {
            ZStack {
                if viewModel.invoices.isEmpty && !viewModel.isLoading {
                    VStack(spacing: 20) {
                        Image(systemName: "doc.text")
                            .font(.system(size: 80))
                            .foregroundColor(.gray)
                        Text("No invoices yet")
                            .font(.title2)
                            .foregroundColor(.gray)
                        Text("Tap + to add your first invoice")
                            .font(.subheadline)
                            .foregroundColor(.gray)
                    }
                } else {
                    List {
                        ForEach(viewModel.invoices) { invoice in
                            NavigationLink(destination: InvoiceDetailView(invoice: invoice)) {
                                InvoiceRowView(invoice: invoice)
                            }
                        }

                        if viewModel.hasMorePages {
                            HStack {
                                Spacer()
                                ProgressView()
                                Spacer()
                            }
                            .onAppear {
                                Task {
                                    await viewModel.loadInvoices()
                                }
                            }
                        }
                    }
                    .refreshable {
                        await viewModel.loadInvoices(refresh: true)
                    }
                }
            }
            .navigationTitle("Invoices")
            .searchable(text: $searchText, prompt: "Search invoices")
            .onChange(of: searchText) { oldValue, newValue in
                if !newValue.isEmpty {
                    Task {
                        await viewModel.searchInvoices(query: newValue)
                    }
                } else if oldValue != newValue {
                    Task {
                        await viewModel.loadInvoices(refresh: true)
                    }
                }
            }
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button(action: { showingScanner = true }) {
                            Label("Scan Invoice", systemImage: "camera")
                        }
                        Button(action: { showingAddInvoice = true }) {
                            Label("Add Manually", systemImage: "plus.circle")
                        }
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showingScanner) {
                DocumentScannerView(viewModel: viewModel)
            }
            .sheet(isPresented: $showingAddInvoice) {
                AddInvoiceView(viewModel: viewModel)
            }
            .task {
                if viewModel.invoices.isEmpty {
                    await viewModel.loadInvoices()
                }
            }
        }
    }
}

struct InvoiceRowView: View {
    let invoice: Invoice

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(invoice.vendorName)
                    .font(.headline)
                Spacer()
                Text("$\(invoice.totalAmount as NSDecimalNumber, formatter: currencyFormatter)")
                    .font(.headline)
                    .foregroundColor(.blue)
            }

            HStack {
                if let category = invoice.category {
                    Label(category.name, systemImage: category.icon ?? "tag")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                Spacer()
                Text(invoice.date, style: .date)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            if let description = invoice.description {
                Text(description)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(2)
            }
        }
        .padding(.vertical, 4)
    }
}

private let currencyFormatter: NumberFormatter = {
    let formatter = NumberFormatter()
    formatter.numberStyle = .currency
    formatter.maximumFractionDigits = 2
    return formatter
}()

#Preview {
    InvoiceListView()
}
