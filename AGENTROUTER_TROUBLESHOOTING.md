# Agent Router Integration Troubleshooting & Setup Guide

## ✅ Configuration Status

All code has been updated to use Agent Router API correctly:

### APIService.kt Configuration
```
✅ Base URL: https://agentrouter.org/
✅ Endpoint: @POST("/api/v1/chat/completions")
✅ API Key: sk-HLErwX4JJHOZHX7U2i47b9wNYNCj6HzZZJxJmnFVjfpm1d3V
✅ Models: deepsek-r1-0528, deepsek-v3.1, deepsek-v3.2, glm-4.5, glm-4.6
✅ Timeouts: Connect 120s, Read 180s, Write 120s
```

### ChatViewModel Integration
```
✅ Calls: AIService.chat(messages)
✅ Automatic retry on model failure
✅ Error handling with user-friendly messages
```

## 🔧 Rebuild Steps (CRITICAL!)

The code changes won't work until you rebuild. Follow these steps exactly:

### Step 1: Clean Build (Android Studio)
```
1. File → Invalidate Caches → Invalidate and Restart
2. Gradle → Refresh All Projects
3. Build → Clean Project
4. Build → Rebuild Project
```

### Step 2: Rebuild via Terminal
```bash
cd C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA
.\gradlew.bat clean build -x test
```

### Step 3: Uninstall Old App
```
1. Open Settings on your phone
2. Apps → AI ZAYA
3. Uninstall
```

### Step 4: Run Fresh Build
```
Android Studio: Run → Run 'app'
OR
Command: .\gradlew.bat installDebug
```

## 🧪 Testing After Rebuild

Once the app is running:

1. Open AI Chat
2. Send a test message: "Hello, who are you?"
3. Expected response: Should get a response from Agent Router (not an error)

## 🐛 If Still Getting Error

### Error: "invalid URL (POST /api/v1/chat/completions)"
This means the old compiled app is still running. Solution:
1. Go to Android Studio → Run → Stop
2. Uninstall app from device
3. Rebuild with: `./gradlew clean build`
4. Run fresh install

### Error: "Connection timeout"
The API might be slow. The timeouts are already set to 120-180 seconds. Wait longer before giving up.

### Error: "Model not found"
The models being used must match Agent Router's available models:
- deepsek-r1-0528
- deepsek-v3.1
- deepsek-v3.2
- glm-4.5
- glm-4.6

## 📝 Files Modified

1. **AIService.kt**
   - Base URL: `https://agentrouter.org/`
   - Endpoint: `@POST("/api/v1/chat/completions")`
   - Models list updated
   - Timeouts increased

2. **ChatViewModel.kt**
   - Already using: `AIService.chat(messages)`
   - No changes needed

## 🔐 API Key Details

Your Agent Router API Key:
```
sk-HLErwX4JJHOZHX7U2i47b9wNYNCj6HzZZJxJmnFVjfpm1d3V
```

Status: ✅ Unlimited quota with "Enabled Unlimited quota" on Agent Router dashboard

## ✅ Verification Checklist

Before testing, verify:
- [ ] All code changes in AIService.kt are applied
- [ ] Base URL is `https://agentrouter.org/` (not `.ai`)
- [ ] Endpoint is `@POST("/api/v1/chat/completions")`
- [ ] All 5 Agent Router models are in the list
- [ ] API Key matches: `sk-HLErwX4...`
- [ ] App has been fully rebuilt and reinstalled
- [ ] Old app version uninstalled from device

## 📞 If Build Fails

If `./gradlew build` fails, try:

```bash
# Clear gradle cache
.\gradlew clean

# Update gradle wrapper
.\gradlew wrapper --gradle-version=latest

# Rebuild
.\gradlew build -x test
```

## Expected Result After Fix ✅

When working correctly:
1. User sends message in AI Chat
2. App connects to: `https://agentrouter.org/api/v1/chat/completions`
3. Authenticates with Agent Router API key
4. Sends message with selected model
5. Receives response from Agent Router
6. Message appears in chat

No more "invalid URL" or configuration errors!

