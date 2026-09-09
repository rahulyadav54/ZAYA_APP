# Complete Guide: Rename Android Package from com.aizaya.app to com.zayaai.app

## Overview
This guide will help you safely rename your Android project package name to match your Play Console package name: `com.zayaai.app`

---

## 📋 Pre-Rename Checklist
Before starting, ensure you:
- [ ] Have a backup of your project
- [ ] Close Android Studio completely
- [ ] Commit your code to Git (if using version control)
- [ ] Have about 30 minutes for this process

---

## ⚠️ What Will Change
The following files and directories will be affected:
1. `app/build.gradle.kts` - `applicationId` and `namespace`
2. `app/src/main/AndroidManifest.xml` - package declaration (if present)
3. `app/src/main/java/com/aizaya/app/` - entire directory structure will be renamed
4. `app/src/androidTest/java/com/aizaya/app/` - test files (if any)
5. `app/src/test/java/com/aizaya/app/` - unit test files (if any)
6. `google-services.json` - may need recreation if using Firebase
7. Any hardcoded references in code files

---

## 🔧 Step-by-Step Renaming Process

### **Step 1: Update build.gradle.kts**
**File:** `app/build.gradle.kts`

Change:
- Line 8: `namespace = "com.aizaya.app"` → `namespace = "com.zayaai.app"`
- Line 21: `applicationId = "com.aizaya.app"` → `applicationId = "com.zayaai.app"`

### **Step 2: Rename Package Directory Structure**
The directory structure needs to change from:
```
app/src/main/java/com/aizaya/app/
```
to:
```
app/src/main/java/com/zayaai/app/
```

**Method A: Using File System (Safest)**
1. Open File Explorer to: `C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA\app\src\main\java\com\`
2. Rename folder `aizaya` → `zayaai`
3. Repeat for `app/src/androidTest/java/com/` (if exists)
4. Repeat for `app/src/test/java/com/` (if exists)

**Method B: Using Android Studio (Recommended)**
1. Open Android Studio
2. In Project view (Alt+1), navigate to: `app > src > main > java > com > aizaya`
3. Right-click on the `aizaya` folder → **Refactor** → **Rename...**
4. Type `zayaai`
5. Android Studio will automatically update all imports and references

### **Step 3: Update Package Declarations in Java/Kotlin Files**
The first line in each `.kt` or `.java` file needs to be updated from:
```kotlin
package com.aizaya.app
```
to:
```kotlin
package com.zayaai.app
```

**Automatic Update Method:**
1. Use Find and Replace (Ctrl+H)
2. Find: `package com.aizaya.app`
3. Replace with: `package com.zayaai.app`
4. Click "Replace All" (check "Match Case")

### **Step 4: Update Imports**
Any import statements referencing the old package name:

**Find and Replace:**
- Find: `import com.aizaya.app`
- Replace with: `import com.zayaai.app`
- Find: `com.aizaya.app.` (all internal imports)
- Replace with: `com.zayaai.app.` 

### **Step 5: Check AndroidManifest.xml**
If there are any hardcoded package references, update them.

### **Step 6: Verify Firebase Configuration (google-services.json)**
If you're using Firebase:
1. Open your `google-services.json` file
2. Check if the `package_name` field matches your new package name
3. If not, download a new `google-services.json` from Firebase Console with the new package name
4. Replace the old file

### **Step 7: Clean and Rebuild**
1. **File** → **Invalidate Caches** → **Invalidate and Restart**
2. After restart: **Build** → **Clean Project**
3. Then: **Build** → **Rebuild Project**

---

## ✅ Verification Checklist

After renaming, verify:
- [ ] Project builds without errors
- [ ] Package name in build.gradle.kts is correct
- [ ] No "Package does not match directory" errors
- [ ] All imports are resolved (Ctrl+Alt+O to optimize imports)
- [ ] No syntax errors in AndroidManifest.xml
- [ ] Test runs without errors

---

## 🚨 Common Mistakes to Avoid

1. **Not updating build.gradle.kts** - This is crucial! Without this, the APK will still have the old package name.
2. **Missing imports** - Run Ctrl+Alt+O to fix imports after renaming
3. **Hardcoded package references** - Search for "com.aizaya" in all files
4. **Forgetting test directories** - Update `app/src/androidTest/` and `app/src/test/` too
5. **Not invalidating caches** - This can cause false error messages
6. **Firebase compatibility** - Ensure google-services.json matches the new package name

---

## 🎯 Building APK/AAB After Renaming

### **Build APK (for testing):**
1. **Build** → **Build Bundle(s)/APK(s)** → **Build APK(s)**
2. Find APK at: `app/build/outputs/apk/debug/`

### **Build AAB (for Play Store):**
1. **Build** → **Build Bundle(s)/APK(s)** → **Build Bundle(s)**
2. Find AAB at: `app/build/outputs/bundle/release/`
3. Verify package name in AAB using Android Studio's bundletool analyzer

### **Verify Package Name in Generated File:**
Use this command in PowerShell (from your project directory):
```powershell
cd "C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA\app\build\outputs\bundle\release"
# List files to verify
Get-ChildItem
```

---

## 📱 Testing Before Upload

Before uploading to Play Store:
1. Generate a signed APK/AAB
2. Install on a test device or emulator
3. Verify the app launches correctly
4. Check that all features work as expected
5. Use Play Store's validation tools to check the bundle

---

## 🔄 If Something Goes Wrong

**Rollback option (if you have Git):**
```powershell
git reset --hard HEAD
# Or revert specific files:
git checkout -- .
```

**Manual rollback:**
1. Rename `com/zayaai/` folder back to `com/aizaya/`
2. Revert all changes in build.gradle.kts
3. Revert package declarations and imports

---

## 📞 Troubleshooting

| Error | Solution |
|-------|----------|
| "Package does not match directory" | Verify directory name matches package declaration |
| Import errors after rename | Run **Code** → **Optimize Imports** (Ctrl+Alt+O) |
| Build fails with gradle error | Run **Build** → **Clean Project**, then rebuild |
| APK still has old package name | Verify `applicationId` in build.gradle.kts is correct |
| Firebase errors | Download and replace google-services.json |
| Emulator shows old app | Uninstall old app: `adb uninstall com.aizaya.app` |

---

## 🎉 Success Indicators

You've successfully renamed the package when:
✅ Project builds without errors  
✅ APK/AAB has package name `com.zayaai.app`  
✅ App launches on emulator/device  
✅ All features work correctly  
✅ No warnings about package mismatch  
✅ Play Store accepts the upload  

---

## Next Steps After Renaming

1. Update Play Console app listing if needed
2. Create a signed APK/AAB for release build
3. Test the app thoroughly
4. Upload to Play Store Internal Testing track first
5. Once verified, promote to production


