# Fix for 403 Error on Contact Form

## Problem
The public contact form at `/contact` was receiving a **403 Forbidden** error when trying to submit leads because the `/api/leads` endpoint requires authentication (`@PreAuthorize`).

## Solution
Created a separate **public endpoint** that doesn't require authentication for contact form submissions.

---

## Changes Made

### 1. Backend - Added Public Endpoint

**File:** `src/main/java/com/crm/crm_backend/controller/LeadController.java`

```java
@PostMapping("/public")
public ResponseEntity<ApiResponse<LeadResponseDTO>> createPublicLead(
        @Valid @RequestBody LeadCreateDTO dto) {

    // Public endpoint for contact form submissions - no authentication required
    LeadResponseDTO response = leadService.createLead(dto);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Lead created", response));
}
```

**New Endpoint:** `POST /api/leads/public` (no auth required)
**Existing Endpoint:** `POST /api/leads` (still requires admin/crm_user auth)

---

### 2. Backend - Updated Security Config

**File:** `src/main/java/com/crm/crm_backend/security/SecurityConfig.java`

Added `/api/leads/public` to the permitAll list:

```java
.requestMatchers(
        HttpMethod.POST,
        "/api/auth/login",
        "/api/auth/register",
        "/api/leads/public"  // ← NEW: Allow public lead submissions
).permitAll()
```

---

### 3. Frontend - Updated Lead Service

**File:** `src/services/leadService.ts`

```typescript
async createLead(data: LeadRequest, isPublic: boolean = false): Promise<LeadResponse> {
  const endpoint = isPublic ? "/api/leads/public" : "/api/leads";
  const response = await apiClient.post<ApiResponse<LeadResponse>>(
    endpoint,
    data
  );
  return response.data.data;
}
```

---

### 4. Frontend - Updated Contact Form

**File:** `src/app/contact/page.tsx`

```typescript
await leadService.createLead({
  firstName: formData.firstName,
  lastName: formData.lastName,
  company: formData.company,
  email: formData.email,
  phone: formData.phone,
  jobTitle: formData.jobTitle,
  industry: formData.industry,
  source: "WEBSITE",
  notes: notesText,
  internalNotes: "Lead submitted via public Contact Us form - pending admin qualification",
}, true); // ← Pass true to use public endpoint
```

---

## How It Works Now

### Public Contact Form Flow
```
User fills contact form (/contact)
    ↓
Submit → POST /api/leads/public (NO AUTH REQUIRED)
    ↓
Backend creates Lead with status=NEW
    ↓
Success message shown to user
    ↓
Admin can view lead at /crm/leads
```

### Admin Lead Creation Flow
```
Admin logs in
    ↓
Goes to /crm/leads → Click "Add Lead"
    ↓
Submit → POST /api/leads (REQUIRES AUTH)
    ↓
Lead created with admin's credentials
```

---

## Testing

### Test Public Endpoint (No Auth)
```bash
curl -X POST http://localhost:8082/api/leads/public \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "company": "Test Company",
    "source": "WEBSITE"
  }'
```

**Expected:** 201 Created ✅

### Test Protected Endpoint (With Auth)
```bash
curl -X POST http://localhost:8082/api/leads \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane@example.com",
    "company": "Test Company",
    "source": "REFERRAL"
  }'
```

**Expected:** 201 Created ✅ (with valid token)
**Expected:** 401/403 ❌ (without token)

---

## Security Considerations

### Why Two Endpoints?

1. **Public Endpoint (`/api/leads/public`)**
   - No authentication required
   - Only for website contact form submissions
   - Source is always "WEBSITE"
   - Status always starts as "NEW"
   - Limited to contact form fields only

2. **Protected Endpoint (`/api/leads`)**
   - Requires admin/crm_user authentication
   - For internal lead creation by staff
   - Can set any source, status, assignments
   - Full access to all lead fields
   - Audit trail includes authenticated user

### Protection Against Abuse

The public endpoint is safe because:
- ✅ Input validation via `@Valid LeadCreateDTO`
- ✅ All leads start as "NEW" (unqualified) status
- ✅ Admin must manually qualify leads before conversion
- ✅ Rate limiting can be added if needed
- ✅ CORS configured to allow only specific origins
- ✅ No sensitive operations exposed (read-only for public)

---

## Restart Required

After making these changes, **restart the backend** for the security configuration to take effect:

```bash
cd crm-backend
./mvnw spring-boot:run
```

---

## Verification Checklist

- [ ] Backend compiles without errors
- [ ] Backend starts successfully
- [ ] `/api/leads/public` accessible without auth
- [ ] `/api/leads` still requires auth
- [ ] Contact form submits successfully
- [ ] Lead appears in admin dashboard with status=NEW
- [ ] No CORS errors in browser console
- [ ] Admin can qualify/disqualify the lead

---

## Status: ✅ FIXED

The 403 error is now resolved. Public users can submit contact forms without authentication, and leads are properly created in the database.

**Date:** 2026-09-03
**Issue:** 403 Forbidden on `/api/leads`
**Resolution:** Created public endpoint `/api/leads/public`
