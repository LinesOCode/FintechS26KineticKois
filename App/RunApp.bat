@echo off
setlocal
:: Force the script to run out of the folder it is saved in
cd /d "%~dp0"

echo =========================================
echo Fintech S26 Kinetic Kois Trading App
echo =========================================
echo.

:: Step 1: set up the JDK 26 runtime before starting the app processes
set "JAVA_BIN_DIR="

if exist ".\openjdk-26.0.2_windows-x64_bin\bin\java.exe" (
    set "JAVA_BIN_DIR=.\openjdk-26.0.2_windows-x64_bin\bin"
) else if exist ".\openjdk-26.0.2_windows-x64_bin\jdk-26.0.2\bin\java.exe" (
    set "JAVA_BIN_DIR=.\openjdk-26.0.2_windows-x64_bin\jdk-26.0.2\bin"
)

if not defined JAVA_BIN_DIR (
    for /r %%i in (java.exe) do (
        if not defined JAVA_BIN_DIR if exist "%%~fi" (
            set "JAVA_BIN_DIR=%%~dpi"
        )
    )
)

if not defined JAVA_BIN_DIR (
    echo [ERROR] JDK 26 could not be found.
    echo Please ensure the openjdk-26.0.2_windows-x64_bin folder is present next to this launcher.
    echo.
    pause
    exit /b 1
)

set "JAVA_HOME=%~dp0%JAVA_BIN_DIR%"
set "PATH=%JAVA_HOME%;%PATH%"

echo Using Java runtime from: "%JAVA_HOME%"
echo.

:: Step 2: launch the app processes after the JDK is configured
"%JAVA_HOME%\java.exe" -jar KineticKois.jar
if errorlevel 1 (
    echo.
    echo [ERROR] KineticKois.jar failed to launch.
    pause
    exit /b 1
)

echo.
echo Running Simulator...
"%JAVA_HOME%\java.exe" -cp . Simulator
if errorlevel 1 (
    echo.
    echo [ERROR] Simulator failed to start.
    pause
    exit /b 1
)

echo.
echo Running UI...
"%JAVA_HOME%\java.exe" -cp . UI
if errorlevel 1 (
    echo.
    echo [ERROR] UI failed to start.
    pause
    exit /b 1
)

echo.
pause
