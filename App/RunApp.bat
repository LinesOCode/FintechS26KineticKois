@echo off
:: Force the script to run out of the folder it is saved in
cd /d "%~dp0"

echo =========================================
echo Fintech S26 Kinetic Kois Trading App
echo =========================================
echo.

:: -------------------------------------------------------------
:: FIRST RUN CHECK
:: Triggers GitHub Action ONLY if .first_run_done doesn't exist
:: -------------------------------------------------------------
if not exist ".first_run_done" (
    echo [INFO] First time launching the app. Running initial setup tasks...
    
    :: UNCOMMENT THE LINE BELOW TO ENABLE THE GITHUB TRIGGER
    :: curl -X POST -H "Authorization: Bearer YOUR_SECRET_TOKEN" -H "Accept: application/vnd.github+json" https://github.com -d "{\"event_type\": \"app_opened\"}"
    
    :: Create the hidden flag file so this block never runs again
    echo true > ".first_run_done"
    attrib +h ".first_run_done"
    echo [INFO] Setup complete! Subsequent launches will skip this step.
    echo.
) else (
    echo [INFO] Returning user detected. Skipping cloud sync...
    echo.
)
:: -------------------------------------------------------------

set JAVA_PATH=.\openjdk-26.0.2_windows-x64_bin\bin\java.exe
set ALT_JAVA_PATH=.\openjdk-26.0.2_windows-x64_bin\jdk-26.0.2\bin\java.exe

:: Check Path 1 (Direct bin layout)
if exist "%JAVA_PATH%" (
    set "RUN_JAVA=%JAVA_PATH%"
    goto run_app
)

:: Check Path 2 (Nested jdk-26.0.2 layout)
if exist "%ALT_JAVA_PATH%" (
    set "RUN_JAVA=%ALT_JAVA_PATH%"
    goto run_app
)

:: Check Path 3 (Deep lookup fallback)
for /r %%i in (java.exe) do (
    if exist "%%i" (
        set "RUN_JAVA=%%i"
        goto run_app
    )
)

echo [ERROR] Java could not be found anywhere in this directory tree.
echo Please ensure your 'openjdk-26-0-2_windows-x64_bin' folder contains java.exe.
goto end

:run_app
echo Using Java from: %RUN_JAVA%
"%RUN_JAVA%" -jar KineticKois.jar

echo.
echo Compiling custom simulator and UI files...
:: Dynamically find javac.exe using your working java.exe folder path
set "COMPILER_JAVA=%RUN_JAVA:java.exe=javac.exe%"
"%COMPILER_JAVA%" InvestingSimulator.java SuperStoxUI.java
if %errorlevel% neq 0 (
    echo [ERROR] Code compilation failed. Please verify syntax.
    goto end
)

echo.
echo Running Simulator...
start "" "%RUN_JAVA%" InvestingSimulator

echo.
echo Running UI...
start "" "%RUN_JAVA%" SuperStoxUI

if errorlevel 1 (
    echo.
    echo [ERROR] Java found, but one or more application components failed to launch or crashed.
)

:end
echo.
pause
