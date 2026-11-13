@echo off
echo ========================================
echo    CRM Backend Restart Script
echo ========================================
echo.

echo Step 1: Stopping existing backend...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8082 ^| findstr LISTENING') do (
    echo   Killing process %%a
    taskkill /F /PID %%a >nul 2>&1
)
timeout /t 2 >nul

echo.
echo Step 2: Cleaning and compiling...
call mvn clean compile -DskipTests
if %errorlevel% neq 0 (
    echo   Compilation FAILED!
    pause
    exit /b 1
)

echo.
echo Step 3: Starting backend...
echo ========================================
echo Backend is starting...
echo Watch for: "Started CrmBackendApplication"
echo Press Ctrl+C to stop
echo ========================================
echo.

call mvn spring-boot:run
