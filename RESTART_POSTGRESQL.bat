@echo off
echo ================================================
echo  Restarting PostgreSQL Service
echo ================================================
echo.
echo Attempting to restart postgresql-x64-17...
echo.

net stop postgresql-x64-17
timeout /t 3 /nobreak >nul
net start postgresql-x64-17

echo.
echo ================================================
echo  PostgreSQL Restart Complete!
echo ================================================
echo.
echo Now you can:
echo 1. Connect to pgAdmin with password: si7a14ti21$
echo 2. Backend will reconnect automatically
echo 3. Check pending deals in Admin Dashboard!
echo.
pause
