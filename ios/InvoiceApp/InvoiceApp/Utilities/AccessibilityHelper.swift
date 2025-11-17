import SwiftUI

/**
 * Accessibility utilities for Invoice App
 */

// MARK: - Accessibility Constants
enum AccessibilityConstants {
    static let minimumTouchTarget: CGFloat = 44
}

// MARK: - Accessibility Helper
struct AccessibilityHelper {
    /// Format currency for VoiceOver
    static func formatCurrencyForVoiceOver(_ amount: Decimal) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencyCode = "USD"
        let formatted = formatter.string(from: amount as NSDecimalNumber) ?? "$0.00"
        return formatted.replacingOccurrences(of: "$", with: "").appending(" dollars")
    }

    /// Format date for VoiceOver
    static func formatDateForVoiceOver(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .long
        formatter.timeStyle = .none
        return formatter.string(from: date)
    }

    /// Create invoice item accessibility label
    static func invoiceItemLabel(
        vendorName: String,
        amount: Decimal,
        date: Date,
        category: String?
    ) -> String {
        let amountText = formatCurrencyForVoiceOver(amount)
        let dateText = formatDateForVoiceOver(date)
        let categoryText = category.map { ", Category: \($0)" } ?? ""
        return "Invoice from \(vendorName), Amount: \(amountText), Date: \(dateText)\(categoryText)"
    }

    /// Create budget item accessibility label
    static func budgetItemLabel(
        name: String,
        spent: Decimal,
        total: Decimal,
        percentage: Double
    ) -> String {
        let spentText = formatCurrencyForVoiceOver(spent)
        let totalText = formatCurrencyForVoiceOver(total)
        let status: String
        if percentage >= 100 {
            status = "exceeded"
        } else if percentage >= 80 {
            status = "near limit"
        } else {
            status = "on track"
        }
        return "Budget: \(name), Spent: \(spentText) of \(totalText), \(Int(percentage)) percent used, \(status)"
    }

    /// Create category item accessibility label
    static func categoryItemLabel(
        name: String,
        description: String?,
        isDefault: Bool
    ) -> String {
        let descText = description.map { ", \($0)" } ?? ""
        let defaultText = isDefault ? ", Default category" : ""
        return "Category: \(name)\(descText)\(defaultText)"
    }

    /// Create button accessibility label with state
    static func buttonLabel(_ text: String, isLoading: Bool = false, isDisabled: Bool = false) -> String {
        if isLoading {
            return "\(text), Loading"
        } else if isDisabled {
            return "\(text), Disabled"
        }
        return text
    }
}

// MARK: - View Modifiers

struct AccessibilityLabelModifier: ViewModifier {
    let label: String
    let hint: String?
    let traits: AccessibilityTraits

    func body(content: Content) -> some View {
        content
            .accessibilityLabel(label)
            .accessibilityHint(hint ?? "")
            .accessibilityAddTraits(traits)
    }
}

struct AccessibilityHeadingModifier: ViewModifier {
    let level: Int

    func body(content: Content) -> some View {
        content
            .accessibilityAddTraits(.isHeader)
            .accessibilityLabel("Heading level \(level)")
    }
}

struct AccessibilityButtonModifier: ViewModifier {
    let label: String
    let hint: String?
    let isDisabled: Bool

    func body(content: Content) -> some View {
        content
            .accessibilityLabel(label)
            .accessibilityHint(hint ?? "")
            .accessibilityAddTraits(.isButton)
            .accessibilityRemoveTraits(isDisabled ? [] : .isButton)
            .accessibilityAddTraits(isDisabled ? .isButton : [])
    }
}

struct AccessibilityImageModifier: ViewModifier {
    let description: String?
    let isDecorative: Bool

    func body(content: Content) -> some View {
        if isDecorative {
            content
                .accessibilityHidden(true)
        } else {
            content
                .accessibilityLabel(description ?? "")
                .accessibilityAddTraits(.isImage)
        }
    }
}

struct MinimumTouchTargetModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .frame(minWidth: AccessibilityConstants.minimumTouchTarget,
                   minHeight: AccessibilityConstants.minimumTouchTarget)
    }
}

struct AccessibilityValueModifier: ViewModifier {
    let value: String
    let range: ClosedRange<Double>?
    let current: Double?

    func body(content: Content) -> some View {
        var view = content.accessibilityValue(value)

        if let range = range, let current = current {
            view = view.accessibilityAdjustableAction { direction in
                switch direction {
                case .increment:
                    // Handle increment
                    break
                case .decrement:
                    // Handle decrement
                    break
                @unknown default:
                    break
                }
            }
        }

        return view
    }
}

// MARK: - View Extensions

extension View {
    /// Add accessibility label with optional hint and traits
    func accessibilityLabel(
        _ label: String,
        hint: String? = nil,
        traits: AccessibilityTraits = []
    ) -> some View {
        modifier(AccessibilityLabelModifier(label: label, hint: hint, traits: traits))
    }

    /// Mark view as heading
    func accessibilityHeading(level: Int = 1) -> some View {
        modifier(AccessibilityHeadingModifier(level: level))
    }

    /// Add button accessibility
    func accessibilityButton(
        label: String,
        hint: String? = nil,
        isDisabled: Bool = false
    ) -> some View {
        modifier(AccessibilityButtonModifier(label: label, hint: hint, isDisabled: isDisabled))
    }

    /// Add image accessibility
    func accessibilityImage(
        description: String? = nil,
        isDecorative: Bool = false
    ) -> some View {
        modifier(AccessibilityImageModifier(description: description, isDecorative: isDecorative))
    }

    /// Ensure minimum touch target size
    func minimumTouchTarget() -> some View {
        modifier(MinimumTouchTargetModifier())
    }

    /// Add accessibility value with optional adjustable range
    func accessibilityValue(
        _ value: String,
        range: ClosedRange<Double>? = nil,
        current: Double? = nil
    ) -> some View {
        modifier(AccessibilityValueModifier(value: value, range: range, current: current))
    }

    /// Group accessibility elements
    func accessibilityGroup() -> some View {
        self.accessibilityElement(children: .combine)
    }

    /// Make view ignored by accessibility
    func accessibilityIgnored() -> some View {
        self.accessibilityHidden(true)
    }
}

// MARK: - Accessibility-Friendly Components

/// Accessible button with minimum touch target
struct AccessibleButton<Label: View>: View {
    let action: () -> Void
    let label: String
    let hint: String?
    let isDisabled: Bool
    let content: () -> Label

    init(
        _ label: String,
        hint: String? = nil,
        isDisabled: Bool = false,
        action: @escaping () -> Void,
        @ViewBuilder content: @escaping () -> Label
    ) {
        self.label = label
        self.hint = hint
        self.isDisabled = isDisabled
        self.action = action
        self.content = content
    }

    var body: some View {
        Button(action: action) {
            content()
                .frame(minWidth: AccessibilityConstants.minimumTouchTarget,
                       minHeight: AccessibilityConstants.minimumTouchTarget)
        }
        .disabled(isDisabled)
        .accessibilityLabel(label)
        .accessibilityHint(hint ?? "")
        .accessibilityAddTraits(.isButton)
    }
}

/// Accessible text field with label
struct AccessibleTextField: View {
    let label: String
    let placeholder: String
    @Binding var text: String
    let hint: String?
    let isSecure: Bool
    let keyboardType: UIKeyboardType

    init(
        _ label: String,
        placeholder: String = "",
        text: Binding<String>,
        hint: String? = nil,
        isSecure: Bool = false,
        keyboardType: UIKeyboardType = .default
    ) {
        self.label = label
        self.placeholder = placeholder
        self._text = text
        self.hint = hint
        self.isSecure = isSecure
        self.keyboardType = keyboardType
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
                .accessibilityHidden(true)

            if isSecure {
                SecureField(placeholder, text: $text)
                    .textFieldStyle(.roundedBorder)
                    .keyboardType(keyboardType)
                    .accessibilityLabel(label)
                    .accessibilityHint(hint ?? "")
            } else {
                TextField(placeholder, text: $text)
                    .textFieldStyle(.roundedBorder)
                    .keyboardType(keyboardType)
                    .accessibilityLabel(label)
                    .accessibilityHint(hint ?? "")
            }
        }
    }
}

/// Accessible progress bar
struct AccessibleProgressView: View {
    let value: Double
    let total: Double
    let label: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.caption)
                .accessibilityHidden(true)

            ProgressView(value: value, total: total)
                .accessibilityLabel("\(label): \(Int(value / total * 100)) percent")
                .accessibilityValue("\(Int(value)) of \(Int(total))")
        }
    }
}

// MARK: - Dynamic Type Support

extension Font {
    /// Scale font for accessibility
    static func scaledFont(
        _ font: Font,
        minimumScaleFactor: CGFloat = 0.5,
        maximumScaleFactor: CGFloat = 2.0
    ) -> Font {
        return font
    }
}

/// Text that supports Dynamic Type
struct ScaledText: View {
    let text: String
    let font: Font

    @ScaledMetric private var scaleFactor: CGFloat = 1.0

    var body: some View {
        Text(text)
            .font(font)
    }
}
