# Verify signing configuration
Write-Host "=== Verifying Signing Configuration ===" -ForegroundColor Green

# Check keystore file exists
$keystorePath = Join-Path $PSScriptRoot "aizaya-keystore.jks"
Write-Host "Keystore path: $keystorePath"
Write-Host "Keystore exists: $(Test-Path $keystorePath)"

if (Test-Path $keystorePath) {
    # Get SHA1 fingerprint
    $sha1 = (keytool -list -v -keystore $keystorePath -alias aizaya-key -storepass "Aizaya@2026Secure" | Select-String "SHA1").ToString().Trim()
    Write-Host "SHA1 Fingerprint: $sha1"

    # Check if it matches expected
    $expectedSHA1 = "62:C3:52:33:30:D3:56:DF:C8:F3:6E:1D:68:4C:30:FD:55:5D:90:AA"
    if ($sha1 -eq $expectedSHA1) {
        Write-Host "✓ SHA1 fingerprint matches expected value!" -ForegroundColor Green
    } else {
        Write-Host "✗ SHA1 fingerprint does NOT match expected value!" -ForegroundColor Red
        Write-Host "Expected: $expectedSHA1" -ForegroundColor Yellow
    }
}

# Check build.gradle.kts configuration
$buildGradlePath = Join-Path $PSScriptRoot "app\build.gradle.kts"
Write-Host "`n=== Checking build.gradle.kts ===" -ForegroundColor Green
if (Test-Path $buildGradlePath) {
    $content = Get-Content $buildGradlePath -Raw
    if ($content -like "*aizaya-keystore.jks*") {
        Write-Host "✓ build.gradle.kts references correct keystore" -ForegroundColor Green
    } else {
        Write-Host "✗ build.gradle.kts does not reference expected keystore" -ForegroundColor Red
    }
}

Write-Host "`n=== Verification Complete ===" -ForegroundColor Green
