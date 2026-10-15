@echo off
setlocal
echo ==================================================================
echo   Publishing FitPulse Globally (Live HTTPS Web URL)...
echo ==================================================================
cd /d "%~dp0"
powershell -ExecutionPolicy Bypass -File "publish-global.ps1"
pause
