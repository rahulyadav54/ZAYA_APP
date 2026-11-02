package com.zayaai.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Brand Colors (Reference Image: Tools Redesign Modern Professional) ───
val PrimaryPurple = Color(0xFF7C3AED)        // Electric Violet / Royal Purple
val PrimaryPurpleDark = Color(0xFF6D28D9)
val SecondaryIndigo = Color(0xFF6366F1)
val AccentCyan = Color(0xFF38BDF8)
val AccentBlue = Color(0xFF60A5FA)
val ProGold = Color(0xFFF59E0B)              // Crown Gold

// Legacy aliases for backward compatibility
val NeonPurple = PrimaryPurple
val NeonCyan = SecondaryIndigo
val AccentGradientStart = PrimaryPurple
val AccentGradientEnd = SecondaryIndigo

// ─── Light Mode Palette (Soft White & Lavender Surfaces) ───
val LightBackground = Color(0xFFF8F9FE)      // Ultra-clean lavender-white
val LightSurface = Color(0xFFFFFFFF)         // Pure white cards
val LightSurfaceVariant = Color(0xFFF1F3FB)  // Inactive pills & subtle containers
val LightPillActive = Color(0xFF7C3AED)      // Active filter pill
val LightPillActiveText = Color(0xFFFFFFFF)
val LightPillInactive = Color(0xFFF1F4FA)
val LightPillInactiveText = Color(0xFF475569)
val LightTextPrimary = Color(0xFF0F172A)     // Deep Slate Navy
val LightTextSecondary = Color(0xFF64748B)   // Cool Slate
val LightBorder = Color(0xFFEDE9FE)          // Soft lavender border
val LightBottomBarSelected = Color(0xFFEDE9FE)
val LightBottomBarSelectedIcon = Color(0xFF7C3AED)

// ─── Dark Mode Palette (Deep Obsidian Navy & Dark Violet Cards) ───
val DarkBackground = Color(0xFF0B0C16)       // Deep Obsidian Navy
val DarkSurface = Color(0xFF141527)          // Dark purple-navy card surface
val DarkSurfaceVariant = Color(0xFF1B1D36)   // Subtle elevated containers
val DarkPillActive = Color(0xFF7C3AED)       // Active filter pill
val DarkPillActiveText = Color(0xFFFFFFFF)
val DarkPillInactive = Color(0xFF191B33)     // Inactive dark pill
val DarkPillInactiveText = Color(0xFF94A3B8)
val DarkTextPrimary = Color(0xFFFFFFFF)      // Crisp White
val DarkTextSecondary = Color(0xFF94A3B8)    // Muted Lavender Gray
val DarkBorder = Color(0xFF232544)           // Subtle purple-navy border
val DarkBottomBarSelected = Color(0xFF271F47)
val DarkBottomBarSelectedIcon = Color(0xFFA78BFA)

// Material 3 defaults
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
val LightPrimary = PrimaryPurple
val TextPrimary = DarkTextPrimary
val TextSecondary = DarkTextSecondary
val SurfaceDark = DarkSurface
