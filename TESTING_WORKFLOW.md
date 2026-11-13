# 🧪 Testing the Deal Approval Workflow

## 📋 Quick Test Guide

### **Step 1: Create a Deal as Customer**

1. Open http://localhost:3000
2. Login as **Customer** (username: `customer`, password: `customer123`)
3. Go to **"Deals"** tab in Customer Dashboard
4. Click **"Create New Deal"** button
5. Fill in the form:
   - Title: "Test HSM Deal"
   - Product: Select any product
   - Quantity: 5
   - Description: "Testing approval workflow"
6. Click **"Create Deal"**
7. Deal is created with status = **DRAFT**

### **Step 2: Submit Deal for Approval**

1. In the Deals table, find your newly created deal
2. Look for the status badge - should show **"DRAFT"** (gray)
3. Click the **"Submit for Approval"** button (green button with Send icon)
4. You should see toast message: "✅ Deal submitted and forwarded to Finance Portal for approval!"
5. Deal status changes to **"PENDING_APPROVAL"** (yellow)

### **Step 3: View as Admin/Sales Manager**

1. Logout from Customer account
2. Login as **Admin** (username: `admin`, password: `admin123`)  
   OR **Sales Manager** (username: `salesmanager`, password: `manager123`)
3. Go to **"Deals"** tab in Admin Dashboard
4. You should see **"⚠️ Pending Approvals"** section at the top
5. Your submitted deal should appear in the orange/red highlighted section

### **Step 4: Approve or Reject**

**To Approve:**
1. Click the **"Review"** button (green button)
2. Modal opens showing deal details
3. Optionally enter discount percentage
4. Click **"Approve Deal"**
5. Deal status changes to **"APPROVED"**
6. Customer receives notification
7. Quotation is auto-generated

**To Reject:**
1. Click **"Review"** button
2. In the modal, click **"Reject"** tab
3. Enter rejection reason (required)
4. Click **"Reject Deal"**
5. Deal status changes to **"REJECTED"**
6. Customer receives notification with reason

---

## 🔍 Troubleshooting

### **Problem: Deal not appearing in Admin Dashboard**

**Check 1: Is the deal status PENDING_APPROVAL?**
```sql
-- Connect to database
psql -h localhost -p 5433 -U postgres -d crm_db

-- Check deal status
SELECT id, opportunity_number, opportunity_name, workflow_status 
FROM opportunities 
WHERE workflow_status = 'PENDING_APPROVAL';
```

**Check 2: Check browser console for errors**
- Press F12 in browser
- Go to Console tab
- Look for red errors when submitting deal

**Check 3: Check backend logs**
- Look at the terminal where backend is running
- Should see:
  ```
  Opportunity 1 submitted for approval. Status: DRAFT -> PENDING_APPROVAL
  ```

**Check 4: Check API endpoint directly**
```bash
# Get pending approvals (replace TOKEN with your JWT)
curl "http://localhost:8082/api/opportunities/pending-approvals" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🎯 Expected API Responses

### **After Creating Deal (POST /api/opportunities)**
```json
{
  "success": true,
  "message": "Opportunity created successfully",
  "data": {
    "id": 1,
    "opportunityNumber": "OPP-2026-001",
    "opportunityName": "Test HSM Deal",
    "workflowStatus": "DRAFT",
    "stage": "NEW",
    "expectedRevenue": 4000000
  }
}
```

### **After Submitting (PATCH /api/opportunities/1/submit-for-approval)**
```json
{
  "success": true,
  "message": "Deal submitted for approval",
  "data": {
    "id": 1,
    "opportunityNumber": "OPP-2026-001",
    "workflowStatus": "PENDING_APPROVAL",
    "stage": "PROPOSAL"
  }
}
```

### **Get Pending Approvals (GET /api/opportunities/pending-approvals)**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "opportunityNumber": "OPP-2026-001",
        "opportunityName": "Test HSM Deal",
        "customerName": "Test Customer",
        "expectedRevenue": 4000000,
        "workflowStatus": "PENDING_APPROVAL",
        "quantity": 5,
        "productName": "Enterprise HSM Server"
      }
    ],
    "totalElements": 1
  }
}
```

---

## 🐛 Common Issues

### **Issue 1: "Submit for Approval" button not visible**
**Cause:** Deal status is not DRAFT  
**Solution:** Only DRAFT deals can be submitted. Check deal status in database.

### **Issue 2: 401 Unauthorized when submitting**
**Cause:** User not logged in or token expired  
**Solution:** Logout and login again to get fresh JWT token.

### **Issue 3: Admin sees empty pending approvals**
**Cause:** Frontend not calling API or filter issue  
**Solution:** 
1. Check browser console for API errors
2. Check Network tab - look for `/pending-approvals` request
3. Verify response data structure

### **Issue 4: Deal appears in "All Deals" but not "Pending Approvals"**
**Cause:** Frontend filtering based on `workflowStatus`  
**Solution:** Verify deal has `workflowStatus: "PENDING_APPROVAL"` in API response

---

## 📊 Database Verification

### **Check Deal Workflow Status:**
```sql
-- View all opportunities with workflow status
SELECT 
  id,
  opportunity_number,
  opportunity_name,
  customer_id,
  expected_revenue,
  stage,
  workflow_status,
  created_at
FROM opportunities
ORDER BY created_at DESC
LIMIT 10;
```

### **Check Pending Approvals:**
```sql
-- Count pending approvals
SELECT COUNT(*) as pending_count
FROM opportunities
WHERE workflow_status = 'PENDING_APPROVAL';

-- View pending deal details
SELECT 
  o.id,
  o.opportunity_number,
  o.opportunity_name,
  c.name as customer_name,
  o.expected_revenue,
  o.workflow_status,
  o.created_at
FROM opportunities o
LEFT JOIN customers c ON o.customer_id = c.id
WHERE o.workflow_status = 'PENDING_APPROVAL';
```

---

## ✅ Success Indicators

**When workflow is working correctly, you should see:**

1. **Customer Portal:**
   - ✅ Create Deal button works
   - ✅ Submit for Approval button visible for DRAFT deals
   - ✅ Status badge changes from gray (DRAFT) to yellow (PENDING_APPROVAL)
   - ✅ Toast notification appears on submission

2. **Admin Dashboard:**
   - ✅ Orange "⚠️ Pending Approvals" section visible
   - ✅ Deal appears in pending table with all details
   - ✅ "Review" button clickable
   - ✅ Approval modal opens with deal info

3. **Backend Logs:**
   - ✅ No 4XX or 5XX errors
   - ✅ SQL queries for `workflow_status = 'PENDING_APPROVAL'` visible
   - ✅ Success messages in logs

4. **Database:**
   - ✅ Deal record exists in `opportunities` table
   - ✅ `workflow_status` column = 'PENDING_APPROVAL'
   - ✅ `customer_id` properly linked

---

## 🚀 Quick Fix Commands

### **If backend needs restart:**
```bash
# Stop current process (Ctrl+C in terminal)
# Then restart:
cd crm-backend
mvn spring-boot:run
```

### **If frontend needs restart:**
```bash
# Stop current process (Ctrl+C in terminal)
# Then restart:
cd crm-frontend/insa-erp-crm
npm run dev
```

### **Clear browser cache:**
```
1. Press Ctrl+Shift+R (hard refresh)
2. Or clear cookies for localhost
3. Re-login to get fresh JWT token
```

---

## 📞 Next Steps After Testing

Once you verify the deal appears in Admin Dashboard:

1. **Test Approval Flow:**
   - Approve deal → Check customer receives notification
   - Verify quotation auto-generated
   - Check deal status becomes APPROVED

2. **Test Rejection Flow:**
   - Reject deal with reason → Check customer receives notification
   - Verify rejection reason saved
   - Check customer can see rejection reason

3. **Test Complete Workflow:**
   - Create → Submit → Approve → Place Order → Invoice Generated → Finance Approve

---

**If you still don't see the deal in Admin Dashboard after following these steps, let me know and I'll help debug!** 🐛
