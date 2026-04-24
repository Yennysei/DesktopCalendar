package dev.yenny.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CalendarLightColorScheme = lightColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),

    secondary = Color(0xFF64748B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF1F2937),

    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF111827),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),

    surfaceVariant = Color(0xFFF9FAFB),
    onSurfaceVariant = Color(0xFF6B7280),

    outline = Color(0xFFE5E7EB),

    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
)

private val CalendarDarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFF94A3B8),
    onSecondary = Color(0xFF0F172A),

    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF9FAFB),

    surface = Color(0xFF111827),
    onSurface = Color(0xFFF9FAFB),

    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF9CA3AF),

    outline = Color(0xFF374151),

    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
)

@Composable
fun CalendarTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (isDarkTheme) CalendarDarkColorScheme else CalendarLightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = RobotoTypography,
        content = content,
    )
}
