# Email Service Configuration

## Overview
The CRM system now automatically sends login credentials via email when:
1. Admin invites a new customer (via "Invite Customer" button)
2. Admin creates a new user (via "Add New User" button)

## Quick Setup

### 1. Configure Email in application.properties

Copy the example file if you haven't already:
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

### 2. Add Your SMTP Settings

Edit `src/main/resources/application.properties` and configure these values:

```properties
# SMTP Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# From email address
crm.notification.email.from=noreply@insa.gov.et

# Frontend URL for login links
app.frontend.url=http://localhost:3000
```

## Email Provider Examples

### Gmail
1. Enable 2-factor authentication on your Google account
2. Generate an [App Password](https://myaccount.google.com/apppasswords)
3. Use these settings:
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-16-char-app-password
   ```

### Microsoft 365 / Outlook
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=your-email@outlook.com
spring.mail.password=your-password
```

### Custom SMTP Server
```properties
spring.mail.host=smtp.yourdomain.com
spring.mail.port=587
spring.mail.username=noreply@yourdomain.com
spring.mail.password=your-smtp-password
```

### Using TLS (port 465)
```properties
spring.mail.host=smtp.example.com
spring.mail.port=465
spring.mail.username=your-email@example.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.ssl.enable=true
spring.mail.properties.mail.transport.protocol=smtp
```

## Behavior

### When Email is Configured
- ✅ Credentials are sent automatically via email
- ✅ Modal shows "Email Sent!" success message
- ✅ Password is NOT displayed in the UI

### When Email is NOT Configured
- ⚠️ Account is still created successfully
- ⚠️ Modal shows temporary password for manual sharing
- ⚠️ Admin must copy and share credentials manually

## Testing

1. Start the backend with email configured
2. Login as admin
3. Click "Invite Customer" button
4. Fill in customer details
5. Check the email inbox for invitation

## Troubleshooting

### Email Not Sending
Check backend logs for errors:
```bash
tail -f logs/spring.log | grep -i mail
```

Common issues:
- **Authentication failed**: Wrong username/password
- **Connection timeout**: Firewall blocking SMTP port
- **SSL/TLS error**: Wrong port or SSL settings

### Testing with MailHog (Development)
For local testing without real email:
```bash
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

Then configure:
```properties
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false
```

View emails at: http://localhost:8025

## Security Notes

- ⚠️ Never commit `application.properties` with real credentials
- ✅ Use environment variables in production:
  ```properties
  spring.mail.username=${SMTP_USERNAME}
  spring.mail.password=${SMTP_PASSWORD}
  ```
- ✅ Consider using application-specific passwords (not your main password)
- ✅ Enable rate limiting on email sending to prevent abuse

## Email Templates

### Customer Invitation Email
```
Subject: Welcome to INSA CRM - Your Account Has Been Created

Dear [Contact Name],

Your customer account has been created in the INSA CRM system.

Login Details:
- Email: [customer@example.com]
- Temporary Password: [ABC123]

Please login at: http://localhost:3000/customer-login

For security reasons, please change your password after your first login.

Best regards,
INSA CRM Team
```

### User Creation Email
```
Subject: INSA CRM - Your Account Has Been Created

Dear [Username],

Your account has been created in the INSA CRM system.

Login Details:
- Username: [username]
- Email: [email@example.com]
- Temporary Password: [ABC123]
- Role: [SALES_OFFICER]

Please login at: http://localhost:3000/role-login

Best regards,
INSA CRM Team
```

## Production Deployment

When deploying to production, use environment variables:

```bash
export SMTP_HOST=smtp.gmail.com
export SMTP_PORT=587
export SMTP_USERNAME=noreply@insa.gov.et
export SMTP_PASSWORD=your-secure-password
export CRM_MAIL_FROM=noreply@insa.gov.et
export FRONTEND_URL=https://crm.insa.gov.et
```

Or use a `.env` file with your deployment platform.
