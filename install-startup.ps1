# ==============================================================================
# Install FitPulse to Windows Startup
# Makes FitPulse start silently in the background whenever Windows boots or logs in.
# ==============================================================================

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$targetScript = Join-Path $projectDir "start-silent.vbs"

$startupFolder = [Environment]::GetFolderPath("Startup")
$shortcutPath = Join-Path $startupFolder "FitPulse-Background.lnk"

$wshShell = New-Object -ComObject WScript.Shell
$shortcut = $wshShell.CreateShortcut($shortcutPath)
$shortcut.TargetPath = "wscript.exe"
$shortcut.Arguments = "`"$targetScript`""
$shortcut.WorkingDirectory = $projectDir
$shortcut.Description = "FitPulse Background Auto-Starter"
$shortcut.Save()

Write-Host "SUCCESS: FitPulse has been registered in Windows Startup!" -ForegroundColor Green
Write-Host "Location: $shortcutPath" -ForegroundColor Cyan
Write-Host "FitPulse will now automatically launch in the background whenever you log into Windows." -ForegroundColor Green
