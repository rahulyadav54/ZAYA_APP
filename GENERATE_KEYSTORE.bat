@echo off
REM Generate keystore for AIZAYA PlayStore signing

setlocal enabledelayedexpansion

echo.
echo ============================================
echo AIZAYA Keystore Generator
echo ============================================
echo.

REM Try to find keytool from Java
set KEYTOOL=keytool.exe
for /d %%d in ("C:\Program Files\Java\jdk*") do (
    if exist "%%d\bin\keytool.exe" (
        set KEYTOOL=%%d\bin\keytool.exe
        goto :found_keytool
    )
)

for /d %%d in ("C:\Program Files\Android\Android Studio\jbr") do (
    if exist "%%d\bin\keytool.exe" (
        set KEYTOOL=%%d\bin\keytool.exe
        goto :found_keytool
    )
)

echo ERROR: keytool not found!
echo Please ensure Java JDK is installed
exit /b 1

:found_keytool
echo Using keytool: %KEYTOOL%

set KEYSTORE_PATH=%CD%\aizaya-keystore.jks

if exist "%KEYSTORE_PATH%" (
    echo.
    echo Keystore already exists at: %KEYSTORE_PATH%
    echo.
    echo Options:
    echo 1. Delete existing keystore and create new one (NOT RECOMMENDED!)
    echo 2. Keep existing keystore (RECOMMENDED)
    echo 3. Exit
    echo.
    set /p CHOICE="Enter your choice (1-3): "

    if "!CHOICE!"=="1" (
        del "%KEYSTORE_PATH%"
        echo Deleted existing keystore.
    ) else if "!CHOICE!"=="2" (
        echo Keeping existing keystore.
        echo Your keystore is ready to use!
        echo.
        echo ⚠️  REMEMBER:
        echo - Store the keystore password safely
        echo - Do NOT share this file publicly
        echo - You need it for all future app updates
        echo.
        exit /b 0
    ) else (
        echo Exiting.
        exit /b 0
    )
)

echo.
echo This will create a new signing keystore for PlayStore.
echo.
echo ⚠️  IMPORTANT:
echo - You will be asked for a PASSWORD - WRITE IT DOWN!
echo - You need this password for PlayStore uploads
echo - Losing this keystore means you can't update your app
echo.
echo Press Enter when ready to continue...
pause

echo.
echo Generating keystore...
echo (This may take a moment...)
echo.

"%KEYTOOL%" -genkey -v -keystore "%KEYSTORE_PATH%" ^
    -keyalg RSA ^
    -keysize 2048 ^
    -validity 9125 ^
    -alias aizaya-key

if %ERRORLEVEL% equ 0 (
    echo.
    echo ============================================
    echo ✓ Keystore generated successfully!
    echo ============================================
    echo.
    echo Keystore location: %KEYSTORE_PATH%
    echo Alias: aizaya-key
    echo.
    echo ⚠️  IMPORTANT - SAVE YOUR PASSWORD!
    echo.
    echo Next steps:
    echo 1. Remember your keystore PASSWORD
    echo 2. Make a backup of: aizaya-keystore.jks
    echo 3. Store in a safe location
    echo 4. Run BUILD_RELEASE.bat to build the app
    echo 5. Upload to PlayStore
    echo.
    echo ✓ You're ready for PlayStore!
    echo.
) else (
    echo.
    echo ❌ Error generating keystore!
    echo Please try again or check Java installation.
    echo.
    exit /b 1
)

