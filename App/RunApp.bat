@echo off
:: Force the script to run out of the folder it is saved in
cd /d "%~dp0"

echo =========================================
echo Fintech S26 Kinetic Kois Trading App
echo =========================================
echo.

<<<<<<< HEAD
:: Check Path 1 (Direct bin layout)
if exist ".\openjdk-26.0.2_windows-x64_bin\bin\java.exe" (
    ".\openjdk-26.0.2_windows-x64_bin\bin\java.exe" -jar KineticKois.jar
    goto end
)

:: Check Path 2 (Nested jdk-26.0.2 layout)
if exist ".\openjdk-26.0.2_windows-x64_bin\jdk-26.0.2\bin\java.exe" (
    ".\openjdk-26.0.2_windows-x64_bin\jdk-26.0.2\bin\java.exe" -jar KineticKois.jar
    goto end
=======
set JAVA_PATH=.\openjdk-26.0.2_windows-x64_bin\bin\java.exe
set ALT_JAVA_PATH=.\openjdk-26.0.2_windows-x64_bin\jdk-26.0.2\bin\java.exe

if exist "%JAVA_PATH%" (
    "%JAVA_PATH%" -jar KineticKois.jar
    echo.
    echo Running Simulator...
    "%JAVA_PATH%" -cp . Simulator
    echo.
    echo Running UI...
    "%JAVA_PATH%" -cp . UI
) else if exist "%ALT_JAVA_PATH%" (
    "%ALT_JAVA_PATH%" -jar KineticKois.jar
    echo.
    echo Running Simulator...
    "%ALT_JAVA_PATH%" -cp . Simulator
    echo.
    echo Running UI...
    "%ALT_JAVA_PATH%" -cp . UI
) else (
    echo [ERROR] Cannot find Java. Please ensure the openjdk folder is unzipped.
)

if errorlevel 1 (
    echo.
    echo [ERROR] Java found, but KineticKois.jar, Simulator, or UI failed to launch or crashed.
<<<<<<< HEAD
>>>>>>> bb8827446c536c9367b6d8d12029e60594807f1f
=======
>>>>>>> bb8827446c536c9367b6d8d12029e60594807f1f
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
