import SwiftUI

struct MCPQueryView: View {
    @StateObject private var viewModel = MCPViewModel()
    @State private var questionText = ""
    @FocusState private var isInputFocused: Bool

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Chat History
                ScrollView {
                    LazyVStack(spacing: 16) {
                        if viewModel.queryHistory.isEmpty && !viewModel.isLoading {
                            VStack(spacing: 20) {
                                Image(systemName: "sparkles")
                                    .font(.system(size: 60))
                                    .foregroundColor(.blue)
                                Text("Ask AI About Your Spending")
                                    .font(.title2)
                                    .fontWeight(.bold)
                                Text("Ask questions like:\n• How much did I spend this month?\n• What's my biggest expense?\n• Show spending by category")
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                                    .multilineTextAlignment(.center)
                            }
                            .padding()
                            .padding(.top, 60)
                        } else {
                            ForEach(viewModel.queryHistory, id: \.timestamp) { query in
                                QueryBubbleView(query: query)
                            }
                        }
                    }
                    .padding()
                }

                // Input Area
                VStack(spacing: 0) {
                    Divider()

                    if let errorMessage = viewModel.errorMessage {
                        Text(errorMessage)
                            .font(.caption)
                            .foregroundColor(.red)
                            .padding(.horizontal)
                            .padding(.top, 8)
                    }

                    HStack(spacing: 12) {
                        TextField("Ask a question...", text: $questionText, axis: .vertical)
                            .textFieldStyle(.roundedBorder)
                            .focused($isInputFocused)
                            .lineLimit(1...4)

                        Button(action: {
                            let question = questionText
                            questionText = ""
                            isInputFocused = false
                            Task {
                                await viewModel.askQuestion(question)
                            }
                        }) {
                            if viewModel.isLoading {
                                ProgressView()
                                    .frame(width: 24, height: 24)
                            } else {
                                Image(systemName: "arrow.up.circle.fill")
                                    .font(.title2)
                                    .foregroundColor(questionText.isEmpty ? .gray : .blue)
                            }
                        }
                        .disabled(questionText.isEmpty || viewModel.isLoading)
                    }
                    .padding()
                }
                .background(Color(.systemBackground))
            }
            .navigationTitle("Ask AI")
            .task {
                if viewModel.queryHistory.isEmpty {
                    await viewModel.loadHistory()
                }
            }
        }
    }
}

struct QueryBubbleView: View {
    let query: MCPQueryResponse

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Question
            HStack {
                Spacer()
                Text(query.query)
                    .padding(12)
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(16)
                    .frame(maxWidth: .infinity * 0.75, alignment: .trailing)
            }

            // Answer
            VStack(alignment: .leading, spacing: 8) {
                Text(query.answer)
                    .padding(12)
                    .background(Color(.systemGray6))
                    .cornerRadius(16)
                    .frame(maxWidth: .infinity * 0.85, alignment: .leading)

                // Data Summary
                if let data = query.data {
                    VStack(alignment: .leading, spacing: 8) {
                        if let totalAmount = data.totalAmount {
                            HStack {
                                Text("Total Amount:")
                                    .foregroundColor(.secondary)
                                Spacer()
                                Text("$\(totalAmount as NSDecimalNumber, formatter: currencyFormatter)")
                                    .fontWeight(.semibold)
                                    .foregroundColor(.blue)
                            }
                        }

                        if let count = data.count {
                            HStack {
                                Text("Transaction Count:")
                                    .foregroundColor(.secondary)
                                Spacer()
                                Text("\(count)")
                                    .fontWeight(.semibold)
                            }
                        }

                        if let categoryBreakdown = data.categoryBreakdown, !categoryBreakdown.isEmpty {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("By Category:")
                                    .foregroundColor(.secondary)
                                    .font(.subheadline)

                                ForEach(Array(categoryBreakdown.keys.sorted()), id: \.self) { category in
                                    if let amount = categoryBreakdown[category] {
                                        HStack {
                                            Text("• \(category)")
                                                .font(.caption)
                                            Spacer()
                                            Text("$\(amount as NSDecimalNumber, formatter: currencyFormatter)")
                                                .font(.caption)
                                                .foregroundColor(.blue)
                                        }
                                    }
                                }
                            }
                        }

                        if let topVendors = data.topVendors, !topVendors.isEmpty {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Top Vendors:")
                                    .foregroundColor(.secondary)
                                    .font(.subheadline)

                                ForEach(topVendors, id: \.self) { vendor in
                                    Text("• \(vendor)")
                                        .font(.caption)
                                }
                            }
                        }
                    }
                    .padding(12)
                    .background(Color(.systemGray6))
                    .cornerRadius(12)
                }

                Text(query.timestamp, style: .relative)
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }
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
    MCPQueryView()
}
