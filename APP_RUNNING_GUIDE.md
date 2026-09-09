# AIZAYA App - Running and Testing Guide

## Overview
This guide explains how to properly build and run the AIZAYA app with all fixes applied.

## Recent Fixes Applied

### 1. **StudentQuizPlayer.kt** - Fixed Emoji Encoding Issues
- **Problem**: Corrupted emoji characters were causing display errors
- **Solution**: Replaced corrupt emojis with proper Material Icons (EmojiEvents, ThumbUp, TrendingDown, etc.)
- **Added**: Bounds checking for question index to prevent crashes
- **Added**: Better error messages when quiz has no questions

### 2. **StudentContentViewModel.kt** - Enhanced Error Handling
- **Problem**: Content loading failures weren't being tracked or reported
- **Solution**: Added error state tracking for each content type:
  - `hasErrorAnnouncements`, `hasErrorArticles`, `hasErrorQuizzes`, `hasErrorFormulas`
  - `errorMessageAnnouncements`, `errorMessageArticles`, etc.
- **Added**: Try-catch blocks with detailed error messages

### 3. **HomeScreen.kt** - Added Error Display UI
- **Problem**: Users couldn't see if content failed to load
- **Solution**: 
  - Added `ErrorMessageCard` composable to display loading errors
  - Integrated error display in content sections
  - Added "Retry" buttons for users to reload failed content

### 4. **AndroidManifest.xml** - Verified Permissions
- ✅ INTERNET permission enabled
- ✅ RECORD_AUDIO for voice features
- ✅ USE_BIOMETRIC for app lock
- ✅ WRITE_EXTERNAL_STORAGE & READ_EXTERNAL_STORAGE for file access
- ✅ Clear text traffic enabled for HTTP

## Building the App

### Prerequisites
- Android Studio installed
- Android SDK 36+ installed
- Java 11+ installed
- Gradle wrapper available

### Build Steps

**Option 1: Using Android Studio IDE**
1. Open the project in Android Studio
2. Click **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
3. Wait for the build to complete
4. Find APK at: `app/build/outputs/apk/debug/app-debug.apk`

**Option 2: Using Terminal/Command Line**
```bash
cd "C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA"
.\gradlew.bat clean assembleDebug
```

After build completes:
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`

## Running the App

### Option 1: On Android Emulator
1. Open Android Studio
2. Go to **Tools** → **Device Manager**
3. Launch an emulator (Pixel 5 API 33+ recommended)
4. Click **Run** (Green Play button) or press `Shift + F10`
5. Select the emulator from the dialog
6. App will install and launch

### Option 2: On Physical Android Device
1. Enable **Developer Mode**: Settings → About Phone → Tap "Build Number" 7 times
2. Enable **USB Debugging**: Settings → Developer Options → USB Debugging
3. Connect device via USB cable
4. Click **Run** in Android Studio
5. Select your device
6. App will install and launch

## Testing Content Loading

### Testing Announcements, Quizzes, Formulas & Articles

**Prerequisite**: Ensure Firestore Database is configured and has data in these collections:
- `announcements` - Contains FirestoreAnnouncement documents
- `quizzes` - Contains FirestoreQuiz documents with questions
- `formulas` - Contains FirestoreFormula documents
- `articles` - Contains FirestoreArticle documents

**Steps to Test**:
1. Launch the app on emulator/device
2. Navigate to Home screen
3. Scroll down to see content sections:
   - ✅ **Announcements Section** - Shows 5 latest announcements
   - ✅ **Quizzes Section** - Shows all quizzes with play button
   - ✅ **Formulas Section** - Shows all formula cards
   - ✅ **Articles Section** - Shows all articles with expand/collapse

### Testing Quiz Player
1. Click on any quiz card in the home screen
2. Quiz dialog should open with:
   - ✅ Progress bar
   - ✅ Question display
   - ✅ Answer options (A, B, C, D)
   - ✅ "Check Answer" button
   - ✅ Correct/incorrect answer feedback
   - ✅ Final score screen with icons

### Testing Error Handling
1. Disconnect device from internet (airplane mode)
2. Close and restart the app
3. You should see:
   - ✅ Error message cards for failed content sections
   - ✅ "Retry" buttons to reload content
   - ✅ Graceful error messages

## Troubleshooting

### Build Fails
```bash
# Clean and rebuild
.\gradlew.bat clean build

# If still fails, try
.\gradlew.bat clean assembleDebug --stacktrace
```

### App Crashes on Startup
- Check logcat: **View** → **Tool Windows** → **Logcat**
- Look for red error messages
- Ensure Firebase is properly initialized in MainActivity

### Content Not Showing
1. Check Firestore collections exist and have data
2. Check Firebase permissions allow reading
3. Check internet connection (WiFi or USB internet sharing)
4. Look at error messages in app - tap "Retry" to reload

### Quiz Player Crashes
- Ensure quiz documents have valid `questions` array
- Each question must have: `question`, `options`, `correctAnswerIndex`
- Use error message displayed to debug specific issues

## App Features Verified ✅

### Home Screen
- ✅ Welcome message with username
- ✅ 13 AI tool cards in grid layout
- ✅ Admin-managed content sections (Announcements, Quizzes, Formulas, Articles)
- ✅ Error handling and retry buttons

### Student Content Viewer
- ✅ Announcements with expandable cards
- ✅ Articles with read more functionality
- ✅ Quizzes with interactive player
- ✅ Formulas with category badges

### Quiz Player
- ✅ Question progress indicator
- ✅ Multiple choice options
- ✅ Real-time feedback
- ✅ Score calculation
- ✅ Retry button
- ✅ Close button

### Error Handling
- ✅ Firestore connection errors caught
- ✅ Empty content handled gracefully
- ✅ User-friendly error messages
- ✅ Retry functionality

## Firebase Setup Required

Add test data to Firestore with this structure:

**Announcements Collection**
```
{
  title: "Welcome!",
  content: "Welcome to AIZAYA",
  timestamp: 1234567890000
}
```

**Quizzes Collection**
```
{
  title: "Math Quiz",
  description: "Test your math skills",
  category: "Mathematics",
  questions: [
    {
      question: "What is 2 + 2?",
      options: ["3", "4", "5", "6"],
      correctAnswerIndex: 1
    }
  ]
}
```

**Formulas Collection**
```
{
  title: "Pythagorean Theorem",
  formula: "a² + b² = c²",
  category: "Geometry"
}
```

**Articles Collection**
```
{
  title: "Getting Started",
  content: "Article content here",
  author: "Admin"
}
```

## Performance Tips

1. **Disable Animations** during testing:
   - Settings → Developer Options → Animation Scale: 0.5x
   
2. **Use Pixel 5 or Pixel 6 emulator** (better performance)

3. **Allocate more RAM to emulator**:
   - Device Manager → Edit → RAM: 4GB+

## Next Steps

1. ✅ Build the app with `./gradlew clean assembleDebug`
2. ✅ Test on emulator or device
3. ✅ Add sample data to Firestore
4. ✅ Verify all content displays correctly
5. ✅ Test error scenarios (offline mode)
6. ✅ Ready for deployment!

## Support

For issues or errors:
1. Check the Logcat for error details
2. Verify Firebase configuration
3. Ensure all dependencies are installed
4. Check Firestore security rules
5. Verify network connectivity

