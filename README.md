# AI ZAYA 2.0 (Zaya AI) — Futuristic AI Assistant & Learning Companion

**AI ZAYA** is a production-ready, modern AI assistant and academic learning companion for Android built with **Kotlin** and **Jetpack Compose**. Featuring an ultra-fast Cloudflare Workers serverless backend, multi-model AI routing (NVIDIA Nemotron 3 Ultra 550B via OpenRouter), full voice interaction, offline student tools, and a complete **Light Mode / Dark Mode** design system.

Developed by **ZAYA CODE HUB**.

---

## 📱 Visual Design & UI/UX Highlights

* **Modern Design System**: Royal Violet (`#7C3AED`), Deep Indigo (`#6366F1`), and Sky Cyan (`#38BDF8`) branding with high-contrast Dark Mode (`#0B0C16`) and clean Lavender-White Light Mode (`#F8F9FE`).
* **Futuristic Hero Banner**: 3D vector-rendered Assistant Robot with glowing visor, eyes, and 1-tap quick chat action.
* **Elevated Voice Assistant**: Prominent, floating center microphone action button integrated directly in the bottom navigation taskbar (`Home` | `Tools` | **`Voice`** | `History` | `Profile`).
* **3-Column Tool Grids**: Responsive 3-column tool catalog with gradient icon containers for AI Tools, Student Academic Tools, Productivity Tools, and Creative Suites.
* **Side Navigation Drawer**: User profile badge with glowing gradient avatar ring, `👑 Pro Member` indicator, and organized tool sections.
* **AI Chat Interface**: Interactive suggestion prompt chips ("💡 Study for exams", "💻 Write Python code", etc.), markdown rendering, code syntax blocks, and text-to-speech voice readout.

---

## ⚡ Architecture & Backend Infrastructure

### 1. Cloudflare Workers API Gateway (`cloudflare-worker/`)
* **Endpoint**: `https://aizaya-api.zayacodehub-aizaya.workers.dev`
* **Route**: `POST /api/chat`
* **Default AI Model**: `nvidia/nemotron-3-ultra-550b-a55b:free` via OpenRouter
* **Fallback Cascade**: Automatically falls back to high-availability free tier models if rate-limited:
  - `meta-llama/llama-3.3-70b-instruct:free`
  - `google/gemini-2.0-flash-exp:free`
* **Encoding & Headers**: Strict `Content-Type: application/json; charset=utf-8` and complete CORS handling.

### 2. Android Mobile Application (`app/`)
* **Framework**: Native Android with 100% Jetpack Compose & Material 3
* **Language**: Kotlin 2.0.21 (Compose compiler enabled)
* **SDK Compatibility**: Min SDK 25 (Android 7.1+), Target SDK 36 (Android 15 ready)
* **Local Database**: Room persistence (`AiZayaDatabase`) for offline notes, flashcards, study planner, and chat history
* **Networking**: Retrofit 2 + OkHttp 4 with UTF-8 JSON parsing
* **Voice Engine**: Real-time speech recognition via Android `SpeechRecognizer` + low-latency `TextToSpeech` playback
* **Authentication**: Firebase Authentication + Google Sign-In + Biometric app lock

---

## 🛠️ Build & Installation Guide

### Prerequisites
- Java JDK 17 or higher
- Android SDK (API 34/35/36)
- PowerShell (Windows) or Bash (macOS/Linux)
- Connected Android Device with USB Debugging enabled

### 1. Build Debug APK
From the `ZAYA_APP` root directory:
```powershell
.\gradlew.bat assembleDebug
```
* **Output APK**: `app/build/outputs/apk/debug/app-debug.apk`

### 2. Install on Device via ADB
```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 3. Launch Application
```powershell
adb shell am start -n com.zayaai.app/.MainActivity
```

---

## 📁 Repository Structure

```
ZAYA_APP/
├── app/                                    # Android Application Module
│   ├── src/main/java/com/zayaai/app/       # Kotlin Compose Screens & Components
│   │   ├── AIChatScreen.kt                 # Modern AI chat interface & suggestions
│   │   ├── HomeScreen.kt                   # 3D robot hero card & 3-column tool catalog
│   │   ├── MainActivity.kt                 # Navigation graph & elevated taskbar
│   │   ├── VoiceAssistantScreen.kt         # Real-time voice AI & tutor mode
│   │   ├── StudentToolsScreen.kt           # 3-column academic tools grid
│   │   ├── DrawerContent.kt                # Pro member side drawer
│   │   └── ui/theme/                       # Centralized Light & Dark design tokens
│   ├── src/main/res/                       # App icons, drawables, XML configs
│   ├── google-services.json                # Firebase configuration
│   └── build.gradle.kts                    # App Gradle build script
│
├── cloudflare-worker/                      # Production Cloudflare Worker Backend
│   ├── src/index.js                        # Multi-model OpenRouter gateway handler
│   ├── wrangler.toml                       # Cloudflare Worker configuration
│   └── package.json                        # Worker dependencies
│
├── backend/                                # Node.js Express server (Fallback / PPT)
│   ├── server.js                           # PPT generation & local API
│   └── package.json                        # Express dependencies
│
├── BUILD_AND_RUN.bat                       # Automated build & install script
├── BUILD_RELEASE.bat                       # Play Store release bundle builder
└── README.md                               # Project documentation
```

---

## 🔒 Security & Privacy

* **Zero Hardcoded Secrets**: Secrets and OpenRouter API keys are securely managed via Cloudflare Worker encrypted secrets (`OPENROUTER_API_KEY`).
* **Environment Protection**: Local environment files (`.env`, `*.env`) and Cloudflare cache/node artifacts are strictly excluded via `.gitignore`.
* **Biometric Lock**: Built-in biometric fingerprint / PIN security protecting user conversations and personal study notes.

---

## 📄 License & Credits

Developed with ❤️ by **ZAYA CODE HUB**.  
All rights reserved © 2026 AI ZAYA.
