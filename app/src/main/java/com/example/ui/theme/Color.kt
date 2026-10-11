package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand accents
val FixItAmber = Color(0xFFF59E0B)
val FixItAmberDark = Color(0xFFD97706)
val FixItAmberLight = Color(0xFFFCD34D)

val FixItEmerald = Color(0xFF10B981)
val FixItGreen = Color(0xFF22C55E)
val FixItCyan = Color(0xFF06B6D4)
val FixItViolet = Color(0xFF8B5CF6)
val FixItPurple = Color(0xFFA855F7)
val FixItRose = Color(0xFFF43F5E)

val FixItBlue = Color(0xFF3B82F6)
val FixItPrimaryBlue = Color(0xFF2563EB)
val FixItPrimaryBlueDark = Color(0xFF1D4ED8)
val FixItPrimaryBlueLight = Color(0xFF60A5FA)

val FixItError = Color(0xFFEF4444)
val FixItTextMuted = Color(0xFF94A3B8)

// Glassmorphism palettes
// 1. Aurora Glass (Luminous deep cosmic violet & indigo with frosted refraction)
val AuroraBgDeep = Color(0xFF0A0E27)
val AuroraBgMid = Color(0xFF121438)
val AuroraBgLight = Color(0xFF1A1A4A)
val AuroraGlassCard = Color(0xFF202048).copy(alpha = 0.55f)
val AuroraGlassCardHighlight = Color.White.copy(alpha = 0.12f)
val AuroraBorder = Color.White.copy(alpha = 0.22f)

// 2. Light Glass (Pearl White & soft pastel frosted glass)
val LightGlassBg = Color(0xFFF0F4F8)
val LightGlassBgMid = Color(0xFFE2E8F0)
val LightGlassCard = Color.White.copy(alpha = 0.78f)
val LightGlassCardVariant = Color(0xFFF8FAFC).copy(alpha = 0.90f)
val LightGlassBorder = Color.White.copy(alpha = 0.65f)

// 3. Obsidian Dark Glass
val ObsidianBg = Color(0xFF090D16)
val ObsidianSurface = Color(0xFF131824)
val ObsidianGlassCard = Color(0xFF1A2234).copy(alpha = 0.60f)
val ObsidianBorder = Color(0xFF334155).copy(alpha = 0.40f)

// Specular reflection brushes for glass edges
val GlassBorderGradientDark = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.32f),
        Color.White.copy(alpha = 0.06f),
        Color.White.copy(alpha = 0.18f)
    )
)

val GlassBorderGradientLight = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.90f),
        Color(0xFFCBD5E1).copy(alpha = 0.50f),
        Color.White.copy(alpha = 0.80f)
    )
)

val AuroraMeshGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0F0C20),
        Color(0xFF111836),
        Color(0xFF091428)
    )
)

val LightMeshGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF8FAFC),
        Color(0xFFEEF2FF),
        Color(0xFFF1F5F9)
    )
)
