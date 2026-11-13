# ⚠️ Fix: "Too Many Clients Already" PostgreSQL Error

## 🔴 Problem
PostgreSQL is showing: **"FATAL: sorry, too many clients already"**

This means PostgreSQL has reached its maximum connection limit (default is usually 100).

---

## ✅ Solution Options

### **Option 1: Restart PostgreSQL (Recommended - Fastest)**

**On Windows:**
```powershell
# Stop PostgreSQL service
Stop-Service -Name "postgresql*" -Force

# Wait 5 seconds
Start-Sleep -Seconds 5

# Start PostgreSQL service
Start-Service -Name "postgresql*"
```

**Or using pgAdmin:**
1. Right-click on the PostgreSQL server
2. Click "Disconnect"
3. Wait 5 seconds
4. Right-click again → "Connect Server"

**Or using Services (Windows GUI):**
1. Press `Win + R`, type `services.msc`, press Enter
2. Find "postgresql" service
3. Right-click → "Restart"

---

### **Option 2: Kill Connections Manually**

If you can connect to PostgreSQL (using pgAdmin or another tool), run:

```sql
-- See all active connections
SELECT pid, usename, datname, application_name, client_addr, state
FROM pg_stat_activity
WHERE datname = 'crm_db';

-- Kill all connections to crm_db (CAREFUL!)
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'crm_db'
  AND pid <> pg_backend_pid();
```

---

### **Option 3: Increase PostgreSQL Max Connections**

Edit PostgreSQL config file `postgresql.conf`:

```ini
# Find this line:
max_connections = 100

# Change to:
max_connections = 200
```

**Then restart PostgreSQL service.**

**Config file location (Windows):**
- Usually: `C:\Program Files\PostgreSQL\17\data\postgresql.conf`
- Or check in pgAdmin under server properties

---

## 🔧 What I Already Fixed

✅ **Reduced connection pool size** in `application.properties`:
```properties
spring.datasource.hikari.maximum-pool-size=5  # Was 10
spring.datasource.hikari.minimum-idle=2       # Was 5
```

✅ **Your password is correct:** `si7a14ti21$`

---

## 🚀 Steps to Get Running

### **Quick Steps:**

1. **Restart PostgreSQL** (use Option 1 above)

2. **Wait 10 seconds**

3. **Start the backend:**
   ```powershell
   cd C:\Users\PAVILION\Downloads\crm-backend\crm-backend
   mvn spring-boot:run
   ```

4. **Check it works:**
   - Backend should start without errors
   - Should see: "Started CrmBackendApplication"

---

## 🔍 Why This Happened

**Possible causes:**
- Backend crashed without releasing connections
- Multiple backend instances running
- Other applications using PostgreSQL
- IDE database connections left open
- Development tools (pgAdmin, DBeaver) with many tabs

**Prevention:**
- ✅ Reduced pool size (done)
- Always stop backend cleanly (Ctrl+C)
- Close unused database tool connections
- Use connection pooling properly

---

## 📞 If Problem Persists

### **Check if multiple backends are running:**
```powershell
Get-Process -Name "java" | Where-Object { $_.CommandLine -like "*crm-backend*" }
```

If you see multiple, kill them:
```powershell
Get-Process -Name "java" | Where-Object { $_.CommandLine -like "*crm-backend*" } | Stop-Process -Force
```

### **Check PostgreSQL connections:**
If you can connect to postgres (not crm_db):
```sql
-- Connect to 'postgres' database first
SELECT count(*) as connections, datname 
FROM pg_stat_activity 
GROUP BY datname;
```

---

## ✅ After Fixing

Once PostgreSQL is restarted:

1. Backend will start successfully
2. It will use only 5 max connections (reduced pool)
3. Lead inquiry system will work
4. Admin dashboard will connect

---

## 🎯 Your Password is Correct!

Your password `si7a14ti21$` is already configured in:
- `crm-backend/src/main/resources/application.properties` (line 12)

The connection error is NOT about the password - it's about PostgreSQL being full.

---

## 🚨 Emergency Option

If nothing works, you can temporarily disable connection pooling:

**In `application.properties`:**
```properties
# Emergency: Set pool to minimum
spring.datasource.hikari.maximum-pool-size=1
spring.datasource.hikari.minimum-idle=1
```

This will be slow but will work with limited connections.

---

## ✅ Summary

**Do this now:**
1. Restart PostgreSQL service (Option 1)
2. Wait 10 seconds
3. The backend will start successfully

**Your credentials are correct - the issue is just too many open connections!**
