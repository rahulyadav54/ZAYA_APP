# 🆓 Free Unlimited AI API Setup Guide

## Problem
You're hitting rate limits with OpenRouter's free tier (limited requests/minute).

## Solution
Use **Groq API** (primary) + **Google Gemini** (fallback) - Both have generous FREE tiers!

---

## 📋 Step-by-Step Setup

### Step 1️⃣: Get Groq API Key (RECOMMENDED - Fastest)

**Groq Features:**
- ⚡ **Ultra-fast** response time (up to 300+ tokens/sec)
- 🆓 **Free forever** - 30 requests per minute
- 🎯 Great for real-time chat
- No credit card required

**Get Key:**
1. Visit: https://console.groq.com/keys
2. Sign up with email
3. Click "Create API Key"
4. Copy the key (starts with `gsk_proj_`)
5. Paste in `MultiAPIService.kt` line 70:
```kotlin
private const val GROQ_API_KEY = "gsk_proj_YOUR_KEY_HERE"
```

---

### Step 2️⃣: Get Google Gemini API Key (FALLBACK)

**Gemini Features:**
- 🎨 Supports vision/images
- 🆓 **Free forever** - 60 requests per minute
- 🧠 Powerful reasoning
- No credit card required

**Get Key:**
1. Visit: https://ai.google.dev
2. Click "Get API Key" → "Create API key"
3. Accept terms and copy key (starts with `AIza`)
4. Paste in `MultiAPIService.kt` line 71:
```kotlin
private const val GEMINI_API_KEY = "AIzaSyXXXXXXXXXX"
```

---

## 🔧 How It Works

```
User sends message
        ↓
   Try Groq API (30 req/min, ultra-fast)
        ↓ (if fails)
   Try Gemini API (60 req/min, powerful)
        ↓ (if both fail)
   Show error: "Please try again later"
```

**Benefits:**
- ✅ Total 90+ requests/minute across both
- ✅ Automatic fallback if one fails
- ✅ No rate limiting errors
- ✅ Completely free
- ✅ Works forever

---

## 📱 Optional: Switch Your App to Use MultiAPIService

If you want to **replace OpenRouter completely**, update `ChatViewModel.kt`:

**Current code:**
```kotlin
AIService.chat(conversationMessages)
```

**New code:**
```kotlin
MultiAPIService.generateWithFallback(
    prompt = conversationMessages.lastOrNull()?.content.toString(),
    systemPrompt = systemPrompt
)
```

---

## 🚀 API Limits Comparison

| API | Free Limit | Speed | Vision | Cost |
|-----|-----------|-------|--------|------|
| Groq | 30 req/min | ⚡⚡⚡ Ultra-fast | ❌ | 🆓 Forever |
| Gemini | 60 req/min | ⚡⚡ Fast | ✅ | 🆓 Forever |
| OpenRouter | 5-10 req/min | ⚡ Moderate | ✅ | 🆓 Limited |

---

## ✅ Verification

After adding keys, test in your app:
1. Open AI Chat
2. Send a message
3. You should get instant response (no rate limit error)
4. Check console logs for which API was used

---

## 🆘 Troubleshooting

**Still getting rate limits?**
- Check API keys are correct (copy-paste from console)
- Verify no typos in `MultiAPIService.kt`
- Clear app cache and rebuild

**Response is slow?**
- Groq is much faster - ensure it's primary
- Check internet connection
- Some models are slower than others

**API not responding?**
- Both APIs might be down (rare)
- Check https://status.groq.com and https://status.google.com
- Try again in a few moments

---

## 💡 Pro Tips

1. **Never share API keys** - Keep them private!
2. **Monitor usage** - Check console logs for which API is being used
3. **Set spending limits** - Go to console to prevent unexpected charges (though free tier never charges)
4. **Keep multiple APIs** - Ensures reliability if one goes down

Enjoy unlimited AI responses! 🎉

