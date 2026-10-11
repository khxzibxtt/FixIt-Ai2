package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val title: String, val subtitle: String) {
    AURORA("Aurora Glass", "Luminous violet & frosted glow"),
    LIGHT("Pearl Light Glass", "Crisp white frosted glass"),
    DARK("Obsidian Glass", "Deep graphite with gold & neon")
}

data class GlassStyle(
    val cardBackground: Color,
    val cardBorder: Brush,
    val isDark: Boolean,
    val backgroundBrush: Brush,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val surfaceHighlight: Color
)

val LocalGlassStyle = staticCompositionLocalOf {
    GlassStyle(
        cardBackground = AuroraGlassCard,
        cardBorder = GlassBorderGradientDark,
        isDark = true,
        backgroundBrush = AuroraMeshGradient,
        accentPrimary = FixItViolet,
        accentSecondary = FixItCyan,
        textPrimary = Color.White,
        textSecondary = Color(0xFF94A3B8),
        surfaceHighlight = Color.White.copy(alpha = 0.10f)
    )
}

private val AuroraColorScheme = darkColorScheme(
    primary = FixItViolet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4C1D95),
    onPrimaryContainer = Color.White,
    secondary = FixItCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = Color.White,
    tertiary = FixItAmber,
    onTertiary = Color.Black,
    background = AuroraBgDeep,
    onBackground = Color(0xFFF8FAFC),
    surface = AuroraBgMid,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E1B4B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = FixItError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = FixItPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = FixItAmber,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = FixItEmerald,
    onTertiary = Color.White,
    background = LightGlassBg,
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    error = FixItError,
    onError = Color.White
)

private val ObsidianColorScheme = darkColorScheme(
    primary = FixItAmber,
    onPrimary = Color.Black,
    primaryContainer = FixItAmberDark,
    onPrimaryContainer = Color.White,
    secondary = FixItEmerald,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = Color.White,
    tertiary = FixItCyan,
    onTertiary = Color.Black,
    background = ObsidianBg,
    onBackground = Color(0xFFF1F5F9),
    surface = ObsidianSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = FixItError,
    onError = Color.White
)

@Composable
fun FixItTheme(
    themeMode: AppThemeMode = AppThemeMode.AURORA,
    content: @Composable () -> Unit,
) {
    val (colorScheme, glassStyle) = when (themeMode) {
        AppThemeMode.AURORA -> Pair(
            AuroraColorScheme,
            GlassStyle(
                cardBackground = Color(0xFF1B1B3A).copy(alpha = 0.58f),
                cardBorder = GlassBorderGradientDark,
                isDark = true,
                backgroundBrush = Brush.verticalGradient(
                    listOf(Color(0xFF090B22), Color(0xFF111132), Color(0xFF0B1426))
                ),
                accentPrimary = FixItViolet,
                accentSecondary = FixItCyan,
                textPrimary = Color.White,
                textSecondary = Color(0xFF94A3B8),
                surfaceHighlight = Color.White.copy(alpha = 0.12f)
            )
        )
        AppThemeMode.LIGHT -> Pair(
            LightColorScheme,
            GlassStyle(
                cardBackground = Color.White.copy(alpha = 0.82f),
                cardBorder = GlassBorderGradientLight,
                isDark = false,
                backgroundBrush = Brush.verticalGradient(
                    listOf(Color(0xFFF8FAFC), Color(0xFFEEF2FF), Color(0xFFF1F5F9))
                ),
                accentPrimary = FixItPrimaryBlue,
                accentSecondary = FixItAmber,
                textPrimary = Color(0xFF0F172A),
                textSecondary = Color(0xFF64748B),
                surfaceHighlight = Color.White.copy(alpha = 0.95f)
            )
        )
        AppThemeMode.DARK -> Pair(
            ObsidianColorScheme,
            GlassStyle(
                cardBackground = Color(0xFF131825).copy(alpha = 0.65f),
                cardBorder = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.04f))
                ),
                isDark = true,
                backgroundBrush = Brush.verticalGradient(
                    listOf(Color(0xFF090D16), Color(0xFF0E131F), Color(0xFF090D16))
                ),
                accentPrimary = FixItAmber,
                accentSecondary = FixItEmerald,
                textPrimary = Color(0xFFF1F5F9),
                textSecondary = Color(0xFF94A3B8),
                surfaceHighlight = Color.White.copy(alpha = 0.08f)
            )
        )
    }

    CompositionLocalProvider(LocalGlassStyle provides glassStyle) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = FixItTheme(themeMode = if (darkTheme) AppThemeMode.AURORA else AppThemeMode.LIGHT, content = content)
