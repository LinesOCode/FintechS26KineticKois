@echo off
REM Fintech S26 Kinetic Kois Trading Application Launcher
REM This batch file runs the compiled Java application

echo.
echo =========================================
echo Fintech S26 Kinetic Kois Trading App
echo =========================================
echo.

REM Run the jar file
java -jar KineticKois.jar

if errorlevel 1 (
    echo.
    echo Error: Failed to run application. Make sure Java is installed.
    echo.
    pause
)
