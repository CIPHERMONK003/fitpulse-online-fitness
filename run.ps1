# FitPulse Online Fitness Coaching Platform Launcher
$PSScriptRoot = Split-Path -Parent -Path $MyInvocation.MyCommand.Definition
Set-Location -Path $PSScriptRoot
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "   Starting FitPulse Online Fitness Coaching Platform...        " -ForegroundColor Green
Write-Host "==================================================================" -ForegroundColor Cyan

$mvnPath = Join-Path (Split-Path -Parent $PSScriptRoot) "maven\apache-maven-3.9.6\bin\mvn.cmd"
if (Test-Path $mvnPath) {
    & $mvnPath exec:java
} else {
    mvn exec:java
}
