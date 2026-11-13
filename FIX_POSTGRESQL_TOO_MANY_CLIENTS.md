# 🔧 Fix PostgreSQL "Too Many Clients" Error

## ❌ Error You're Seeing:

```
FATAL: sorry, too many clients already
connection to server at "127.0.0.1", port 5433 failed
```

## ✅ Quick Fix (2 Minutes)

### **Option 1: Restart PostgreSQL Service (Recommended)**

1. **Open PowerShell as Administrator:**
   - Press `Windows Key`
   - Type: `PowerShell`
   - Right-click on "Windows PowerShell"
   - Click **"Run as administrator"**
   - Click "Yes" if prompted

2. **Run this command:**
   ```powershell
   Restart-Service -Name "postgresql-x64-17" -Force
   ```

3. **Wait 5 seconds, then verify it's running:**
   ```powershell
   Get-Service -Name "postgresql-x64-17"
   ```
   
   Should show: **Status: Running**

4. **Done!** Now go back to your application

---

### **Option 2: Using Windows Services GUI**

1. Press `Windows Key + R`
2. Type: `services.msc`
3. Press Enter
4. Scroll down to find: **"postgresql-x64-17 - PostgreSQL Server 17"**
5. Right-click on it
6. Click **"Restart"**
7. Wait for it to say "Running"
8. Done!

---

### **Option 3: Restart from pgAdmin (What You're Using)**

1. In pgAdmin, close the connection dialog (click X or Cancel)
2. **Open Command Prompt as Administrator:**
   - Press `Windows Key`
   - Type: `cmd`
   - Right-click on "Command Prompt"
   - Click **"Run as administrator"**
3. Run:
   ```cmd
   net stop postgresql-x64-17
   net start postgresql-x64-17
   ```
4. Go back to pgAdmin
5. Try connecting again with password: `si7a14ti21$`

---

## 🎯 After Restart

### **Test Connection:**

1. **In pgAdmin:**
   - Click on PostgreSQL 17 server
   - Enter password: `si7a14ti21$`
   - Should connect successfully!

2. **Test from Command Line:**
   ```bash
   psql -h localhost -p 5433 -U postgres -d crm_db
   # Password: si7a14ti21$
   ```

3. **Test from Backend:**
   - Your backend should automatically reconnect
   - Check backend terminal for: "HikariPool connection acquired"

---

## 🔍 Why This Happens

PostgreSQL has a maximum number of client connections (default: 100).

**Common causes:**
1. Backend app crashed without closing connections
2. Multiple apps connecting simultaneously
3. Connection pool not releasing connections
4. Long-running queries holding connections

**Our solution:**
- We configured HikariCP with max 5 connections
- But old zombie connections need to be killed
- Restarting PostgreSQL clears all connections

---

## 🛡️ Prevent Future Issues

### **Already Configured (in application.properties):**

```properties
# HikariCP Connection Pool Settings
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.leak-detection-threshold=60000
```

This limits our backend to only 5 connections maximum.

### **Manual Cleanup (If needed):**

If restart doesn't work, you can kill connections manually:

```sql
-- Connect as postgres superuser
psql -h localhost -p 5433 -U postgres

-- View all connections
SELECT pid, usename, application_name, client_addr, state 
FROM pg_stat_activity 
WHERE datname = 'crm_db';

-- Kill idle connections (if needed)
SELECT pg_terminate_backend(pid) 
FROM pg_stat_activity 
WHERE datname = 'crm_db' 
  AND state = 'idle' 
  AND pid <> pg_backend_pid();
```

---

## ✅ Quick Command Reference

### **Check PostgreSQL Status:**
```powershell
Get-Service postgresql-x64-17
```

### **Start PostgreSQL:**
```powershell
Start-Service postgresql-x64-17
```

### **Stop PostgreSQL:**
```powershell
Stop-Service postgresql-x64-17
```

### **Restart PostgreSQL (Recommended):**
```powershell
Restart-Service postgresql-x64-17 -Force
```

**Note:** All commands need **Administrator privileges**

---

## 🎉 Summary

**To fix right now:**

1. **Close pgAdmin connection dialog**
2. **Open PowerShell as Administrator**
3. **Run:** `Restart-Service -Name "postgresql-x64-17" -Force`
4. **Wait 5 seconds**
5. **Go back to pgAdmin and try connecting**
6. **Password:** `si7a14ti21$`

**That's it! Problem solved!** 🚀

---

## 📞 After Restart

Once PostgreSQL is restarted:

1. ✅ pgAdmin will connect successfully
2. ✅ Backend will reconnect automatically
3. ✅ Frontend will work normally
4. ✅ You can continue testing the pending deals workflow!

**The pending deals are waiting for you in the Admin Dashboard!** 📊
