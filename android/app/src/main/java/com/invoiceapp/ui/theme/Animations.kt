package com.invoiceapp.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin

/**
 * Material 3 motion animations for Invoice App
 */

// Duration constants
object AnimationDuration {
    const val SHORT = 200
    const val MEDIUM = 300
    const val LONG = 500
}

// Easing curves
object AnimationEasing {
    val FastOutSlowIn = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val LinearOutSlowIn = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val FastOutLinearIn = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    val Standard = CubicBezierEasing(0.4f, 0.0f, 0.6f, 1.0f)
}

/**
 * Slide in from right transition
 */
@OptIn(ExperimentalAnimationApi::class)
fun slideInFromRight(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(
            durationMillis = AnimationDuration.MEDIUM,
            easing = AnimationEasing.FastOutSlowIn
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = AnimationDuration.MEDIUM
        )
    )
}

/**
 * Slide out to left transition
 */
@OptIn(ExperimentalAnimationApi::class)
fun slideOutToLeft(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(
            durationMillis = AnimationDuration.MEDIUM,
            easing = AnimationEasing.FastOutSlowIn
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = AnimationDuration.MEDIUM
        )
    )
}

/**
 * Fade in transition
 */
fun fadeInTransition(): EnterTransition {
    return fadeIn(
        animationSpec = tween(
            durationMillis = AnimationDuration.MEDIUM,
            easing = AnimationEasing.Standard
        )
    )
}

/**
 * Fade out transition
 */
fun fadeOutTransition(): ExitTransition {
    return fadeOut(
        animationSpec = tween(
            durationMillis = AnimationDuration.MEDIUM,
            easing = AnimationEasing.Standard
        )
    )
}

/**
 * Scale in transition (Material 3 shared axis)
 */
@OptIn(ExperimentalAnimationApi::class)
fun scaleInTransition(): EnterTransition {
    return scaleIn(
        initialScale = 0.9f,
        transformOrigin = TransformOrigin.Center,
        animationSpec = tween(
            durationMillis = AnimationDuration.MEDIUM,
            easing = AnimationEasing.FastOutSlowIn
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = AnimationDuration.SHORT
        )
    )
}

/**
 * Scale out transition
 */
@OptIn(ExperimentalAnimationApi::class)
fun scaleOutTransition(): ExitTransition {
    return scaleOut(
        targetScale = 0.9f,
        transformOrigin = TransformOrigin.Center,
        animationSpec = tween(
            durationMillis = AnimationDuration.MEDIUM,
            easing = AnimationEasing.FastOutSlowIn
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = AnimationDuration.SHORT
        )
    )
}

/**
 * Expand vertically transition
 */
@OptIn(ExperimentalAnimationApi::class)
fun expandVerticallyTransition(): EnterTransition {
    return expandVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    ) + fadeIn()
}

/**
 * Shrink vertically transition
 */
@OptIn(ExperimentalAnimationApi::class)
fun shrinkVerticallyTransition(): ExitTransition {
    return shrinkVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    ) + fadeOut()
}

/**
 * Spring animation spec for interactive elements
 */
fun<T> springAnimation(
    dampingRatio: Float = Spring.DampingRatioMediumBouncy,
    stiffness: Float = Spring.StiffnessMedium
): SpringSpec<T> {
    return spring(
        dampingRatio = dampingRatio,
        stiffness = stiffness
    )
}

/**
 * Tween animation spec for simple transitions
 */
fun<T> tweenAnimation(
    duration: Int = AnimationDuration.MEDIUM,
    easing: Easing = AnimationEasing.Standard
): TweenSpec<T> {
    return tween(
        durationMillis = duration,
        easing = easing
    )
}

/**
 * List item animation modifier
 */
@Composable
fun Modifier.listItemAnimation(
    visible: Boolean
): Modifier {
    return this.animateEnterExit(
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    )
}
