import SwiftUI

/**
 * Animation utilities for Invoice App
 */

// MARK: - Animation Constants
enum AnimationDuration {
    static let short: Double = 0.2
    static let medium: Double = 0.3
    static let long: Double = 0.5
}

// MARK: - Spring Animations
extension Animation {
    /// Smooth spring animation for interactive elements
    static let smooth = Animation.spring(response: 0.3, dampingFraction: 0.7, blendDuration: 0)

    /// Bouncy spring animation for emphasis
    static let bouncy = Animation.spring(response: 0.4, dampingFraction: 0.6, blendDuration: 0)

    /// Snappy spring animation for quick interactions
    static let snappy = Animation.spring(response: 0.25, dampingFraction: 0.8, blendDuration: 0)

    /// Gentle spring animation for subtle effects
    static let gentle = Animation.spring(response: 0.4, dampingFraction: 0.9, blendDuration: 0)
}

// MARK: - View Modifiers

/// Fade in/out animation modifier
struct FadeModifier: ViewModifier {
    let isVisible: Bool

    func body(content: Content) -> some View {
        content
            .opacity(isVisible ? 1 : 0)
            .animation(.smooth, value: isVisible)
    }
}

/// Scale animation modifier
struct ScaleModifier: ViewModifier {
    let isPressed: Bool

    func body(content: Content) -> some View {
        content
            .scaleEffect(isPressed ? 0.95 : 1.0)
            .animation(.snappy, value: isPressed)
    }
}

/// Slide transition modifier
struct SlideModifier: ViewModifier {
    let edge: Edge
    let isVisible: Bool

    func body(content: Content) -> some View {
        content
            .transition(.move(edge: edge).combined(with: .opacity))
    }
}

/// Shimmer effect for loading states
struct ShimmerModifier: ViewModifier {
    @State private var phase: CGFloat = 0

    func body(content: Content) -> some View {
        content
            .overlay(
                GeometryReader { geometry in
                    Rectangle()
                        .fill(
                            LinearGradient(
                                colors: [.clear, .white.opacity(0.3), .clear],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .frame(width: geometry.size.width)
                        .offset(x: phase * geometry.size.width * 2 - geometry.size.width)
                }
                .clipped()
            )
            .onAppear {
                withAnimation(.linear(duration: 1.5).repeatForever(autoreverses: false)) {
                    phase = 1
                }
            }
    }
}

/// Rotation animation modifier
struct RotationModifier: ViewModifier {
    let isRotating: Bool

    @State private var rotation: Double = 0

    func body(content: Content) -> some View {
        content
            .rotationEffect(.degrees(rotation))
            .onAppear {
                if isRotating {
                    withAnimation(.linear(duration: 1).repeatForever(autoreverses: false)) {
                        rotation = 360
                    }
                }
            }
    }
}

/// Card flip animation modifier
struct FlipModifier: ViewModifier {
    let isFlipped: Bool

    func body(content: Content) -> some View {
        content
            .rotation3DEffect(
                .degrees(isFlipped ? 180 : 0),
                axis: (x: 0, y: 1, z: 0),
                perspective: 0.5
            )
            .animation(.smooth, value: isFlipped)
    }
}

// MARK: - View Extensions

extension View {
    /// Apply fade animation
    func fade(isVisible: Bool) -> some View {
        modifier(FadeModifier(isVisible: isVisible))
    }

    /// Apply scale animation on press
    func scaleOnPress(isPressed: Bool) -> some View {
        modifier(ScaleModifier(isPressed: isPressed))
    }

    /// Apply slide transition
    func slideTransition(from edge: Edge, isVisible: Bool) -> some View {
        modifier(SlideModifier(edge: edge, isVisible: isVisible))
    }

    /// Apply shimmer effect for loading
    func shimmer() -> some View {
        modifier(ShimmerModifier())
    }

    /// Apply rotation animation
    func rotate(isRotating: Bool = true) -> some View {
        modifier(RotationModifier(isRotating: isRotating))
    }

    /// Apply card flip animation
    func flip(isFlipped: Bool) -> some View {
        modifier(FlipModifier(isFlipped: isFlipped))
    }

    /// Animated list item appearance
    func listItemAnimation(delay: Double = 0) -> some View {
        self
            .transition(.asymmetric(
                insertion: .opacity.combined(with: .move(edge: .trailing)),
                removal: .opacity.combined(with: .move(edge: .leading))
            ))
            .animation(.smooth.delay(delay), value: UUID())
    }

    /// Button press animation
    func buttonPressAnimation() -> some View {
        self
            .buttonStyle(ScaleButtonStyle())
    }
}

// MARK: - Button Styles

struct ScaleButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .animation(.snappy, value: configuration.isPressed)
    }
}

struct BounceButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.9 : 1.0)
            .animation(.bouncy, value: configuration.isPressed)
    }
}

// MARK: - Transitions

extension AnyTransition {
    /// Smooth scale transition
    static var smoothScale: AnyTransition {
        .scale(scale: 0.9).combined(with: .opacity)
    }

    /// Slide from trailing edge
    static var slideTrailing: AnyTransition {
        .move(edge: .trailing).combined(with: .opacity)
    }

    /// Slide from leading edge
    static var slideLeading: AnyTransition {
        .move(edge: .leading).combined(with: .opacity)
    }

    /// Pop in/out transition
    static var pop: AnyTransition {
        .scale(scale: 0.8).combined(with: .opacity)
    }
}

// MARK: - Loading View with Animation

struct LoadingView: View {
    @State private var isAnimating = false

    var body: some View {
        HStack(spacing: 8) {
            ForEach(0..<3) { index in
                Circle()
                    .fill(Color.blue)
                    .frame(width: 12, height: 12)
                    .scaleEffect(isAnimating ? 1.0 : 0.5)
                    .animation(
                        .easeInOut(duration: 0.6)
                            .repeatForever()
                            .delay(Double(index) * 0.2),
                        value: isAnimating
                    )
            }
        }
        .onAppear {
            isAnimating = true
        }
    }
}

// MARK: - Skeleton Loading View

struct SkeletonView: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            RoundedRectangle(cornerRadius: 8)
                .fill(Color.gray.opacity(0.2))
                .frame(height: 20)
                .shimmer()

            RoundedRectangle(cornerRadius: 8)
                .fill(Color.gray.opacity(0.2))
                .frame(height: 16)
                .frame(width: 200)
                .shimmer()

            RoundedRectangle(cornerRadius: 8)
                .fill(Color.gray.opacity(0.2))
                .frame(height: 16)
                .frame(width: 150)
                .shimmer()
        }
        .padding()
    }
}
