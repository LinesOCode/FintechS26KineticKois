@echo off
:: Force the script to run out of the folder it is saved in
cd /d "%~dp0"

echo =========================================
echo Fintech S26 Kinetic Kois Trading App
echo =========================================
echo.

:: Check Path 1 (Direct bin layout)
if exist ".\openjdk-26.0.2_windows-x64_bin\bin\java.exe" (
    ".\openjdk-26.0.2_windows-x64_bin\bin\java.exe" -jar KineticKois.jar
    goto end
)

:: Check Path 2 (Nested jdk-26.0.2 layout)
if exist ".\openjdk-26.0.2_windows-x64_bin\jdk-26.0.2\bin\java.exe" (
    ".\openjdk-26.0.2_windows-x64_bin\jdk-26.0.2\bin\java.exe" -jar KineticKois.jar
    goto end
)

:: Check Path 3 (Deep lookup fallback)
for /r %%i in (java.exe) do (
    if exist "%%i" (
        "%%i" -jar KineticKois.jar
        goto end
    )
)

echo [ERROR] Java could not be found anywhere in this directory tree.
echo Please ensure your 'openjdk-26.0.2_windows-x64_bin' folder contains java.exe.

:end
echo.
pause
