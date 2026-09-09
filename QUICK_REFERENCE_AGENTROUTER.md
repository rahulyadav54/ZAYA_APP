# 🚀 AGENT ROUTER INTEGRATION - QUICK REFERENCE

## FILES CREATED/MODIFIED

| File | Type | Location | Purpose |
|------|------|----------|---------|
| **AgentRouterClient.kt** | NEW | `app/src/main/java/com/aizaya/app/` | Production OkHttp client |
| **ChatViewModel.kt** | MODIFIED | `app/src/main/java/com/aizaya/app/` | Updated to use AgentRouterClient |
| **AgentRouterUsageExamples.kt** | NEW | `app/src/main/java/com/aizaya/app/examples/` | 10 complete examples |

---

## CONFIGURATION

### Endpoint
```
https://api.agentrouter.ai/v1/chat/completions
```

### API Key
```
sk-HLErwX4JJHOZHX7U2i47b9wNYNCj6HzZZJxJmnFVjfpm1d3V
```

### Available Models
| Model | Type | Use Case |
|-------|------|----------|
| `deepseek-v3.1` | CHAT | Regular conversations |
| `deepseek-r1-0528` | REASONING | Complex tasks |
| `glm-4.6` | FAST | Quick responses |

---

## BASIC USAGE

### Send Simple Message (Recommended)
```kotlin
val client = AgentRouterClient()
client.sendMessageAsync(
    message = "Hello!",
    modelType = AgentRouterClient.ModelType.CHAT,
    onSuccess = { response ->
        println("✅ $response")
    },
    onError = { error ->
        println("❌ $error")
    }
)
```

### Send with Different Models
```kotlin
// For reasoning
modelType = AgentRouterClient.ModelType.REASONING  // slow but accurate

// For fast response
modelType = AgentRouterClient.ModelType.FAST       // quick

// For regular chat (default)
modelType = AgentRouterClient.ModelType.CHAT       // balanced
```

### Multi-turn Conversation
```kotlin
val messages = listOf(
    "First message" to true,    // user message
    "AI response" to false,     // assistant response
    "Follow-up" to true        // user message
)

client.sendConversation(
    messages = messages,
    modelType = AgentRouterClient.ModelType.CHAT,
    onSuccess = { response -> /* ... */ },
    onError = { error -> /* ... */ }
)
```

---

## BUILD & RUN

### 1. Clean Build
```bash
cd C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA
./gradlew clean build -x test
```

### 2. Install
```bash
./gradlew installDebug
```

### 3. Run
```
Android Studio: Run → Run 'app'
```

---

## TESTING

### Check Logs
```
View → Tool Windows → Logcat
Filter: AgentRouterClient
```

### Expected Success Log
```
D/AgentRouterClient: Sending request to: https://api.agentrouter.ai/v1/chat/completions
D/AgentRouterClient: Model: deepseek-v3.1
D/AgentRouterClient: Response Status: 200
D/AgentRouterClient: Parsed response: [AI response]
```

### Manual Test
1. Open AI Chat screen
2. Type: "Hello"
3. Click Send
4. Wait 3-10 seconds
5. Should see AI response (not error)

---

## COMMON ERRORS & FIXES

| Error | Cause | Fix |
|-------|-------|-----|
| `Invalid URL` | Old AIService used | Rebuild project |
| `Model not found` | Wrong model name | Use exact names: `deepseek-v3.1`, `deepseek-r1-0528`, `glm-4.6` |
| `Invalid API Key` (401) | Wrong API key | Verify in AgentRouterClient.kt |
| `Rate limit exceeded` (429) | Too many requests | Wait 1-5 minutes |
| `Connection timeout` | Slow network | Check internet, increase timeout |
| `No response` | Network issue | Restart device, check WiFi |

---

## ARCHITECTURE

```
User Input
    ↓
ChatViewModel.sendMessage()
    ↓
AgentRouterClient.sendMessageAsync()
    ↓
OkHttpClient.newCall(request).enqueue()
    ↓
POST https://api.agentrouter.ai/v1/chat/completions
    ↓
Response parsing
    ↓
onSuccess/onError callback
    ↓
Update UI
    ↓
Display message
```

---

## FEATURE SUPPORT

| Feature | Status | Example |
|---------|--------|---------|
| Chat | ✅ | `ModelType.CHAT` |
| Reasoning | ✅ | `ModelType.REASONING` |
| Fast Response | ✅ | `ModelType.FAST` |
| Multi-turn | ✅ | `sendConversation()` |
| Async (non-blocking) | ✅ | `sendMessageAsync()` |
| Sync (blocking) | ✅ | `sendMessageSync()` |
| Error Handling | ✅ | 401, 429, 500, timeout |
| Logging | ✅ | Filter: `AgentRouterClient` |
| Voice Assistant | ✅ | See examples |
| AI Tutor | ✅ | See examples |
| Code Generation | ✅ | See examples |
| Batch Processing | ✅ | See examples |

---

## TIMEOUT SETTINGS

Current values (in AgentRouterClient.kt):
```kotlin
.connectTimeout(60, TimeUnit.SECONDS)
.readTimeout(60, TimeUnit.SECONDS)
.writeTimeout(60, TimeUnit.SECONDS)
```

To increase (if getting timeout errors):
```kotlin
.connectTimeout(120, TimeUnit.SECONDS)  // 2 minutes
.readTimeout(180, TimeUnit.SECONDS)     // 3 minutes
.writeTimeout(120, TimeUnit.SECONDS)    // 2 minutes
```

---

## PRODUCTION CHECKLIST

Before deploying:
- [ ] API key is correct
- [ ] Endpoint is `https://api.agentrouter.ai/v1/chat/completions`
- [ ] Models are exact: `deepseek-v3.1`, `deepseek-r1-0528`, `glm-4.6`
- [ ] All async calls use `sendMessageAsync()` (not sync)
- [ ] Error handling is comprehensive
- [ ] Logging is in place
- [ ] Testing on real device completed
- [ ] Internet permission in AndroidManifest.xml
- [ ] No API key hardcoded (move to BuildConfig)
- [ ] Rate limiting handled

---

## MONITORING

### Key Metrics to Track
- Response time (should be 3-10s)
- Error rate (should be < 1%)
- Model success rate
- Timeout occurrences
- Rate limit hits

### Sample Monitoring Code
```kotlin
val startTime = System.currentTimeMillis()

client.sendMessageAsync(
    message = message,
    modelType = AgentRouterClient.ModelType.CHAT,
    onSuccess = { response ->
        val responseTime = System.currentTimeMillis() - startTime
        Log.d("Monitor", "Success in ${responseTime}ms")
        // Track metric
    },
    onError = { error ->
        Log.e("Monitor", "Error: $error")
        // Track error
    }
)
```

---

## EXTENDING FOR NEW FEATURES

### Voice Assistant
```kotlin
fun processVoice(recognizedText: String) {
    client.sendMessageAsync(
        message = recognizedText,
        modelType = AgentRouterClient.ModelType.CHAT,
        onSuccess = { response -> speakResponse(response) }
    )
}
```

### AI Tutor
```kotlin
fun explainTopic(topic: String) {
    val prompt = "Explain $topic in simple terms"
    client.sendMessageAsync(
        message = prompt,
        modelType = AgentRouterClient.ModelType.REASONING  // use reasoning for education
    )
}
```

### Code Generator
```kotlin
fun generateCode(requirement: String) {
    val prompt = "Generate Kotlin code for: $requirement"
    client.sendMessageAsync(
        message = prompt,
        modelType = AgentRouterClient.ModelType.REASONING
    )
}
```

---

## PERFORMANCE TIPS

1. **Reuse Client**: Create once, use many times
   ```kotlin
   private val client = AgentRouterClient()  // singleton
   ```

2. **Use Async**: Never block main thread
   ```kotlin
   client.sendMessageAsync(...)  // ✅ Good
   client.sendMessageSync(...)   // ❌ Only on background thread
   ```

3. **Handle Errors**: Always provide error callback
   ```kotlin
   onError = { error -> showErrorToUser(error) }
   ```

4. **Add Delays**: For batch processing
   ```kotlin
   Thread.sleep(1000)  // 1 second delay between requests
   ```

5. **Monitor Logs**: Check for bottlenecks
   ```
   Filter: AgentRouterClient
   Look for: Response Status and timing
   ```

---

## SECURITY NOTES

🔐 **For Production:**
1. Move API key from code to BuildConfig
2. Use environment variables
3. Implement certificate pinning
4. Add request signing
5. Rate limit on client side
6. Validate responses

**Current Setup** (Dev):
- ✅ HTTPS only
- ✅ Bearer token auth
- ✅ Proper error handling
- ⚠️ API key in code (move for production)

---

## SUPPORT RESOURCES

- **Implementation Guide**: `AGENTROUTER_COMPLETE_IMPLEMENTATION.md`
- **Build Guide**: `BUILD_AND_DEPLOY_GUIDE_AGENTROUTER.md`
- **Examples**: `AgentRouterUsageExamples.kt`
- **Troubleshooting**: `AGENTROUTER_TROUBLESHOOTING.md`
- **Logs**: Filter `AgentRouterClient` in Logcat

---

**Ready to Deploy! 🚀**

Questions? Check the implementation guide or usage examples.

