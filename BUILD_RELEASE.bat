@echo off
REM Build script for AIZAYA PlayStore Release
REM This script sets up the environment and builds the release APK/Bundle

setlocal enabledelayedexpansion

echo.
echo ============================================
echo AIZAYA PlayStore Release Build
echo ============================================
echo.

REM Try to find Java
set JAVA_HOME=
for /d %%d in ("C:\Program Files\Java\jdk*") do (
    set JAVA_HOME=%%d
    goto :found_java
)

for /d %%d in ("C:\Program Files\Android\Android Studio\jbr") do (
    set JAVA_HOME=%%d
    goto :found_java
)

echo ERROR: Java Development Kit not found!
echo Please install Android Studio or JDK 11+
exit /b 1

:found_java
echo Found Java: %JAVA_HOME%
set PATH=%JAVA_HOME%\bin;%PATH%

echo.
echo Step 1: Cleaning project...
call gradlew clean

echo.
echo Step 2: Building release APK...
call gradlew assembleRelease

echo.
echo Step 3: Building release Bundle (AAB for PlayStore)...
call gradlew bundleRelease

echo.
echo ============================================
echo Build Complete!
echo ============================================
echo.
echo Output files:
echo - APK: app\build\outputs\apk\release\app-release.apk
echo - Bundle: app\build\outputs\bundle\release\app-release.aab
echo.
echo Next Steps:
echo 1. Generate keystore (if not done): run GENERATE_KEYSTORE.bat
echo 2. Use app-release.aab to upload to PlayStore
echo 3. Read PLAYSTORE_QUICK_START.md for next steps
echo.

