# 🚀 AI ZAYA 2.0 — NVIDIA Nemotron 3 Ultra Integration

## 📋 Executive Overview

AI ZAYA has been upgraded with **NVIDIA Nemotron 3 Ultra** (`nvidia/nemotron-3-ultra-550b-a55b`) as its primary intelligence engine via OpenRouter. The integration features a secure server-side AI Gateway, real-time Server-Sent Events (SSE) streaming, reasoning-token isolation (internal reasoning tokens are never exposed to users), multi-turn context preservation, and resilient automatic fallback to Groq and Gemini.

---

## 🏗️ Architecture & Security Model

```
   ┌─────────────────────────────────────────────────────────────────┐
   │                       Android Mobile App                        │
   │           (Jetpack Compose UI + SSE Stream Consumer)            │
   └────────────────────────────────┬────────────────────────────────┘
                                    │  No secrets in client
                                    ▼  Internal loopback or LAN IP
   ┌─────────────────────────────────────────────────────────────────┐
   │                    AI ZAYA Gateway (Node.js)                    │
   │               (server.js + aiGateway.js on :3000)               │
   ├─────────────────────────────────────────────────────────────────┤
   │  • Rate Limiting & User Token Validation                        │
   │  • Server-Side Secrets (.env)                                   │
   │  • Reasoning Token Stripping (<think>...</think>)               │
   │  • Provider Health & Usage Monitoring                           │
   └───────┬────────────────────────┬────────────────────────┬───────┘
           │ Primary                │ Fallback 1             │ Fallback 2
           ▼                        ▼                        ▼
  ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
  │    OpenRouter    │    │       Groq       │    │  Google Gemini   │
  │ Nemotron 3 Ultra │    │  Llama 3.3 70B   │    │  Gemini 1.5/2.0  │
  │    550B MoE      │    │  (Fast Fallback) │    │  (Cloud Backup)  │
  └──────────────────┘    └──────────────────┘    └──────────────────┘
```

---

## 🔑 Environment Variables Required

Store keys in `ZAYA_APP/backend/.env` (**never commit this file to public repositories**):

```properties
# Server Port
PORT=3000

# OpenRouter Configuration
OPENROUTER_API_KEY=sk-or-v1-your-actual-key-here
NEMOTRON_MODEL_ID=nvidia/nemotron-3-ultra-550b-a55b
NEMOTRON_FALLBACK_MODEL_ID=nvidia/nemotron-3-ultra-550b-a55b:free

# Optional Fallback Providers
GROQ_API_KEY=gsk_your_groq_key_if_desired
GEMINI_API_KEY=AIzaSy_your_gemini_key_if_desired

# Rate Limiting & Security
RATE_LIMIT_DAILY=80
RATE_LIMIT_PER_MINUTE=15
```

---

## 📡 API Gateway Endpoints

| Endpoint | Method | Description |
|---|---|---|
| `/api/chat/stream` | `POST` | Real-time Server-Sent Events (SSE) streaming with Nemotron 3 Ultra |
| `/api/chat` | `POST` | Non-streaming conversation completion with automatic provider fallback |
| `/api/models` | `GET` | List of available models, descriptions, and primary model identification |
| `/api/health` | `GET` | Gateway health status, configured provider statuses, and server uptime |
| `/api/text-generate` | `POST` | Specialized writing assistant route |
| `/api/code-generate` | `POST` | Code synthesis with markdown language tagging |
| `/api/quiz-generate` | `POST` | Automated multiple choice quiz creation |
| `/api/ppt-create` | `POST` | PowerPoint presentation creation engine |

---

## 📱 Mobile UI & Chat Upgrades

1. **Model Selector**:
   - Dynamic model selector pill in the chat header allowing users to select between:
     - **NVIDIA Nemotron 3 Ultra** (Primary frontier reasoning)
     - **NVIDIA Nemotron 3 Ultra (Free)** (OpenRouter trial endpoint)
     - **Groq Llama 3.3 70B** (Ultra-fast fallback)
2. **Animated Thinking Indicator**:
   - Detects internal reasoning mode and displays an animated card (`Nemotron 3 Ultra is thinking...`) with pulsing brain and glowing cyan gradient border.
   - Internal reasoning tokens (`<think>...</think>`) are filtered out cleanly so only the final assistant answer is presented.
3. **Real-Time Streaming Typing**:
   - Chunks stream into the message bubble in real-time.
4. **Stop Generation Button**:
   - Transforms into an illuminated square stop button while generating. Tapping halts the stream immediately and preserves the text generated so far.
5. **Regenerate & Copy**:
   - Quick one-tap copy with toast notification.
   - One-tap response regeneration.
6. **Chat History**:
   - Access saved conversations, switch chats, delete conversations, or start new conversations.
7. **Error & Retry States**:
   - Clear explanations for HTTP 400, 401, 402, 403, 408, 429, 500, and network disconnects with a prominent **Retry** button.

---

## 🛠️ Build and Running Instructions

### 1. Start the Secure Backend AI Gateway
```powershell
cd "c:\PROJECTS\AI ZAYA APP\ZAYA_APP\backend"
npm start
```
*Note: Make sure your `OPENROUTER_API_KEY` is placed in `ZAYA_APP/backend/.env`.*

### 2. Connect Your Phone via USB (with Port Forwarding)
```powershell
# Forward port 3000 so the mobile app seamlessly talks to localhost:3000
adb reverse tcp:3000 tcp:3000

# Install the newly built APK
adb install -r "c:\PROJECTS\AI ZAYA APP\ZAYA_APP\app\build\outputs\apk\debug\app-debug.apk"

# Launch AI ZAYA
adb shell am start -n com.zayaai.app/.MainActivity
```

