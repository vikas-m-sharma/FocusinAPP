package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ========================================================
// FOCUSIN CLASSIC ACADEMIC PALETTE - CANONICAL TOKENS
// ========================================================

// 1. Light Mode Canonical Tokens
val DeepNavy = Color(0xFF172033)
val SecondaryNavy = Color(0xFF24324A)
val SurfaceLight = Color(0xFFF7F5EF)
val CardLight = Color(0xFFFFFFFF)
val WarmIvory = Color(0xFFF3F0E7)
val MutedGold = Color(0xFFB08D57)
val DarkGold = Color(0xFF8F7042)
val TextPrimaryLight = Color(0xFF172033)
val TextSecondaryLight = Color(0xFF667085)
val BorderLight = Color(0xFFD9D5CB)
val SuccessLight = Color(0xFF2F6B4F)
val WarningLight = Color(0xFFB7791F)
val ErrorLight = Color(0xFFB54747)
val DisabledGray = Color(0xFFA0A5AE)

// 2. Dark Mode Canonical Tokens (Calm, High-Focus, Timeless)
val BackgroundDark = Color(0xFF101722)
val SurfaceDark = Color(0xFF172033)
val CardDark = Color(0xFF1D2939)
val ElevatedCardDark = Color(0xFF24324A)
val TextPrimaryDark = Color(0xFFF7F5EF)
val TextSecondaryDark = Color(0xFFB8C0CC)
val BorderDark = Color(0xFF344054)
val MutedGoldDark = Color(0xFFC2A36B)
val SuccessDark = Color(0xFF5B9B78)
val WarningDark = Color(0xFFD6A84F)
val ErrorDark = Color(0xFFD06B6B)

// ========================================================
// RE-MAPPED EXISTING COMPONENT COLOR ALIASES
// Replaces neon-blue/cyan with classic navy & muted gold
// ========================================================

val Slate950 = BackgroundDark       // #101722 - Canvas / Screen background
val Slate900 = SurfaceDark          // #172033 - Surfaces, Bars, Headers
val Slate850 = CardDark             // #1D2939 - Cards & Containers
val Slate800 = ElevatedCardDark     // #24324A - Elevated cards, Chips, Row items
val Slate700 = BorderDark           // #344054 - Borders & Dividers
val Slate500 = TextSecondaryLight   // #667085 - Inactive labels, Secondary text
val Slate400 = TextSecondaryDark    // #B8C0CC - Readable subtitles & hints
val Slate200 = BorderLight          // #D9D5CB - Light borders
val Slate100 = TextPrimaryDark      // #F7F5EF - Crisp academic text

// Accent Vibrants (Replaced Cyan with Muted Gold & Academic Navy)
val CyanPrimary = MutedGoldDark     // #C2A36B - Classic Muted Gold accent
val CyanPrimaryDark = MutedGold     // #B08D57 - Darker Gold for high-contrast CTA
val CyanGlow = Color(0x29C2A36B)    // Subtle non-glare gold tint
val CyanBright = WarningDark        // #D6A84F - Warm Amber Gold for highlights

// Status Colors
val EmeraldSuccess = SuccessDark    // #5B9B78 - Classic Forest Green
val EmeraldGlow = Color(0x295B9B78)
val AmberWarning = WarningDark      // #D6A84F - Academic Amber
val RoseError = ErrorDark           // #D06B6B - Muted Crimson Error

// Supporting Accents
val PurpleAccent = DarkGold         // #8F7042
val AmethystAccent = MutedGoldDark  // #C2A36B
val IndigoAccent = SecondaryNavy    // #24324A
val TealAccent = SuccessDark        // #5B9B78

// Classic Royal / Academic Theme Palette
val ClassicNavyDark = BackgroundDark
val ClassicNavySurface = SurfaceDark
val ClassicNavyCard = CardDark
val ClassicBluePrimary = DeepNavy
val ClassicBlueLight = MutedGoldDark
val ClassicGoldAccent = MutedGold
val ClassicPaperWhite = SurfaceLight

// Subject Badges & Timetable (Academic Harmony)
val SubjectBlue = SecondaryNavy     // #24324A - Physics (Deep Classic Navy)
val SubjectEmerald = SuccessDark    // #5B9B78 - Biology / Botany (Academic Forest Green)
val SubjectViolet = DarkGold        // #8F7042 - Chemistry (Antique Bronze)
val SubjectAmber = WarningDark      // #D6A84F - Zoology (Warm Gold)
val SubjectRose = ErrorDark         // #D06B6B - High-Yield / Weak Topics (Muted Rose)
val SubjectCyan = MutedGoldDark     // #C2A36B - General / Revision (Muted Gold)


