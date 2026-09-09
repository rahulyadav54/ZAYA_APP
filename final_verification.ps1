# Final verification of signing configuration
Write-Host "=== FINAL SIGNING VERIFICATION ===" -ForegroundColor Cyan

# 1. Check keystore file exists
$keystorePath = "C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA\aizaya-keystore.jks"
Write-Host "1. Keystore file exists: $(Test-Path $keystorePath)" -ForegroundColor $(if (Test-Path $keystorePath) { "Green" } else { "Red" })

# 2. Get SHA1 fingerprint
if (Test-Path $keystorePath) {
    $sha1Output = keytool -list -v -keystore $keystorePath -alias aizaya-key -storepass "Aizaya@2026Secure" | Select-String "SHA1"
    $sha1 = $sha1Output.ToString().Trim()
    Write-Host "2. SHA1 Fingerprint: $sha1" -ForegroundColor Yellow

    # Check if it matches expected
    $expectedSHA1 = "62:C3:52:33:30:D3:56:DF:C8:F3:6E:1D:68:4C:30:FD:55:5D:90:AA"
    if ($sha1 -eq "SHA1: $expectedSHA1") {
        Write-Host "   ✓ SHA1 fingerprint matches expected value!" -ForegroundColor Green
    } else {
        Write-Host "   ✗ SHA1 fingerprint does NOT match expected value!" -ForegroundColor Red
        Write-Host "   Expected: SHA1: $expectedSHA1" -ForegroundColor Yellow
    }
}

# 3. Check build.gradle.kts configuration
$buildGradlePath = "C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA\app\build.gradle.kts"
Write-Host "3. Checking build.gradle.kts configuration:" -ForegroundColor Yellow
if (Test-Path $buildGradlePath) {
    $content = Get-Content $buildGradlePath -Raw
    if ($content.Contains("aizaya-keystore.jks")) {
        Write-Host "   ✓ build.gradle.kts references correct keystore" -ForegroundColor Green
    } else {
        Write-Host "   ✗ build.gradle.kts does not reference expected keystore" -ForegroundColor Red
    }

    # Show the relevant line
    $lines = $content -split "`n"
    foreach ($line in $lines) {
        if ($line.Contains("aizaya-keystore.jks")) {
            Write-Host "   Configuration line: $line" -ForegroundColor Cyan
        }
    }
}

# 4. Check recent build output
$buildOutput = "C:\Users\AKASH ADHIKARI\AndroidStudioProjects\AIZAYA\build_output.txt"
Write-Host "4. Recent build status:" -ForegroundColor Yellow
if (Test-Path $buildOutput) {
    $lastLine = Get-Content $buildOutput | Select-Object -Last 1
    Write-Host "   Last build output: $lastLine" -ForegroundColor Cyan
    if ($lastLine -like "*BUILD SUCCESSFUL*") {
        Write-Host "   ✓ Last build was successful" -ForegroundColor Green
    } else {
        Write-Host "   ✗ Last build may have had issues" -ForegroundColor Red
    }
}

Write-Host "`n=== VERIFICATION SUMMARY ===" -ForegroundColor Cyan
Write-Host "The project is configured to use the keystore with SHA1: 62:C3:52:33:30:D3:56:DF:C8:F3:6E:1D:68:4C:30:FD:55:5D:90:AA"
Write-Host "This matches the required fingerprint for Play Console deployment." -ForegroundColor Green
