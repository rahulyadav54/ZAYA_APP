# 🎨 AI ZAYA - Design & Implementation Reference Guide

## 📐 Layout Dimensions

### Splash Screen
```
┌─────────────────────────────────┐
│                                 │
│         (Top Padding 20%)       │
│                                 │
│    ┌──────────────────────┐    │
│    │   ANIMATED LOGO      │    │ ← 140dp size
│    │   (with glow effect) │    │
│    └──────────────────────┘    │
│                                 │
│       (Vertical Spacer)         │
│                                 │
│   AI ZAYA (Title)               │
│   Your Futuristic AI...         │
│   Master your subjects...       │
│                                 │
│   (Features list with emojis)   │
│                                 │
│   ●  ●  ●  (Loading dots)      │
│   Getting everything ready...  │
│                                 │
└─────────────────────────────────┘
```

### Login Screen
```
┌─────────────────────────────────┐
│                                 │
│      ┌──────────────────┐       │
│      │    LOGO          │       │ ← 130dp with glow
│      └──────────────────┘       │
│                                 │
│         AI ZAYA                 │
│    Your Futuristic...          │
│    Master your subjects        │
│                                 │
│  ┌─────────────────────────┐   │
│  │  📧 Email Address       │   │
│  └─────────────────────────┘   │
│                                 │
│  ┌─────────────────────────┐   │
│  │  🔒 Password            │   │
│  └─────────────────────────┘   │
│                                 │
│  ┌─────────────────────────┐   │
│  │     LOGIN BUTTON        │   │
│  └─────────────────────────┘   │
│                                 │
│  Don't have an account?        │
│  Register                       │
│                                 │
└─────────────────────────────────┘
```

---

## 🎨 Color Codes & Gradients

### Primary Gradient
```
Start: #9D50BB (Neon Purple)
  ↓
  ↓
End:   #00E5FF (Neon Cyan)

Usage: Logo background, buttons, accents
```

### Background Gradient
```
Top:    #0F0F1A (Dark Blue-Black)
  ↓
Middle: #1A0F2E (Dark Purple)
  ↓
Bottom: #000000 (Pure Black)

Usage: Screen backgrounds
```

### Text Colors
```
Primary:   #FFFFFF (White) - Main text
Secondary: #00E5FF (Cyan) - Links & highlights
Tertiary:  #AAAAAA (Gray) - Descriptions
```

---

## 📏 Spacing System (8dp Grid)

### Standard Spacing
```
4dp  - Minimal spacing
8dp  - Base unit
12dp - Extra tight spacing
16dp - Standard paragraph spacing
20dp - Medium spacing
24dp - Good breathing room
32dp - Section divider
40dp - Large section spacing
48dp - Extra large spacing
```

### Applied Spacing in Screens
```
Splash Screen:
- Top padding: 20% of screen
- Logo to title: 48dp
- Title to features: 32dp
- Features spacing: 12dp each
- Bottom padding: 24dp

Login Screen:
- Logo to title: 40dp
- Title to input: 48dp
- Input spacing: 16dp between
- Input to button: 40dp
- Button to link: 24dp
```

---

## 🎭 Animation Timings

### Splash Screen Animations
```
Logo Scale:     1500ms (pulse effect)
Glow Alpha:     1500ms (fade in/out)
Title Fade-in:  300ms start, 1s duration
Subtitle:       600ms start, 1s duration
Features:       900ms start, 1s duration
Dots:           Staggered 150ms delays
Auto-dismiss:   3000ms total
```

### Transition Timings
```
Splash to Login: Smooth Crossfade
Duration: ~500ms
Effect: Fade out splash, fade in login
```

---

## 📱 Responsive Design

### Device Support
```
Phones (320-540dp):
- Single column layout
- Full width elements
- Smaller padding

Tablets (540-960dp):
- Optimized for larger screens
- Proportional scaling
- Enhanced spacing

Large Tablets (960dp+):
- Centered content
- Max-width constraints
```

### Key Responsive Elements
```
Logo:
- Splash: 140dp (scales to 80% on small phones)
- Login: 130dp (scales to 75% on small phones)
- Register: 120dp

Text:
- Display: 32sp-40sp
- Headline: 20sp-24sp
- Body: 14sp-16sp
- Small: 12sp

Buttons:
- Height: 48dp minimum
- Width: match_parent - 48dp padding
- Corner radius: 16dp
```

---

## 🎯 Typography Hierarchy

### Display (App Title)
```kotlin
Material.typography.displaySmall
fontWeight = ExtraBold
letterSpacing = 4.sp
fontSize = ~32sp
color = Color.White
```

### Headline (Taglines)
```kotlin
Material.typography.titleMedium or headlineSmall
fontWeight = Light or Medium
fontSize = ~20sp
color = NeonCyan
```

### Body (Descriptions)
```kotlin
Material.typography.bodySmall or bodyMedium
fontWeight = Regular
fontSize = ~14sp
color = Color.Gray
```

### Labels (Form Fields)
```kotlin
Material.typography.bodyMedium
fontWeight = Regular
fontSize = ~14sp
color = NeonCyan (hint), White (input)
```

---

## 🖼️ Component Specifications

### Logo Component
```
Shape: Circle
Size: 130dp (login), 120dp (register), 140dp (splash)
Background: LinearGradient(Purple → Cyan)
Icon: Icons.Rounded.AutoAwesome
Icon Size: 50-60% of circle size
Glow: RadialGradient with 0.4-0.5 alpha
Ring: 1-2dp border with 0.3 alpha
```

### Input Field Component
```
Height: 56dp
Padding: 16dp horizontal
Border: 1dp, color = NeonCyan.copy(alpha=0.3f)
Corner Radius: 12dp
Icon Color: NeonCyan
Placeholder Color: Gray.copy(alpha=0.6f)
```

### Button Component
```
Height: 48dp
Width: match_parent - padding
Padding: 24dp horizontal
Background: LinearGradient(Purple → Cyan)
Corner Radius: 16dp
Text Color: White
Text Size: 16sp
Shape: RoundedCornerShape
```

### Link Component
```
Text Color: NeonCyan
Text Style: Bold
Underline: None (ripple on press)
Size: 14sp
Alignment: Center
```

---

## 🔄 Animation Implementation

### Infinite Pulsing Logo
```kotlin
infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 1.1f,
    animationSpec = infiniteRepeatable(
        animation = tween(1500, easing = EaseInOutCubic),
        repeatMode = RepeatMode.Reverse
    )
)
```

### Glow Effect
```kotlin
infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 0.8f,
    animationSpec = infiniteRepeatable(
        animation = tween(1500, easing = EaseInOutCubic),
        repeatMode = RepeatMode.Reverse
    )
)
```

### Sequential Text Animations
```kotlin
LaunchedEffect(Unit) {
    delay(300)
    titleAlpha = 1f  // Title fades in
    delay(400)
    subtitleAlpha = 1f  // Subtitle fades in
    delay(400)
    featureAlpha = 1f  // Features fade in
    delay(3000)
    onSplashComplete()  // Auto-dismiss
}
```

### Staggered Loading Dots
```kotlin
dot1: delay = 0ms
dot2: delay = 150ms
dot3: delay = 300ms

Each pulses with: tween(800ms, easing = EaseInOutCubic)
```

---

## 🎨 CSS Equivalents (for reference)

### Logo Gradient (CSS)
```css
background: linear-gradient(135deg, #9D50BB 0%, #00E5FF 100%);
border-radius: 50%;
box-shadow: 0 0 40px rgba(0, 229, 255, 0.4);
```

### Background Gradient (CSS)
```css
background: linear-gradient(180deg, 
    #0F0F1A 0%, 
    #1A0F2E 50%, 
    #000000 100%);
```

### Button (CSS)
```css
background: linear-gradient(135deg, #9D50BB 0%, #00E5FF 100%);
border-radius: 16px;
color: white;
font-weight: bold;
padding: 16px 24px;
```

---

## 📋 Implementation Checklist

### Splash Screen Elements
- [x] Background gradient (3 colors)
- [x] Logo with glow effect
- [x] Title text with letter-spacing
- [x] Subtitle (tagline)
- [x] Feature list with emoji
- [x] Feature descriptions
- [x] Loading dots animation
- [x] Auto-transition logic

### Login Screen Elements
- [x] Background gradient
- [x] Logo with glow effect (larger)
- [x] Title "AI ZAYA"
- [x] Main tagline
- [x] Sub-tagline (student-focused)
- [x] Email input field
- [x] Password input field
- [x] Login button
- [x] Register link
- [x] Proper spacing throughout

### Registration Screen Elements
- [x] Background gradient
- [x] Logo with glow effect
- [x] Title "Create Account"
- [x] Motivational tagline
- [x] Full name input
- [x] Email input
- [x] Password input
- [x] Confirm password input
- [x] Create account button
- [x] Login link

---

## 🚀 Performance Targets

### Animation Performance
```
FPS: 60fps (target)
Duration: <500ms per animation
Memory: <50MB app memory usage
Startup: <2 seconds to splash
Transition: <500ms splash to login
```

### Rendering Performance
```
Gradients: GPU accelerated
Text: Native rendering
Icons: Vector-based (no bitmaps)
Transitions: Compose optimized
```

---

## 📚 Reference Resources

### Colors
- Neon Purple: `#9D50BB`
- Neon Cyan: `#00E5FF`
- Dark Background: `#0F0F1A`

### Icons
- App Logo: `Icons.Rounded.AutoAwesome`
- Email: `Icons.Rounded.Email`
- Password: `Icons.Rounded.Lock`
- Person: `Icons.Rounded.Person`

### Material Theme
- Typography: MaterialTheme.typography
- Colors: NeonPurple, NeonCyan (custom)
- Shapes: RoundedCornerShape

---

This reference guide provides everything needed to understand, modify, or extend the AI ZAYA UI design system!

