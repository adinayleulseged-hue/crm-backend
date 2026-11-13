# 🧪 TESTING INSTRUCTIONS - Email Notifications

## ✅ System Status
- **Backend:** Running on port 8082 ✅
- **Frontend:** Running on port 3000 ✅
- **Email Service:** Gmail SMTP Active ✅
- **Database:** PostgreSQL Connected ✅

---

## 🎯 Quick Test Guide

### 1️⃣ TEST USER CREATION EMAIL (30 seconds)

**Steps:**
1. Open browser: http://localhost:3000/role-login
2. Login as admin:
   - Username: `admin`
   - Password: `admin123` *(or your admin password)*
3. Click "Add New User" button
4. Fill in:
   - Username: `testuser`
   - Email: **YOUR_EMAIL_ADDRESS**
   - Role: Select any role (e.g., SALES_OFFICER)
5. Click "Create User"
6. **CHECK YOUR EMAIL INBOX!** 📧

**Expected Email:**
```
Subject: Welcome to INSA CRM - Your Account Has Been Created

Dear testuser,

Your INSA CRM account has been successfully created!

Login Details:
- Username: testuser
- Email: [your email]
- Temporary Password: [8 characters]
- Role: SALES_OFFICER

Please login at: http://localhost:3000/role-login
...
```

---

### 2️⃣ TEST CUSTOMER INVITATION EMAIL (30 seconds)

**Steps:**
1. Still on admin dashboard
2. Click "Invite Customer" button
3. Fill in:
   - Company Name: `Test Company`
   - Email: **YOUR_EMAIL_ADDRESS**
   - Phone: `1234567890`
   - Industry: Select any
   - Contact Person: `Test Contact`
4. Click "Send Invitation"
5. **CHECK YOUR EMAIL INBOX!** 📧

**Expected Email:**
```
Subject: Welcome to INSA CRM Customer Portal

Dear Test Company,

Welcome to INSA CRM! Your customer account has been created.

Login Credentials:
- Email: [your email]
- Temporary Password: [6 characters]

Customer Portal: http://localhost:3000/customer-login
...
```

---

### 3️⃣ TEST DEAL APPROVAL EMAIL (1 minute)

**Prerequisites:** Need an existing sales deal in the system

**Steps:**
1. Go to Sales Deals or Opportunities section
2. Find a deal with status "PENDING" or "NEEDS_APPROVAL"
   - If no deals exist, create one first (or ask me to create test data)
3. Click on the deal to open details
4. Click "Approve" button
5. (Optional) Enter discount percentage: `5`
6. Click "Confirm Approval"
7. **CHECK THE CUSTOMER'S EMAIL!** 📧

**Expected Email:**
```
Subject: INSA CRM - Your Sales Deal Has Been Approved!

Dear [Customer Company],

Good news! Your sales deal has been APPROVED.

Deal Details:
- Deal ID: 123
- Title: [Deal Name]
- Value: $[Amount]
- Status: APPROVED
- Discount Approved: 5.00%

You can now proceed to place your order...
```

---

### 4️⃣ TEST DEAL REJECTION EMAIL (1 minute)

**Steps:**
1. Find another pending sales deal
2. Click "Reject" button
3. Enter reason: `Budget constraints`
4. Click "Confirm Rejection"
5. **CHECK THE CUSTOMER'S EMAIL!** 📧

**Expected Email:**
```
Subject: INSA CRM - Sales Deal Status Update

Dear [Customer Company],

We regret to inform you that your sales deal has been 
reviewed and we are unable to proceed at this time.

Deal Details:
- Deal ID: 123
- Title: [Deal Name]
- Status: NOT APPROVED
- Reason: Budget constraints
...
```

---

## 🔍 Troubleshooting

### Email Not Received?

**1. Check Spam/Junk Folder**
- Gmail might filter it as spam initially

**2. Check Backend Logs**
Look for these messages in backend terminal:
```
INFO - Approval notification email sent to customer: [email]
INFO - Rejection notification email sent to customer: [email]
```

If you see:
```
WARN - Failed to send approval notification email
```
Then check email configuration.

**3. Verify Email Address**
- Make sure you entered a valid email address
- Check for typos

**4. Check Backend Console**
Look in the backend terminal for errors like:
```
ERROR - Error sending approval email notification: [error message]
```

**5. Test Gmail SMTP Connection**
The application.properties has:
```
spring.mail.host=smtp.gmail.com
spring.mail.username=adey81151@gmail.com
spring.mail.password=oeeptcizknbbghdy
```

If emails fail, the App Password might have expired or been revoked.

---

## 📊 Success Indicators

### ✅ User Creation Email Working If:
- User appears in user list
- Email received within 1-2 minutes
- Email contains username and temporary password
- Can login with provided credentials

### ✅ Customer Invitation Email Working If:
- Customer appears in customer list
- Email received within 1-2 minutes
- Email contains email and temporary password
- Can login at customer portal with credentials

### ✅ Deal Approval Email Working If:
- Deal status changes to "APPROVED"
- Email received within 1-2 minutes
- Email shows correct deal details
- Customer can see "Place Order" button

### ✅ Deal Rejection Email Working If:
- Deal status changes to "REJECTED"
- Email received within 1-2 minutes
- Email shows rejection reason
- Reason is recorded in system

---

## 🎯 Quick Verification Commands

### Check if Backend is Running:
```powershell
Invoke-WebRequest -Uri http://localhost:8082/actuator/health -UseBasicParsing
```
Should return: `StatusCode: 200`

### Check Backend Logs:
```powershell
# Look for this in the backend terminal:
"Started CrmBackendApplication"
"Tomcat started on port 8082"
```

### Check if Email Service is Loaded:
Look for in backend logs:
```
INFO - EmailService initialized
INFO - Mail server: smtp.gmail.com
```

---

## 🚀 Production Checklist

Before going to production:

### Email Configuration:
- [ ] Change from Gmail to company email server
- [ ] Use proper email templates with company branding
- [ ] Add email delivery tracking
- [ ] Set up email logs and monitoring
- [ ] Configure proper error alerts

### Security:
- [ ] Move email password to environment variables (not in properties file)
- [ ] Use secure credential management (Azure Key Vault, AWS Secrets Manager)
- [ ] Enable email rate limiting
- [ ] Add email verification before sending

### Testing:
- [ ] Test with multiple email providers (Gmail, Outlook, Yahoo)
- [ ] Test email delivery rate
- [ ] Test with large user base
- [ ] Load test email sending

---

## 📧 Email Configuration Reference

### Current Setup (Development):
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=adey81151@gmail.com
spring.mail.password=oeeptcizknbbghdy
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### For Production (Example - Microsoft 365):
```properties
spring.mail.host=smtp.office365.com
spring.mail.port=587
spring.mail.username=noreply@yourcompany.com
spring.mail.password=${EMAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### For Production (Example - AWS SES):
```properties
spring.mail.host=email-smtp.us-east-1.amazonaws.com
spring.mail.port=587
spring.mail.username=${AWS_SES_USERNAME}
spring.mail.password=${AWS_SES_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 🎉 ALL DONE!

Everything is **READY FOR TESTING**! 🚀

Go ahead and test all 4 email features using the steps above!

**Expected Result:** All emails should be delivered within 1-2 minutes.

**Have fun testing!** 😊
