# Quick Access Guide - Build Files

## Open Directories Directly

### 📦 Debug APK Folder
```
C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA\app\build\outputs\apk\debug\
```

**File:** `app-debug.apk` (84.9 MB)

---

### 📦 Release APK Folder  
```
C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA\app\build\outputs\apk\release\
```

**File:** `app-release.apk` (57.2 MB)

---

### 📦 AAB Bundle Folder
```
C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA\app\build\outputs\bundle\release\
```

**File:** `app-release.aab` (39.0 MB)

---

## Device Information

| Detail | Value |
|--------|-------|
| Connected Device | ZA223726CB |
| Device Status | ✅ Online |
| App Package | com.zayaai.app |
| App Status | ✅ Running |

---

## How to Re-install

### Install Debug APK:
```powershell
adb install "C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA\app\build\outputs\apk\debug\app-debug.apk"
```

### Install Release APK:
```powershell
adb install "C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA\app\build\outputs\apk\release\app-release.apk"
```

### Uninstall App:
```powershell
adb uninstall com.zayaai.app
```

### Launch App:
```powershell
adb shell am start -n com.zayaai.app/.MainActivity
```

---

## For Play Store Upload

**File needed:** `app-release.aab`

### Steps:
1. Open Google Play Console
2. Go to your app
3. Navigate to "Release" → "Production"
4. Click "Create new release"
5. Upload: `C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA\app\build\outputs\bundle\release\app-release.aab`
6. Review and publish

---

## For Direct APK Distribution

**File needed:** `app-release.apk`

### Share via:
- Email
- Cloud storage (Google Drive, OneDrive)
- File transfer apps
- Direct download link

---

## What's New in This Build

✨ **New Features:**
- ✅ Forgot Password functionality
- ✅ Password reset via email
- ✅ Complete authentication flow

🐛 **Improvements:**
- Code optimization and minification
- Resource shrinking
- ProGuard optimization

---

## Troubleshooting

### If app won't install:
```powershell
# Uninstall first
adb uninstall com.zayaai.app

# Then install
adb install "path/to/app-release.apk"
```

### If app crashes:
```powershell
# View logs
adb logcat | grep zayaai
```

### If device not detected:
```powershell
# Restart ADB
adb kill-server
adb start-server
adb devices
```

---

## Building Again

To rebuild anytime:

```powershell
cd "C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA"

# Clean build
.\gradlew.bat clean

# Debug APK
.\gradlew.bat assembleDebug

# Release APK  
.\gradlew.bat assembleRelease

# AAB Bundle
.\gradlew.bat bundleRelease
```

---

**Last Updated:** April 14, 2026

