# 🚀 AIZAYA PlayStore Deployment Guide

## ✅ Pre-Deployment Checklist

### Step 1: Prepare the Build Configuration
- [ ] API Configuration: ✅ DONE (OpenRouter API key updated)
- [ ] Firebase Configuration: ✅ DONE (google-services.json present)
- [ ] Firebase Security Rules: ✅ DONE (production rules available)
- [ ] Version Code/Name: **Current: 1.0 (versionCode=1)**
- [ ] App Signing: NEEDS SETUP

### Step 2: Update Version Numbers
**⚠️ IMPORTANT:** Before each PlayStore release, update the version:

```kotlin
// File: app/build.gradle.kts
defaultConfig {
    versionCode = 1      // Increment for each release (1, 2, 3...)
    versionName = "1.0"  // Use semantic versioning (1.0, 1.1, 2.0...)
}
```

**Version History:**
- v1.0 (Build 1) - Initial Release

---

## 📋 STEP 1: Create a Keystore for App Signing

### What is a Keystore?
A keystore is a certificate that proves you own the app. PlayStore requires all apps to be signed with the same key for updates.

### Generate the Keystore (First Time Only)

**In Android Studio:**
1. Go to **Build** → **Generate Signed Bundle/APK**
2. Select **Android App Bundle** (recommended for PlayStore)
3. Click **Create new...** under "Key store path"
4. Fill in the details:

```
Key store path:     C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA\aizaya-keystore.jks
Password:           [CREATE A STRONG PASSWORD - SAVE THIS!]
Key alias:          aizaya-key
Key password:       [SAME PASSWORD OR DIFFERENT]
Validity (years):   25
Organization:       ZAYA CODE HUB
City/locality:      [Your City]
State/province:     [Your State]
Country code:       IN
```

**⚠️ CRITICAL:** Save this keystore file and password in a SAFE LOCATION!

### Generate the Keystore (Command Line)

If you prefer command line:

```bash
cd C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA

keytool -genkey -v -keystore aizaya-keystore.jks ^
  -keyalg RSA ^
  -keysize 2048 ^
  -validity 9125 ^
  -alias aizaya-key
```

Then follow the prompts.

---

## 📋 STEP 2: Configure Signing in Gradle

**Edit: `app/build.gradle.kts`**

Add this signing configuration:

```kotlin
android {
    namespace = "com.aizaya.app"
    compileSdk = 36

    signingConfigs {
        create("release") {
            storeFile = file("../aizaya-keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "your-password"
            keyAlias = "aizaya-key"
            keyPassword = System.getenv("KEYSTORE_PASSWORD") ?: "your-password"
        }
    }

    defaultConfig {
        applicationId = "com.aizaya.app"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    // ... rest of config
}
```

---

## 📋 STEP 3: Enable ProGuard for Release Build

ProGuard reduces APK size and obfuscates code for security.

**File: `app/proguard-rules.pro`** (Already exists, verify it has these rules)

```proguard
# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Gson
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Keep app classes
-keep class com.aizaya.app.** { *; }
-keepclassmembers class com.aizaya.app.** { *; }

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
```

---

## 📋 STEP 4: Build the Release Bundle

### Method 1: Android Studio GUI
1. **Build** → **Generate Signed Bundle/APK**
2. Select **Android App Bundle**
3. Choose keystore (or create new one)
4. Enter keystore password
5. Select **Release** build type
6. Click **Create**
7. Wait for build to complete
8. Find output in: `app/release/app-release.aab`

### Method 2: Command Line
```bash
cd C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA

# Set environment variables (Windows PowerShell)
$env:KEYSTORE_PASSWORD = "your-keystore-password"

# Build release bundle
./gradlew bundleRelease

# Find output
Get-Item app/build/outputs/bundle/release/app-release.aab
```

---

## 📋 STEP 5: Create PlayStore Account & App

### 5.1: Set Up Google Play Developer Account
1. Go to **[Google Play Console](https://play.google.com/console)**
2. Sign in with your Google account
3. Complete the developer registration:
   - Pay $25 USD (one-time)
   - Accept terms and conditions
   - Add payment method
4. Complete your developer profile

### 5.2: Create New App
1. In Google Play Console, click **Create app**
2. Fill in details:
   ```
   App name:           AI ZAYA
   Default language:   English
   App type:           Free App
   Category:           Education
   Content rating:     Everyone (or appropriate rating)
   ```
3. Click **Create app**

---

## 📋 STEP 6: Fill In App Details

### 6.1: App Details Page
- **Short description:** "AI-powered learning companion with instant tutoring, quizzes, and study materials"
- **Full description:**
  ```
  AI ZAYA is an intelligent learning platform that combines:
  
  ✨ Features:
  • AI Tutor - Ask any question and get instant answers
  • Quiz Generator - Create unlimited quizzes
  • Flashcards - Smart flashcard system
  • Study Materials - Organized learning resources
  • Admin Dashboard - Manage content
  • Biometric Security - Fingerprint/Face login
  
  🎯 Perfect for:
  • Students studying for exams
  • Teachers creating test materials
  • Anyone looking to learn faster
  
  💡 Why AI ZAYA?
  • Lightning-fast AI responses
  • Completely offline support (some features)
  • Privacy-focused security
  • No ads, no distractions
  
  Developed by ZAYA CODE HUB
  ```

### 6.2: App Icon & Screenshots
1. Create app icon (512x512 PNG, 32MB max)
   - Should be professional and match your brand
2. Create 2-5 screenshots (1080x1920 or 1440x2560)
   - Show key features
   - Use text overlays to explain features
3. Create a feature graphic (1024x500 PNG)
   - Used in store listings

### 6.3: Category & Content Rating
- **Category:** Education
- **Content Rating Questionnaire:** Complete it honestly
  - Violence: None
  - Content: Appropriate for all ages
  - Other content: None

---

## 📋 STEP 7: Set Up App Signing

### 7.1: Choose Signing Method
1. In Google Play Console → **Setup** → **App signing**
2. Choose: **Google Play App Signing** (Recommended)
   - Google manages your keystore
   - Safer and more convenient
   - OR upload your own keystore

### 7.2: Upload Signing Certificate
If not using Google Play App Signing:
1. Export your keystore certificate:
   ```bash
   keytool -export -rfc -alias aizaya-key -file aizaya-cert.pem ^
     -keystore aizaya-keystore.jks
   ```
2. Upload `aizaya-cert.pem` to PlayStore

---

## 📋 STEP 8: Upload Bundle & Configure Release

### 8.1: Upload App Bundle
1. In Google Play Console → **Testing** → **Internal testing**
2. Click **Create new release**
3. Click **Upload** and select `app-release.aab`
4. Fill in release notes:
   ```
   Version 1.0
   
   ✨ Initial Release Features:
   • AI Tutor with real-time responses
   • Quiz and flashcard system
   • Study materials library
   • Secure admin panel
   • Biometric authentication
   ```

### 8.2: Review & Test
1. Distribute to **Internal testing** track first (test with team)
2. Fix any issues
3. Move to **Open testing** (limited beta)
4. Then promote to **Production**

---

## 📋 STEP 9: Configure Pricing & Distribution

### 9.1: Pricing
1. **Manage apps** → Your app → **Pricing**
2. Select: **Free**

### 9.2: Countries/Regions
1. **Manage apps** → Your app → **Countries, regions, and territories**
2. Select all countries or specific ones where you want the app available

### 9.3: Content Rating
1. **Setup** → **Content rating**
2. Complete the questionnaire
3. Your rating will be assigned

---

## 📋 STEP 10: Final Checks Before Launch

### Pre-Launch Checklist
- [ ] App builds without errors
- [ ] All 4 models in OpenRouter API are correctly configured
- [ ] Firebase is connected and rules are set to production
- [ ] Admin account created (admin@zaya.com / password123)
- [ ] App icon uploaded (512x512)
- [ ] 2-5 screenshots uploaded
- [ ] App description complete and accurate
- [ ] Privacy policy URL added (if required)
- [ ] Contact email for support added
- [ ] Version 1.0 built and signed with keystore
- [ ] Bundle uploaded to Internal Testing
- [ ] Tested on real device (or emulator)
- [ ] AI chat feature works without "SERVICE UNAVAILABLE"
- [ ] Admin login works
- [ ] Firebase data loads correctly

---

## 🚀 STEP 11: Submit for Review

### 11.1: Submit to Production
1. In Google Play Console
2. **Testing** → **Internal testing** → Select your release
3. Click **Promote to Open testing**
4. Test for 1-2 days
5. **Promote to Production**
6. Click **Review and roll out**
7. Confirm details and click **Rollout**

### 11.2: Track Review Status
- Google usually reviews within **24-48 hours**
- You'll receive email notifications
- If rejected, fix issues and resubmit

### 11.3: Post-Launch
- Monitor reviews and ratings
- Fix bugs and crashes quickly
- Plan future updates
- Engage with users

---

## 📊 Versioning Strategy

For each update, follow this pattern:

```
Version 1.0 → 1.1 → 1.2 → 2.0 → 2.1 ...

Patch version (1.0.x): Bug fixes, small changes
Minor version (1.x.0): New features
Major version (x.0.0): Significant redesign or features
```

**Update in `app/build.gradle.kts`:**
```kotlin
versionCode = 2          // Always increment
versionName = "1.1"      // Semantic version
```

---

## 🔧 Troubleshooting

### Build Fails with "Signing" Error
**Solution:**
```bash
# Verify keystore exists
Test-Path "C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA\aizaya-keystore.jks"

# Verify password is correct
keytool -list -v -keystore aizaya-keystore.jks
```

### "SERVICE UNAVAILABLE" Error in App
**Solution:** The API isn't responding. Check:
1. OpenRouter API key is valid: `YOUR_API_KEY_HERE`
2. Models are correct and available
3. Internet connection works
4. API account has credits

### PlayStore Review Rejected
**Common reasons:**
- App crashes on startup
- Requires permissions but doesn't use them
- Copycat app (too similar to existing app)
- Inappropriate content

**Solution:** Fix the issue, increment versionCode, and resubmit.

---

## 📱 Testing Before Launch

### Required Testing
1. **Device testing:**
   - Test on real Android device (not just emulator)
   - Test on Android 10, 11, 12, 13, 14+
   - Test with various screen sizes

2. **Feature testing:**
   - AI Chat works (send 5+ messages)
   - Admin login works
   - Quizzes load correctly
   - Biometric login works (if available)
   - Data persists after app restart

3. **Performance:**
   - App starts in < 5 seconds
   - No crashes or ANRs
   - Memory usage reasonable

4. **Security:**
   - No API keys exposed in logs
   - Permissions used appropriately
   - User data stored securely

---

## 📞 Support & Monitoring

### After Launch
1. **Monitor crashes:** Google Play Console → Crashes & ANRs
2. **Read reviews:** Respond to user feedback
3. **Track downloads:** Available in Play Console dashboard
4. **Plan updates:**
   - New features
   - Bug fixes
   - Performance improvements

---

## 🔐 Important Files to Protect

**NEVER share publicly:**
- `aizaya-keystore.jks` (keep backup in secure location)
- Keystore password
- OpenRouter API key (already in code - consider moving to backend)
- Firebase service account key

---

## ✅ Summary

1. ✅ Update API key in AIService.kt (DONE)
2. Create keystore for signing
3. Configure ProGuard for optimization
4. Build release bundle
5. Create Google Play Developer account
6. Fill in app details and graphics
7. Upload bundle
8. Test on Internal Testing track
9. Submit for review
10. Monitor launch

**Estimated timeline:** 2-3 days from submission to live on PlayStore

---

## 🎉 You're Ready!

The app is configured and ready for PlayStore. Follow these steps systematically, and your app will be live within days!

Need help? Check the troubleshooting section or refer to Google Play official documentation.

**Happy launching! 🚀**

