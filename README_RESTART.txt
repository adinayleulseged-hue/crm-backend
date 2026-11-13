=====================================
   RESTART BACKEND - 3 EASY STEPS
=====================================

The "Create User" button doesn't work because your backend
is still running OLD CODE that blocks the endpoint.

You MUST restart the backend for the fix to work!


STEP 1: Open PowerShell
------------------------
Press Windows Key + X
Click "Windows PowerShell" or "Terminal"


STEP 2: Go to Backend Folder
-----------------------------
cd C:\Users\PAVILION\Downloads\crm-backend\crm-backend


STEP 3: Run Restart Script
---------------------------
.\RESTART-BACKEND.bat


WAIT FOR THIS MESSAGE:
----------------------
"Started CrmBackendApplication in X seconds"


THEN TEST:
----------
1. Go back to your browser
2. Refresh the page (Ctrl + Shift + R)
3. Login as admin
4. Click "Add New User"
5. Fill the form
6. Click "Create User"
7. Should work now! ✓


ALTERNATIVE (Manual Method):
----------------------------
If script doesn't work:

1. Stop backend: Press Ctrl+C in the terminal where it's running
2. Kill port: 
   for /f "tokens=5" %a in ('netstat -aon ^| findstr :8082') do taskkill /F /PID %a
3. Recompile: mvn clean compile -DskipTests
4. Start: mvn spring-boot:run
5. Wait for "Started CrmBackendApplication"


VERIFY IT WORKED:
-----------------
Open: http://localhost:8082/actuator/health

Should show: {"status":"UP"}

If not, backend is not running!


COMMON ERRORS:
--------------
"mvn not found" → Maven not installed/not in PATH
"Port already in use" → Kill the process using Step 2 above
"Connection refused" → Backend not started yet, wait more


STILL NOT WORKING?
------------------
Run this test script:
.\TEST-ENDPOINTS.ps1

It will tell you exactly what's wrong!


=====================================
        DON'T SKIP THE RESTART!
     The code changes REQUIRE it!
=====================================
