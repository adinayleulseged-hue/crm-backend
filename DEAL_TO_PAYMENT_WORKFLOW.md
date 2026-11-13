# 💼 Complete Deal to Payment Workflow Documentation

## 🔄 Workflow Overview

```
CUSTOMER → SALES MANAGER → QUOTATION → INVOICE → FINANCE → PAYMENT → DELIVERY
```

### **Complete Flow Steps:**

1. **Customer submits deal** (Create Opportunity/Deal)
2. **Admin/Sales Manager approves deal** (with optional discount)
3. **System auto-generates quotation** from approved deal
4. **Customer places order** from quotation
5. **System auto-generates invoice** from order
6. **Finance Officer approves/rejects invoice**
7. **Customer sees "PAID" status** when approved
8. **Delivery is triggered** automatically

---

## 📊 Backend API Endpoints (Already Implemented)

### **1. Deal/Opportunity Management**

#### **POST** `/api/opportunities` or `/api/sales-deals`
Create new deal (Customer action)
```json
{
  "opportunityName": "Cybersecurity System Deal",
  "customerId": 123,
  "productId": 1,
  "quantity": 5,
  "unitPrice": 800000,
  "expectedRevenue": 4000000,
  "expectedCloseDate": "2026-10-01",
  "description": "5 units of HSM servers",
  "stage": "NEW",
  "type": "NEW_BUSINESS"
}
```

#### **PATCH** `/api/opportunities/{id}/submit-for-approval`
Submit deal for approval (Customer → Admin/Sales Manager)
- Changes status from `DRAFT` to `PENDING_APPROVAL`
- Sends notification to Admin/Sales Manager

#### **PATCH** `/api/opportunities/{id}/approve?discountApproved=5.0`
Approve deal (Admin/Sales Manager only)
- Changes status from `PENDING_APPROVAL` to `APPROVED`
- Auto-generates quotation
- Sends approval email to customer

#### **PATCH** `/api/opportunities/{id}/reject?reason=Budget exceeded`
Reject deal (Admin/Sales Manager only)
- Changes status to `REJECTED`
- Records rejection reason
- Sends rejection email to customer

#### **GET** `/api/opportunities/pending-approvals`
Get pending deals (Admin/Sales Manager view)

---

### **2. Quotation Management**

#### **POST** `/api/opportunities/{id}/generate-quotation`
Generate quotation from approved deal (Auto or Manual)
- Creates quotation with line items
- Links to opportunity
- Sets expiry date (30 days)

---

### **3. Order Management**

#### **POST** `/api/opportunities/{id}/place-order`
Place order from approved deal (Customer action)
- Creates sales order
- Auto-generates invoice
- Changes workflow status to `ORDER_PLACED` → `INVOICE_GENERATED`

---

### **4. Invoice Management**

#### **GET** `/api/invoices`
Get all invoices
- Filter by customer: `?customerId=123`
- Filter by status: `?status=PENDING`

#### **GET** `/api/invoices/pending-approval`
Get pending invoices (Finance Officer view)

#### **PATCH** `/api/invoices/{id}/approve`
Approve invoice (Finance Officer only)
- Changes status from `PENDING` to `APPROVED`
- Sends notification to customer
- Customer can now see "APPROVED" status

#### **PATCH** `/api/invoices/{id}/reject?reason=Incorrect amount`
Reject invoice (Finance Officer only)
- Changes status to `DRAFT`
- Records rejection reason and timestamp
- Sends notification to customer with reason

#### **PATCH** `/api/invoices/{id}/confirm-payment`
Customer confirms payment (Customer action)
- Changes status from `APPROVED` to `CONFIRMED`

#### **PATCH** `/api/invoices/{id}/mark-paid`
Mark invoice as paid (Finance Officer/System action)
- Changes status to `PAID`
- Updates paid amount
- Triggers delivery workflow

---

## 🎨 Frontend Implementation Requirements

### **1. Customer Dashboard Updates Needed**

#### **A. Deals/Opportunities Tab**
**Features to add:**
- ✅ Create new deal form
- ✅ Submit deal for approval button
- ✅ View deal status (DRAFT, PENDING_APPROVAL, APPROVED, REJECTED)
- ✅ Show approval/rejection notifications
- ✅ Place order button (only when status = APPROVED)

**Deal Status Display:**
```typescript
const getDealStatusBadge = (status: string) => {
  const badges = {
    DRAFT: { color: "gray", icon: "📝", text: "Draft" },
    PENDING_APPROVAL: { color: "yellow", icon: "⏳", text: "Awaiting Approval" },
    APPROVED: { color: "green", icon: "✅", text: "Approved" },
    REJECTED: { color: "red", icon: "❌", text: "Rejected" },
    ORDER_PLACED: { color: "blue", icon: "📦", text: "Order Placed" },
    INVOICE_GENERATED: { color: "purple", icon: "📄", text: "Invoice Generated" },
  };
  return badges[status] || badges.DRAFT;
};
```

#### **B. Invoices Tab**
**Features to add:**
- ✅ View all invoices
- ✅ Show invoice status with color coding
- ✅ Display "PAID" badge when approved by finance
- ✅ Show rejection reason if rejected
- ✅ Confirm payment button (when status = APPROVED)

**Invoice Status Display:**
```typescript
const getInvoiceStatusBadge = (status: string) => {
  const badges = {
    DRAFT: { color: "gray", icon: "📝", text: "Draft" },
    PENDING: { color: "yellow", icon: "⏳", text: "Awaiting Finance Approval" },
    APPROVED: { color: "green", icon: "✅", text: "Approved - Ready for Payment" },
    CONFIRMED: { color: "blue", icon: "💳", text: "Payment Confirmed" },
    PAID: { color: "emerald", icon: "💰", text: "PAID" },
    REJECTED: { color: "red", icon: "❌", text: "Rejected" },
  };
  return badges[status] || badges.DRAFT;
};
```

#### **C. Notifications**
**Show real-time notifications for:**
- ✅ Deal approved
- ✅ Deal rejected (with reason)
- ✅ Invoice approved by finance
- ✅ Invoice rejected by finance (with reason)
- ✅ Invoice marked as PAID
- ✅ Delivery scheduled

---

### **2. Admin Dashboard Updates Needed**

#### **Pending Deals Tab**
**Features:**
- ✅ List all deals with status = PENDING_APPROVAL
- ✅ Approve button with optional discount field
- ✅ Reject button with required reason field
- ✅ Show customer details

**Approve Modal:**
```typescript
<Modal>
  <h3>Approve Deal #{dealCode}</h3>
  <input type="number" placeholder="Discount %" />
  <button>Approve Deal</button>
</Modal>
```

**Reject Modal:**
```typescript
<Modal>
  <h3>Reject Deal #{dealCode}</h3>
  <textarea placeholder="Rejection reason (required)" />
  <button>Reject Deal</button>
</Modal>
```

---

### **3. Sales Manager Dashboard Updates Needed**

**Same as Admin Dashboard:**
- ✅ Pending Deals tab
- ✅ Approve/Reject functionality
- ✅ View all opportunities

---

### **4. Finance Officer Dashboard Updates Needed**

#### **Pending Invoices Tab**
**Features:**
- ✅ List all invoices with status = PENDING
- ✅ Show invoice details (customer, amount, order ref)
- ✅ Approve button
- ✅ Reject button with required reason field

**Approve Modal:**
```typescript
<Modal>
  <h3>Approve Invoice #{invoiceNumber}</h3>
  <p>Customer: {customerName}</p>
  <p>Amount: ETB {amount}</p>
  <button>Approve Invoice</button>
</Modal>
```

**Reject Modal:**
```typescript
<Modal>
  <h3>Reject Invoice #{invoiceNumber}</h3>
  <p>Customer: {customerName}</p>
  <textarea placeholder="Rejection reason (required)" />
  <button>Reject Invoice</button>
</Modal>
```

---

## 📧 Email Notifications (Already Implemented)

### **Notification Types Added:**
1. `INVOICE_APPROVED` - Finance approves invoice
2. `INVOICE_REJECTED` - Finance rejects invoice (with reason)

### **Existing Notifications:**
1. Deal submitted for approval
2. Deal approved
3. Deal rejected
4. Invoice paid
5. Order confirmation

---

## 🚀 Complete User Journey Example

### **Scenario: Customer purchases 5 HSM servers**

#### **Step 1: Customer Creates Deal**
```
Customer Portal → Deals Tab → Create New Deal
- Product: Enterprise HSM Server
- Quantity: 5
- Unit Price: ETB 800,000
- Total: ETB 4,000,000
- Status: DRAFT
```

#### **Step 2: Customer Submits for Approval**
```
Click "Submit for Approval"
- Status: DRAFT → PENDING_APPROVAL
- Notification sent to Sales Manager
- Customer sees: "Deal submitted, awaiting approval"
```

#### **Step 3: Sales Manager Approves**
```
Sales Manager Dashboard → Pending Deals Tab
- Reviews deal details
- Approves with 5% discount
- Status: PENDING_APPROVAL → APPROVED
- System auto-generates quotation
- Email sent to customer: "Your deal has been approved!"
```

#### **Step 4: Customer Places Order**
```
Customer Portal → Deals Tab
- Sees green "✅ Approved" badge
- Clicks "Place Order"
- System creates sales order
- System auto-generates invoice
- Status: APPROVED → ORDER_PLACED → INVOICE_GENERATED
```

#### **Step 5: Finance Reviews Invoice**
```
Finance Officer Dashboard → Pending Invoices Tab
- Invoice #INV-2026-001
- Customer: INSA HQ
- Amount: ETB 3,800,000 (after 5% discount)
- Status: PENDING
```

#### **Step 6A: Finance Approves (Happy Path)**
```
Finance clicks "Approve"
- Status: PENDING → APPROVED
- Email sent to customer: "Invoice approved!"
- Customer Portal shows: "💰 PAID" (green badge)
- Delivery workflow triggered
```

#### **Step 6B: Finance Rejects (Error Path)**
```
Finance clicks "Reject"
- Enters reason: "Incorrect VAT calculation"
- Status: PENDING → DRAFT
- Email sent to customer with rejection reason
- Customer sees: "❌ Rejected: Incorrect VAT calculation"
- Customer can revise and resubmit
```

#### **Step 7: Customer Sees PAID Status**
```
Customer Portal → Invoices Tab
- Invoice #INV-2026-001
- Status: 💰 PAID (emerald green badge)
- Payment Date: 2026-09-05
- Delivery Status: Scheduled
```

#### **Step 8: Delivery Triggered**
```
Order Status → Shipped
- Tracking number assigned
- Driver assigned
- Estimated delivery date
- Customer can track in real-time
```

---

## 🎯 Database Status Flow

### **Opportunity (Deal) WorkflowStatus:**
```
DRAFT → PENDING_APPROVAL → APPROVED → ORDER_PLACED → INVOICE_GENERATED
  ↓
REJECTED (with reason)
```

### **Invoice Status:**
```
DRAFT → PENDING → APPROVED → CONFIRMED → PAID
  ↓
REJECTED (back to DRAFT, with reason)
```

---

## ✅ Implementation Checklist

### **Backend (✅ COMPLETED)**
- [x] Add invoice rejection fields to database
- [x] Add invoice rejection endpoint
- [x] Add getPendingInvoices endpoint
- [x] Add notification methods
- [x] Update NotificationType enum

### **Frontend (🔄 IN PROGRESS)**
- [ ] Update Customer Dashboard - Deals tab
- [ ] Update Customer Dashboard - Invoices tab
- [ ] Update Admin Dashboard - Pending Deals tab
- [ ] Update Sales Manager Dashboard - Pending Deals tab
- [ ] Update Finance Officer Dashboard - Pending Invoices tab
- [ ] Add notification listeners
- [ ] Add status badges with colors
- [ ] Add approval/rejection modals

### **Testing (⏳ PENDING)**
- [ ] Test complete workflow end-to-end
- [ ] Test rejection scenarios
- [ ] Test email notifications
- [ ] Test delivery trigger

---

## 🔐 Permissions Matrix

| Action | Customer | Sales Officer | Sales Manager | Admin | Finance Officer |
|--------|----------|---------------|---------------|-------|-----------------|
| Create Deal | ✅ | ✅ | ✅ | ✅ | ❌ |
| Submit Deal | ✅ | ❌ | ❌ | ❌ | ❌ |
| Approve Deal | ❌ | ❌ | ✅ | ✅ | ❌ |
| Reject Deal | ❌ | ❌ | ✅ | ✅ | ❌ |
| Place Order | ✅ | ❌ | ❌ | ❌ | ❌ |
| View Invoices | ✅ (own) | ❌ | ✅ (all) | ✅ (all) | ✅ (all) |
| Approve Invoice | ❌ | ❌ | ❌ | ✅ | ✅ |
| Reject Invoice | ❌ | ❌ | ❌ | ✅ | ✅ |
| Confirm Payment | ✅ | ❌ | ❌ | ❌ | ❌ |
| Mark as Paid | ❌ | ❌ | ❌ | ✅ | ✅ |

---

## 📞 API Response Examples

### **Approved Deal Response:**
```json
{
  "success": true,
  "message": "Deal approved successfully",
  "data": {
    "id": 123,
    "opportunityNumber": "OPP-2026-001",
    "workflowStatus": "APPROVED",
    "approvedBy": 5,
    "approvedAt": "2026-09-03T14:30:00",
    "discountApproved": 5.0,
    "quotationGenerated": true
  }
}
```

### **Rejected Invoice Response:**
```json
{
  "success": true,
  "message": "Invoice rejected. Customer notified.",
  "data": {
    "id": 456,
    "invoiceNumber": "INV-2026-001",
    "status": "DRAFT",
    "rejectedBy": "FINANCE_OFFICER",
    "rejectedAt": "2026-09-03T15:45:00",
    "rejectionReason": "Incorrect VAT calculation"
  }
}
```

---

## 🎨 UI Components Needed

### **Status Badge Component:**
```typescript
<StatusBadge 
  status="PAID" 
  icon="💰" 
  color="emerald" 
  text="PAID"
/>
```

### **Approval Modal:**
```typescript
<ApprovalModal
  title="Approve Deal"
  onApprove={(discount) => handleApprove(dealId, discount)}
  onCancel={() => setShowModal(false)}
/>
```

### **Rejection Modal:**
```typescript
<RejectionModal
  title="Reject Invoice"
  onReject={(reason) => handleReject(invoiceId, reason)}
  onCancel={() => setShowModal(false)}
  reasonRequired={true}
/>
```

---

**This workflow ensures complete visibility and control at every step, with proper notifications and status tracking!** 🎉
