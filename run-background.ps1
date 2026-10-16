# ==============================================================================
# FitPulse Silent Background Runner (PowerShell)
# Runs the Java Application Server and Cloudflare Tunnel invisibly in the background.
# ==============================================================================

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $projectDir

# 1. Check if Java app is already running on port 8080
$portInUse = $false
try {
    $conn = Test-NetConnection -ComputerName 127.0.0.1 -Port 8080 -WarningAction SilentlyContinue
    $portInUse = $conn.TcpTestSucceeded
} catch {
    $portInUse = $false
}

if (-not $portInUse) {
    Write-Host "Starting FitPulse Java App in background..." -ForegroundColor Cyan
    Start-Process -FilePath "java" -ArgumentList "-jar", "target\fitpulse-global-app.jar" -WorkingDirectory $projectDir -WindowStyle Hidden
    Start-Sleep -Seconds 4
} else {
    Write-Host "FitPulse Java App is already running on port 8080." -ForegroundColor Green
}

# 2. Check if cloudflared is already running
$cfProcess = Get-Process -Name "cloudflared" -ErrorAction SilentlyContinue
if (-not $cfProcess) {
    Write-Host "Starting Cloudflare Tunnel in background..." -ForegroundColor Cyan
    $logPath = Join-Path $projectDir "tunnel.log"
    Start-Process -FilePath ".\cloudflared.exe" -ArgumentList "tunnel", "--url", "http://localhost:8080", "--logfile", $logPath -WorkingDirectory $projectDir -WindowStyle Hidden
    Start-Sleep -Seconds 5
} else {
    Write-Host "Cloudflare Tunnel is already running." -ForegroundColor Green
}

# 3. Read current public URL from tunnel.log if available
$logPath = Join-Path $projectDir "tunnel.log"
if (Test-Path $logPath) {
    $match = Get-Content $logPath | Select-String -Pattern "https://[a-zA-Z0-9-]+\.trycloudflare\.com" | Select-Object -Last 1
    if ($match) {
        Write-Host "Live Global URL: $($match.Matches[0].Value)" -ForegroundColor Yellow
    }
}
Write-Host "Both processes are running silently in the background!" -ForegroundColor Green
