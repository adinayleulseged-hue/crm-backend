# Gmail Email Setup for CRM System

## Current Status
✅ Email configuration is done in `application.properties`
❌ You need a **Gmail App Password** to actually send emails

## Why App Password?
Gmail requires an "App Password" for security when apps access your Gmail account. Your regular Gmail password won't work!

## Step-by-Step: Get Gmail App Password

### Step 1: Enable 2-Factor Authentication
1. Go to: https://myaccount.google.com/security
2. Click **2-Step Verification** (under "Signing in to Google")
3. Click **Get Started** and follow the steps
4. Use your phone number for verification

### Step 2: Generate App Password
1. Go to: https://myaccount.google.com/apppasswords
   - Or search "Google App Passwords" in Google
2. Login if prompted
3. You'll see "App passwords" page
4. Under "Select app", choose **Mail**
5. Under "Select device", choose **Other (Custom name)**
6. Type: **INSA CRM Backend**
7. Click **Generate**
8. **Copy the 16-character password** (example: `abcd efgh ijkl mnop`)
   - It looks like: 4 groups of 4 letters
   - **SAVE THIS!** You won't see it again

### Step 3: Add Password to Backend

**Option A: Using Environment Variable (Recommended)**
1. Open PowerShell as Administrator
2. Run:
```powershell
[System.Environment]::SetEnvironmentVariable('SMTP_PASSWORD', 'your-16-char-password', 'User')
```
Replace `your-16-char-password` with the password you copied (remove spaces!)

3. Restart PowerShell
4. Restart backend

**Option B: Direct in application.properties (Quick Test)**
1. Open: `crm-backend/src/main/resources/application.properties`
2. Find this line:
```properties
spring.mail.password=${SMTP_PASSWORD:your-gmail-app-password}
```
3. Replace `your-gmail-app-password` with your actual password:
```properties
spring.mail.password=abcdefghijklmnop
```
4. **⚠️ IMPORTANT:** Don't commit this file to Git! (add to .gitignore)

### Step 4: Restart Backend
```powershell
cd C:\Users\PAVILION\Downloads\crm-backend\crm-backend
.\RESTART-BACKEND.bat
```

Wait for: `Started CrmBackendApplication`

### Step 5: Test Email
1. Go to admin dashboard
2. Click **"Invite Customer"**
3. Use email: `adey81151@gmail.com` (or any email you want to test)
4. Fill the form
5. Click **"Send Invitation"**
6. **Check your email inbox!** ✉️

## Troubleshooting

### Error: "Username and Password not accepted"
**Cause:** Using regular Gmail password instead of App Password

**Fix:**
1. Make sure you generated an App Password (Step 2 above)
2. Use the 16-character app password, not your regular Gmail password
3. Remove all spaces from the password

### Error: "Authentication failed"
**Cause:** App Password not configured or wrong

**Fix:**
1. Regenerate App Password: https://myaccount.google.com/apppasswords
2. Delete old one, create new one
3. Update in application.properties or environment variable
4. Restart backend

### Error: "Connection timeout"
**Cause:** Firewall blocking port 587

**Fix:**
1. Check Windows Firewall
2. Try port 465 instead:
```properties
spring.mail.port=465
spring.mail.properties.mail.smtp.ssl.enable=true
```

### Error: "Email sent but not received"
**Check:**
1. Spam/Junk folder in Gmail
2. Gmail "All Mail" folder
3. Backend logs for errors

**Backend logs should show:**
```
Attempting to send email to: adey81151@gmail.com
Email sent successfully to: adey81151@gmail.com
```

### No App Passwords Option?
**Cause:** 2-Factor Authentication not enabled

**Fix:**
1. Enable 2FA first: https://myaccount.google.com/security
2. Wait a few minutes
3. Try accessing App Passwords again

## Quick Test Command

After setup, test from PowerShell:
```powershell
$testUser = @{
    username="emailtest"
    email="adey81151@gmail.com"
    role="SALES_OFFICER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/auth/admin/create-user" `
    -Method Post `
    -ContentType "application/json" `
    -Body $testUser
```

**Check your email!** You should receive login credentials.

## Email Template

You'll receive emails like this:

```
Subject: INSA CRM - Your Account Has Been Created

Dear [Your Name],

Your account has been created in the INSA CRM system.

Login Details:
- Username: emailtest
- Email: adey81151@gmail.com
- Temporary Password: Abc12xyz
- Role: SALES_OFFICER

Please login at: http://localhost:3000/role-login

For security reasons, please change your password after your first login.

Best regards,
INSA CRM Team
```

## Security Notes

✅ **Safe:**
- Using App Password (not your main Gmail password)
- Can revoke App Password anytime without changing Gmail password
- App Password only works for this specific purpose

⚠️ **Important:**
- **Never commit** `application.properties` with real password to Git
- Use environment variables in production
- Rotate App Passwords periodically
- Don't share App Password with anyone

## Alternative: Using Another Email Service

If Gmail doesn't work, you can use:

### Microsoft Outlook/Office365
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=your-email@outlook.com
spring.mail.password=your-outlook-password
```

### Custom SMTP Server
```properties
spring.mail.host=smtp.yourcompany.com
spring.mail.port=587
spring.mail.username=noreply@yourcompany.com
spring.mail.password=your-smtp-password
```

## Current Configuration

Your `application.properties` is set to:
- **SMTP Host:** smtp.gmail.com
- **SMTP Port:** 587 (TLS)
- **From Email:** adey81151@gmail.com
- **Password:** `${SMTP_PASSWORD}` (needs to be set!)

## Next Steps

1. ✅ Get Gmail App Password (5 minutes)
2. ✅ Add password to application.properties or environment variable
3. ✅ Restart backend
4. ✅ Test by inviting a customer
5. ✅ Check your email inbox!

---

**Need Help?**
- Can't find App Passwords? Make sure 2FA is enabled first
- Still not working? Check backend logs for detailed error messages
- Email going to spam? Add sender to contacts or mark as "Not Spam"
