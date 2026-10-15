# ==============================================================================
# 🚀 Instant 1-Click Global Publisher for FitPulse Java Platform
# Publishes your local Java app to a live, global HTTPS URL accessible worldwide
# ==============================================================================

$PSScriptRoot = Split-Path -Parent -Path $MyInvocation.MyCommand.Definition
Set-Location -Path $PSScriptRoot

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "   FITPULSE - PUBLISH GLOBALLY TO THE INTERNET                    " -ForegroundColor Green
Write-Host "==================================================================" -ForegroundColor Cyan

# 1. Verify / Build Fat JAR
$jarPath = Join-Path $PSScriptRoot "target\fitpulse-global-app.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "[1/3] Standalone JAR not found. Building with Maven..." -ForegroundColor Yellow
    $mvnPath = Join-Path (Split-Path -Parent $PSScriptRoot) "maven\apache-maven-3.9.6\bin\mvn.cmd"
    if (Test-Path $mvnPath) {
        & $mvnPath clean package -DskipTests
    } else {
        mvn clean package -DskipTests
    }
}

if (-not (Test-Path $jarPath)) {
    Write-Host "[Error] Failed to build $jarPath" -ForegroundColor Red
    exit 1
}

# 2. Check / Download Cloudflare Tunnel Binary (Zero-config, Free Global HTTPS)
$cfExe = Join-Path $PSScriptRoot "cloudflared.exe"
if (-not (Test-Path $cfExe)) {
    Write-Host "[2/3] Downloading lightweight Cloudflare Global Tunnel tool..." -ForegroundColor Yellow
    $cfUrl = "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe"
    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $cfUrl -OutFile $cfExe -UseBasicParsing
        Write-Host "✓ Cloudflare Tunnel downloaded successfully." -ForegroundColor Green
    } catch {
        Write-Host "[Warning] Could not auto-download cloudflared. You can still run with Docker or cloud platforms (Render/Railway)." -ForegroundColor Red
    }
}

# 3. Start Java Web Server
Write-Host "[3/3] Starting FitPulse Enterprise Java Server on port 8080..." -ForegroundColor Cyan
$serverProcess = Start-Process java -ArgumentList "-jar", "`"$jarPath`"" -PassThru -NoNewWindow

Start-Sleep -Seconds 3

# 4. Start Global Tunnel
if (Test-Path $cfExe) {
    Write-Host "`n==================================================================" -ForegroundColor Green
    Write-Host "   GENERATING LIVE GLOBAL HTTPS URL...                          " -ForegroundColor Yellow
    Write-Host "   (Look for the URL ending with '.trycloudflare.com' below)     " -ForegroundColor White
    Write-Host "==================================================================`n" -ForegroundColor Green

    try {
        & $cfExe tunnel --url http://localhost:8080
    } finally {
        Write-Host "`nStopping background Java server process..." -ForegroundColor Yellow
        if ($serverProcess -and -not $serverProcess.HasExited) {
            Stop-Process -Id $serverProcess.Id -Force
        }
    }
} else {
    Write-Host "`nServer is running locally at http://localhost:8080." -ForegroundColor Cyan
    Write-Host "To publish to the cloud, use Docker or push to GitHub + Render (see README.md)." -ForegroundColor White
    Wait-Process -Id $serverProcess.Id
}
