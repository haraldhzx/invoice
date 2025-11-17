import SwiftUI
import Charts

struct AnalyticsView: View {
    @StateObject private var viewModel = AnalyticsViewModel()

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // Spending Trends Chart
                    if !viewModel.monthlyTrends.isEmpty {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Monthly Spending Trends")
                                .font(.headline)
                                .padding(.horizontal)

                            Chart {
                                ForEach(viewModel.monthlyTrends, id: \.period) { trend in
                                    BarMark(
                                        x: .value("Month", trend.period),
                                        y: .value("Amount", NSDecimalNumber(decimal: trend.amount).doubleValue)
                                    )
                                    .foregroundStyle(Color.blue.gradient)
                                }
                            }
                            .frame(height: 200)
                            .padding()
                            .background(Color(.systemBackground))
                            .cornerRadius(10)
                            .shadow(radius: 2)
                        }
                    }

                    // Top Vendors
                    if !viewModel.topVendors.isEmpty {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Top Vendors")
                                .font(.headline)
                                .padding(.horizontal)

                            Chart {
                                ForEach(viewModel.topVendors.prefix(5), id: \.vendorName) { vendor in
                                    BarMark(
                                        x: .value("Amount", NSDecimalNumber(decimal: vendor.averageAmount).doubleValue),
                                        y: .value("Vendor", vendor.vendorName)
                                    )
                                    .foregroundStyle(Color.green.gradient)
                                }
                            }
                            .frame(height: 200)
                            .padding()
                            .background(Color(.systemBackground))
                            .cornerRadius(10)
                            .shadow(radius: 2)
                        }
                    }

                    // Budgets
                    if !viewModel.budgets.isEmpty {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Budgets")
                                .font(.headline)
                                .padding(.horizontal)

                            ForEach(viewModel.budgets) { budget in
                                BudgetCardView(budget: budget)
                            }
                        }
                    }

                    // Forecast
                    if let forecast = viewModel.forecast {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Spending Forecast")
                                .font(.headline)
                                .padding(.horizontal)

                            VStack(alignment: .leading, spacing: 8) {
                                HStack {
                                    Text("Forecasted Amount")
                                        .foregroundColor(.secondary)
                                    Spacer()
                                    Text("$\(forecast.forecastedAmount as NSDecimalNumber, formatter: currencyFormatter)")
                                        .font(.title2)
                                        .fontWeight(.bold)
                                        .foregroundColor(.blue)
                                }

                                HStack {
                                    Text("Confidence")
                                        .foregroundColor(.secondary)
                                    Spacer()
                                    Text(forecast.confidence)
                                }

                                HStack {
                                    Text("Trend")
                                        .foregroundColor(.secondary)
                                    Spacer()
                                    Text(forecast.trend)
                                        .foregroundColor(forecast.trend.lowercased().contains("increasing") ? .red : .green)
                                }
                            }
                            .padding()
                            .background(Color(.systemBackground))
                            .cornerRadius(10)
                            .shadow(radius: 2)
                        }
                    }
                }
                .padding()
            }
            .navigationTitle("Analytics")
            .refreshable {
                await viewModel.loadAllData()
            }
            .task {
                await viewModel.loadAllData()
            }
        }
    }
}

struct BudgetCardView: View {
    let budget: Budget

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text(budget.name)
                    .font(.headline)
                Spacer()
                if let isExceeded = budget.isExceeded, isExceeded {
                    Text("EXCEEDED")
                        .font(.caption)
                        .foregroundColor(.white)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color.red)
                        .cornerRadius(4)
                }
            }

            if let spent = budget.spent, let percentageUsed = budget.percentageUsed {
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text("$\(spent as NSDecimalNumber, formatter: currencyFormatter)")
                            .font(.subheadline)
                        Text("of")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Text("$\(budget.amount as NSDecimalNumber, formatter: currencyFormatter)")
                            .font(.subheadline)
                        Spacer()
                        Text("\(Int(percentageUsed))%")
                            .font(.subheadline)
                            .foregroundColor(percentageUsed > 100 ? .red : .blue)
                    }

                    ProgressView(value: min(percentageUsed, 100), total: 100)
                        .tint(progressColor(for: percentageUsed))
                }
            }

            if let category = budget.category {
                Text("Category: \(category.name)")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(10)
        .shadow(radius: 2)
    }

    private func progressColor(for percentage: Double) -> Color {
        if percentage >= 100 {
            return .red
        } else if percentage >= 80 {
            return .orange
        } else {
            return .blue
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
    AnalyticsView()
}
