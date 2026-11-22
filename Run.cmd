@echo off
cd /d "%~dp0"
where java >nul 2>&1
if errorlevel 1 (
  echo Java 17 or newer is required. Install a JDK and add its bin folder to PATH.
  pause
  exit /b 1
)
if not exist "dist\Structura.jar" (
  powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0build.ps1"
  if errorlevel 1 (
    pause
    exit /b 1
  )
)
java -jar "dist\Structura.jar"
if errorlevel 1 pause
