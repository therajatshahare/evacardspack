param (
    [string]$ip = ""
)

$ErrorActionPreference = "Stop"

Write-Host "[1/4] Building Release APK using Gradle..." -ForegroundColor Cyan
if (Test-Path ".\gradlew.bat") {
    .\gradlew.bat app:assembleRelease
} else {
    gradlew app:assembleRelease
}

$apkPath = "app\build\outputs\apk\release\app-release.apk"
if (-not (Test-Path $apkPath)) {
    Write-Error "Release APK build failed! Could not find $apkPath"
    exit 1
}

Write-Host "[2/4] Release APK built successfully at $apkPath" -ForegroundColor Green

if ($ip -ne "") {
    Write-Host "[3/4] Connecting to Android TV at $ip..." -ForegroundColor Yellow
    adb connect $ip
    Write-Host "[4/4] Installing APK to Android TV ($ip)..." -ForegroundColor Cyan
    adb -s $ip install -r $apkPath
    Write-Host "Launching Eva Cards Pack Activity on $ip..." -ForegroundColor Green
    adb -s $ip shell am start -n io.rns.evacardspack/.EvaCardActivity
} else {
    Write-Host "[3/4] Installing APK to connected Android TV..." -ForegroundColor Cyan
    adb install -r $apkPath
    Write-Host "Launching Eva Cards Pack Activity..." -ForegroundColor Green
    adb shell am start -n io.rns.evacardspack/.EvaCardActivity
}

Write-Host "Done!" -ForegroundColor Green
