@echo off
cd /d "%~dp0"
echo =========================================
echo Fintech S26 Kinetic Kois Trading App
echo =========================================
echo.

set JAVA_PATH=.\openjdk-26.0.2_windows-x64_bin\bin\java.exe
set ALT_JAVA_PATH=.\openjdk-26.0.2_windows-x64_bin\jdk-26.0.2\bin\java.exe

if exist "%JAVA_PATH%" (
    "%JAVA_PATH%" -jar KineticKois.jar
) else if exist "%ALT_JAVA_PATH%" (
    "%ALT_JAVA_PATH%" -jar KineticKois.jar
) else (
    echo [ERROR] Cannot find Java. Please ensure the openjdk folder is unzipped.
)

if errorlevel 1 (
    echo.
    echo [ERROR] Java found, but KineticKois.jar failed to launch or crashed.
)
pause
