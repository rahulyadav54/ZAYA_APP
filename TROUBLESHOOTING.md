# 🆘 TROUBLESHOOTING GUIDE

## Common Issues & Solutions

---

## ❌ Issue 1: "All free APIs failed. Please check your API keys..."

### Causes
- API keys are incorrect
- API keys have extra spaces
- API keys are empty
- Network connection issue

### Solutions

**Solution A: Check API Keys**
1. Open `MultiAPIService.kt`
2. Line 70: `private const val GROQ_API_KEY = "..."`
   - Should start with `gsk_proj_`
   - No extra spaces
   - No quotes around the key value
3. Line 71: `private const val GEMINI_API_KEY = "..."`
   - Should start with `AIza`
   - No extra spaces
4. Save and rebuild

**Solution B: Verify Keys Are Valid**
1. Visit https://console.groq.com/keys
   - Click on your API key
   - Verify it's active (not deleted)
2. Visit https://ai.google.dev
   - Check your API key is active

**Solution C: Check Network**
1. Test internet connection
2. Check if APIs are up: https://status.groq.com
3. Try again in 5 minutes

---

## ❌ Issue 2: "Cannot resolve MultiAPIService"

### Causes
- File `MultiAPIService.kt` doesn't exist
- File is in wrong folder
- Project not rebuilt
- Cache not cleared

### Solutions

**Solution A: Verify File Exists**
1. Navigate to: `app/src/main/java/com/aizaya/app/`
2. Look for `MultiAPIService.kt` file
3. If missing, create it from the provided code

**Solution B: Clean & Rebuild**
1. Android Studio → Build → Clean Project
2. Wait for clean to complete
3. Build → Rebuild Project
4. Wait for build to complete
5. Try again

**Solution C: Check File Location**
1. Right-click `MultiAPIService.kt`
2. Click "Copy Path"
3. Should be in: `com/aizaya/app/` package
4. If not, move it to correct location

---

## ❌ Issue 3: "Retrofit build failed"

### Causes
- Import statements wrong
- Gson dependency missing
- OkHttp dependency missing
- Retrofit dependency missing

### Solutions

**Solution A: Check Dependencies**
1. Open `build.gradle.kts` (app level)
2. Ensure these are present:
   ```gradle
   implementation("com.squareup.retrofit2:retrofit:2.9.0")
   implementation("com.squareup.retrofit2:converter-gson:2.9.0")
   implementation("com.squareup.okhttp3:okhttp:4.11.0")
   implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
   implementation("com.google.code.gson:gson:2.10.1")
   ```
3. Sync gradle
4. Rebuild

**Solution B: Check Imports**
1. Open `MultiAPIService.kt`
2. Check imports at top are correct:
   ```kotlin
   import retrofit2.Retrofit
   import retrofit2.converter.gson.GsonConverterFactory
   import okhttp3.OkHttpClient
   import okhttp3.logging.HttpLoggingInterceptor
   import com.google.gson.Gson
   import com.google.gson.GsonBuilder
   ```

**Solution C: Clear Cache**
1. File → Invalidate Caches
2. Select "Invalidate and Restart"
3. Wait for Android Studio to restart
4. Rebuild project

---

## ❌ Issue 4: Response is Very Slow (5+ seconds)

### Causes
- Groq API key is wrong
- Internet connection is slow
- Server is overloaded
- Request is too large

### Solutions

**Solution A: Verify Groq Key**
1. Test your Groq API key directly:
   - Visit https://console.groq.com/
   - Check API key status
   - Regenerate if needed
2. Update `MultiAPIService.kt` with correct key
3. Rebuild and test

**Solution B: Check Internet**
1. Open browser
2. Visit https://www.google.com
3. Should load instantly
4. If slow, restart WiFi/mobile network

**Solution C: Reduce Request Size**
1. In `ChatViewModel.kt`, check the prompt size
2. Try with shorter message
3. If that's faster, original prompt was too large

**Solution D: Check API Status**
1. Visit https://status.groq.com
2. Visit https://status.google.com
3. If red status, APIs might be overloaded
4. Wait 5 minutes and try again

---

## ❌ Issue 5: App Crashes When Sending Message

### Causes
- Null pointer exception
- Missing permissions
- Network error not handled
- Out of memory

### Solutions

**Solution A: Check Logs**
1. Android Studio → Logcat
2. Filter: `MultiAPIService` or `ChatViewModel`
3. Look for red errors
4. Check line numbers
5. Fix the code

**Solution B: Check Permissions**
1. `AndroidManifest.xml` should have:
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   ```
2. Grant permission in app settings

**Solution C: Add Error Handling**
1. Verify `formatErrorMessage()` exists in `ChatViewModel.kt`
2. Should catch all exceptions
3. If not, add try-catch blocks

**Solution D: Check Memory**
1. If app crashes with large images
2. Reduce image size before sending
3. Add memory optimization code

---

## ❌ Issue 6: "gsk_proj_" key not working

### Causes
- Key is expired
- Key is deleted
- Key has insufficient permissions
- Wrong format

### Solutions

**Solution A: Regenerate Groq Key**
1. Visit https://console.groq.com/keys
2. Delete old key
3. Create new API key
4. Copy the new key (entire string)
5. Update `MultiAPIService.kt` line 70
6. Rebuild and test

**Solution B: Check Key Format**
1. Groq keys always start with: `gsk_proj_`
2. Should be long (50+ characters)
3. No spaces, no extra characters
4. Should look like: `gsk_proj_abc123def456ghi789jkl0`

**Solution C: Verify Permissions**
1. Go to https://console.groq.com
2. Click your API key
3. Check "Permissions" section
4. Should have chat completion enabled

---

## ❌ Issue 7: "AIzaSy..." key not working

### Causes
- Key is restricted to wrong domain
- Key has wrong permissions
- Key is deleted
- Key format is wrong

### Solutions

**Solution A: Regenerate Gemini Key**
1. Visit https://ai.google.dev
2. Click "Get API Key"
3. Delete old key
4. Create API key in Google Cloud Console
5. Copy new key
6. Update `MultiAPIService.kt` line 71
7. Rebuild and test

**Solution B: Check Key Format**
1. Gemini keys always start with: `AIzaSy`
2. Should be long (40+ characters)
3. No spaces, no extra characters

**Solution C: Enable API**
1. Visit Google Cloud Console
2. Enable "Generative Language API"
3. Ensure key is linked to project
4. Wait 5 minutes for changes to take effect

---

## ❌ Issue 8: Groq works but Gemini doesn't (or vice versa)

### Causes
- One API key is incorrect
- One API is down
- One API has different rate limit hit
- One API is not enabled

### Solutions

**Solution A: Test Each API Separately**
1. Try messages with just Groq key valid
2. Try messages with just Gemini key valid
3. See which one fails
4. Fix that specific key

**Solution B: Check API Status**
1. https://status.groq.com - Check Groq status
2. https://status.google.com - Check Google status
3. If one is red, might be temporarily down
4. Try again in 5-10 minutes

**Solution C: Check Rate Limits**
1. Both might be hitting their limits
2. Wait 1 minute between messages
3. Check if pattern repeats
4. Reduce message frequency

---

## ❌ Issue 9: "buildBuildConfigFile" error

### Causes
- Gradle sync issue
- Incompatible Kotlin version
- Plugin conflict

### Solutions

**Solution A: Sync Gradle**
1. Android Studio → File → Sync Now
2. Wait for sync to complete
3. Rebuild project

**Solution B: Update Kotlin**
1. Open `build.gradle.kts`
2. Check Kotlin version (should be 1.9+)
3. Update if older
4. Sync and rebuild

**Solution C: Clean Cache**
1. Delete `.gradle` folder in project
2. Delete `build` folder
3. Android Studio → Rebuild Project

---

## ❌ Issue 10: Nothing happens when I send message

### Causes
- isTyping flag stuck
- Message not being processed
- Error is being caught silently
- Network completely down

### Solutions

**Solution A: Check Logcat**
1. Filter: `ChatViewModel`
2. Send a message
3. Look for any errors/warnings
4. Check exception details
5. Fix based on error

**Solution B: Check Network**
1. Airplane mode OFF
2. WiFi or mobile data connected
3. Speedtest.net to verify
4. If no connection, that's the issue

**Solution C: Restart App**
1. Close app completely
2. Wait 5 seconds
3. Reopen app
4. Try again

**Solution D: Check isTyping Flag**
1. If isTyping gets stuck, UI freezes
2. Add timeout in `ChatViewModel.kt`:
   ```kotlin
   isTyping = false  // Force reset after 30 seconds
   ```

---

## ✅ Solution Checklist

If you're stuck, go through this:

- [ ] API keys are correct (check for spaces)
- [ ] API keys are active on their consoles
- [ ] Internet connection is working
- [ ] Project builds successfully
- [ ] File `MultiAPIService.kt` exists
- [ ] imports are correct
- [ ] Gradle dependencies are installed
- [ ] APIs are not down (check status pages)
- [ ] Logcat shows no exceptions
- [ ] Permissions include INTERNET
- [ ] Tried rebuilding project
- [ ] Tried clearing cache
- [ ] Tried restarting Android Studio

---

## 📞 Getting More Help

### If still stuck:

1. **Check Guides:**
   - `FREE_API_SETUP_GUIDE.md` - Setup help
   - `QUICK_API_SWAP.md` - Quick reference
   - `IMPLEMENTATION_CHECKLIST.md` - Step by step

2. **Check Docs:**
   - Groq: https://console.groq.com/docs
   - Gemini: https://ai.google.dev/tutorials

3. **Check Status:**
   - Groq: https://status.groq.com
   - Google: https://status.google.com

4. **Copy Logs:**
   - Android Studio → Logcat
   - Filter for errors
   - Share the error message

---

## 🎯 Last Resort

If nothing works:

**Option 1: Start Fresh**
1. Delete `MultiAPIService.kt`
2. Copy entire file again from my code
3. Add API keys
4. Rebuild

**Option 2: Use Just OpenRouter**
1. Keep using existing `AIService.kt`
2. It now has better error messages
3. At least errors are user-friendly

**Option 3: Contact Support**
1. Groq: https://console.groq.com/help
2. Google: support.google.com
3. Share error logs

---

**Most issues are solved by:**
1. Verifying API keys (copy-paste fresh)
2. Rebuilding project (Clean → Rebuild)
3. Restarting Android Studio
4. Checking network connection

**Try those three things first! 🚀**

