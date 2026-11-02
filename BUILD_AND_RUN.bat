@echo off
REM Build and Run script for AIZAYA App
setlocal enabledelayedexpansion

echo ============================================
echo AIZAYA Debug Build
echo ============================================

REM Add Android platform-tools to PATH if not present
where adb >nul 2>nul
if %ERRORLEVEL% neq 0 (
    if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
        set "PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools;%PATH%"
    )
)

call gradlew.bat assembleDebug

if %ERRORLEVEL% equ 0 (
    echo.
    echo ============================================
    echo Build Successful!
    echo Output: app\build\outputs\apk\debug\app-debug.apk
    echo ============================================
    echo.
    where adb >nul 2>nul
    if %ERRORLEVEL% equ 0 (
        echo Checking for connected Android devices...
        adb devices
        echo.
        set /p INSTALL="Install to connected device? (Y/N): "
        if /i "!INSTALL!"=="Y" (
            echo Installing APK...
            adb install -r app\build\outputs\apk\debug\app-debug.apk
            echo Launching com.zayaai.app...
            adb shell monkey -p com.zayaai.app -c android.intent.category.LAUNCHER 1
        )
    )
) else (
    echo.
    echo Build failed with error code %ERRORLEVEL%.
)
