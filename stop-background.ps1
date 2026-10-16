# ==============================================================================
# FitPulse Background Process Stopper
# Gracefully terminates background Java and Cloudflared instances.
# ==============================================================================

Write-Host "Stopping Cloudflare Tunnel..." -ForegroundColor Yellow
Get-Process -Name "cloudflared" -ErrorAction SilentlyContinue | Stop-Process -Force

Write-Host "Stopping FitPulse Java Server..." -ForegroundColor Yellow
$javaProcesses = Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" | Where-Object { $_.CommandLine -like "*fitpulse-global-app.jar*" }
foreach ($p in $javaProcesses) {
    Stop-Process -Id $p.ProcessId -Force
}

Write-Host "FitPulse background processes have been stopped." -ForegroundColor Green
