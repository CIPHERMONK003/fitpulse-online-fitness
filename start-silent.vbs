' ==============================================================================
' FitPulse Zero-Window Background Launcher (VBScript)
' Double-clicking this file starts FitPulse + Cloudflare silently with NO console window.
' ==============================================================================

Set fso = CreateObject("Scripting.FileSystemObject")
projectDir = fso.GetParentFolderName(WScript.ScriptFullName)

Set WshShell = CreateObject("WScript.Shell")
command = "powershell.exe -ExecutionPolicy Bypass -NoProfile -WindowStyle Hidden -File """ & projectDir & "\run-background.ps1"""

' 0 = Hide window completely, False = Do not wait for script to finish
WshShell.Run command, 0, False
