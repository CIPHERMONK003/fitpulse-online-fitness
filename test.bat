@echo off
setlocal
echo ==================================================================
echo   Running FitPulse Comprehensive JUnit Test Suite...
echo ==================================================================
cd /d "%~dp0"
call ..\maven\apache-maven-3.9.6\bin\mvn.cmd test
pause
