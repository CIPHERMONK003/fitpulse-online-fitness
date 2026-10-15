@echo off
setlocal
echo ==================================================================
echo   Starting FitPulse Online Fitness Coaching Platform...
echo ==================================================================
cd /d "%~dp0"
call ..\maven\apache-maven-3.9.6\bin\mvn.cmd exec:java
pause
