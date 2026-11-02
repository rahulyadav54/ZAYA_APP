# Final verification of signing configuration
Write-Host "=== FINAL SIGNING VERIFICATION ===" -ForegroundColor Cyan

# 1. Check keystore file exists
$keystorePath = Join-Path $PSScriptRoot "aizaya-keystore.jks"
$keystoreExists = Test-Path $keystorePath
Write-Host "1. Keystore file exists: $keystoreExists" -ForegroundColor $(if ($keystoreExists) { "Green" } else { "Yellow" })

# 2. Get SHA1 fingerprint
if ($keystoreExists) {
    $sha1Output = keytool -list -v -keystore $keystorePath -alias aizaya-key -storepass "Aizaya@2026Secure" | Select-String "SHA1"
    $sha1 = $sha1Output.ToString().Trim()
    Write-Host "2. SHA1 Fingerprint: $sha1" -ForegroundColor Yellow

    # Check if it matches expected
    $expectedSHA1 = "62:C3:52:33:30:D3:56:DF:C8:F3:6E:1D:68:4C:30:FD:55:5D:90:AA"
    if ($sha1 -eq "SHA1: $expectedSHA1") {
        Write-Host "   [PASS] SHA1 fingerprint matches expected value!" -ForegroundColor Green
    } else {
        Write-Host "   [WARN] SHA1 fingerprint does NOT match expected value!" -ForegroundColor Red
        Write-Host "   Expected: SHA1: $expectedSHA1" -ForegroundColor Yellow
    }
} else {
    Write-Host "2. Keystore check skipped (not yet generated - required only for Play Store release)" -ForegroundColor Yellow
}

# 3. Check build.gradle.kts configuration
$buildGradlePath = Join-Path $PSScriptRoot "app\build.gradle.kts"
Write-Host "3. Checking build.gradle.kts configuration:" -ForegroundColor Yellow
if (Test-Path $buildGradlePath) {
    $content = Get-Content $buildGradlePath -Raw
    if ($content.Contains("aizaya-keystore.jks")) {
        Write-Host "   [PASS] build.gradle.kts references correct keystore" -ForegroundColor Green
    } else {
        Write-Host "   [WARN] build.gradle.kts does not reference expected keystore" -ForegroundColor Red
    }
}

# 4. Check recent build output
$apkDebug = Join-Path $PSScriptRoot "app\build\outputs\apk\debug\app-debug.apk"
Write-Host "4. Build output status:" -ForegroundColor Yellow
if (Test-Path $apkDebug) {
    $item = Get-Item $apkDebug
    $sizeMB = [math]::Round($item.Length / 1MB, 2)
    Write-Host "   [PASS] Debug APK exists: $($item.FullName) ($sizeMB MB)" -ForegroundColor Green
} else {
    Write-Host "   [FAIL] Debug APK not found" -ForegroundColor Red
}

Write-Host "`n=== Verification Complete ===" -ForegroundColor Cyan
