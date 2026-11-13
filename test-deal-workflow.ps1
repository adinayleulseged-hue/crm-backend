# Test Deal Approval Workflow
# This script tests the complete customer → admin approval → order → payment → closure workflow

$baseUrl = "http://localhost:8082/api"
$ErrorActionPreference = "Continue"

Write-Host "`n=== DEAL APPROVAL WORKFLOW TEST ===" -ForegroundColor Cyan
Write-Host "Testing: Customer creates deal → Admin approves → Customer places order → Finance approves payment → Admin closes deal`n"

# Step 0: Setup - Update an existing deal with product details
Write-Host "`n[Step 0] Setting up test deal with product details..." -ForegroundColor Yellow

$dealId = 5  # DRAFT deal
$updateDeal = @{
    productId = 3  # INSA ERP Software License
    quantity = 2
    unitPrice = 850000.00
    discountRequested = 15.0
    expectedRevenue = 1445000.00  # (850000 * 2) * 0.85
} | ConvertTo-Json

# First, let's just check the deal exists
Write-Host "Checking deal ID $dealId..."
try {
    $checkResponse = Invoke-WebRequest -Uri "$baseUrl/opportunities/$dealId" -Method GET -UseBasicParsing -ErrorAction Stop
    Write-Host "✓ Deal found!" -ForegroundColor Green
} catch {
    Write-Host "✗ Deal not found. Using deal ID 5 by default." -ForegroundColor Red
}

Write-Host "`n--- Current Deal Status ---"
$env:PGPASSWORD="si7a14ti21$"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -h localhost -p 5433 -U postgres -d crm_db -c "SELECT id, opportunity_number, opportunity_name, workflow_status, product_id, quantity, unit_price, discount_requested FROM opportunities WHERE id = $dealId;"

# Update the deal with product details
Write-Host "`nUpdating deal with product details (productId=3, quantity=2, unitPrice=850000, discount=15%)..."
$env:PGPASSWORD="si7a14ti21$"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -h localhost -p 5433 -U postgres -d crm_db -c "UPDATE opportunities SET product_id = 3, quantity = 2, unit_price = 850000.00, discount_requested = 15.0, expected_revenue = 1445000.00 WHERE id = $dealId;"

Write-Host "✓ Deal updated with product details" -ForegroundColor Green

# Step 1: Customer submits deal for approval (DRAFT → PENDING_APPROVAL)
Write-Host "`n[Step 1] Customer submits deal for approval..." -ForegroundColor Yellow
Write-Host "Endpoint: PATCH /api/opportunities/$dealId/submit-for-approval"

try {
    $response1 = Invoke-WebRequest -Uri "$baseUrl/opportunities/$dealId/submit-for-approval" -Method PATCH -UseBasicParsing -ErrorAction Stop
    Write-Host "✓ Status: $($response1.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response1.Content)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.Exception.Response.StatusDescription)" -ForegroundColor Red
}

Start-Sleep -Seconds 1

# Check database after submission
Write-Host "`n--- After Submission ---"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -h localhost -p 5433 -U postgres -d crm_db -c "SELECT id, opportunity_number, workflow_status, product_id, quantity, discount_requested FROM opportunities WHERE id = $dealId;"

# Step 2: Admin approves deal (PENDING_APPROVAL → APPROVED)
Write-Host "`n[Step 2] Admin approves deal with 10% discount..." -ForegroundColor Yellow
Write-Host "Endpoint: PATCH /api/opportunities/$dealId/approve?discountApproved=10"

try {
    $response2 = Invoke-WebRequest -Uri "$baseUrl/opportunities/$dealId/approve?discountApproved=10" -Method PATCH -UseBasicParsing -ErrorAction Stop
    Write-Host "✓ Status: $($response2.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response2.Content)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 1

# Check database after approval
Write-Host "`n--- After Approval ---"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -h localhost -p 5433 -U postgres -d crm_db -c "SELECT id, workflow_status, approved_by, approved_at, discount_requested, discount_approved FROM opportunities WHERE id = $dealId;"

# Step 3: Customer places order (APPROVED → ORDER_PLACED + creates SalesOrder + Invoice)
Write-Host "`n[Step 3] Customer places order..." -ForegroundColor Yellow
Write-Host "Endpoint: POST /api/opportunities/$dealId/place-order"

try {
    $response3 = Invoke-WebRequest -Uri "$baseUrl/opportunities/$dealId/place-order" -Method POST -UseBasicParsing -ErrorAction Stop
    Write-Host "✓ Status: $($response3.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response3.Content)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        Write-Host "Error details: $($reader.ReadToEnd())" -ForegroundColor Red
    }
}

Start-Sleep -Seconds 1

# Check database after order placement
Write-Host "`n--- After Order Placement ---"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -h localhost -p 5433 -U postgres -d crm_db -c "SELECT id, workflow_status, order_id, invoice_id FROM opportunities WHERE id = $dealId;"

# Check created sales order
Write-Host "`n--- Created Sales Order ---"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -h localhost -p 5433 -U postgres -d crm_db -c "SELECT so.id, so.order_number, so.total_amount, so.status FROM sales_orders so INNER JOIN opportunities o ON so.id = o.order_id WHERE o.id = $dealId;"

# Check created invoice
Write-Host "`n--- Created Invoice ---"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -h localhost -p 5433 -U postgres -d crm_db -c "SELECT i.id, i.invoice_number, i.total_amount, i.status FROM invoices i INNER JOIN opportunities o ON i.id = o.invoice_id WHERE o.id = $dealId;"

# Step 4: Simulate Finance approving payment
Write-Host "`n[Step 4] Simulating Finance payment approval..." -ForegroundColor Yellow

# Get invoice ID
$invoiceIdQuery = & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -h localhost -p 5433 -U postgres -d crm_db -t -c "SELECT invoice_id FROM opportunities WHERE id = $dealId;"
$invoiceId = $invoiceIdQuery.Trim()

if ($invoiceId -and $invoiceId -ne "") {
    Write-Host "Invoice ID: $invoiceId"
    Write-Host "Updating invoice status to PAID..."
    & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -h localhost -p 5433 -U postgres -d crm_db -c "UPDATE invoices SET status = 'PAID', paid_at = NOW() WHERE id = $invoiceId;"
    Write-Host "✓ Invoice marked as PAID" -ForegroundColor Green
} else {
    Write-Host "✗ No invoice found" -ForegroundColor Red
}

Start-Sleep -Seconds 1

# Step 5: Admin closes deal as CLOSED_WON
Write-Host "`n[Step 5] Admin closes deal as CLOSED_WON..." -ForegroundColor Yellow
Write-Host "Endpoint: PATCH /api/opportunities/$dealId/close?status=CLOSED_WON&notes=Deal completed successfully"

try {
    $response5 = Invoke-WebRequest -Uri "$baseUrl/opportunities/$dealId/close?status=CLOSED_WON&notes=Deal%20completed%20successfully" -Method PATCH -UseBasicParsing -ErrorAction Stop
    Write-Host "✓ Status: $($response5.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response5.Content)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Final status
Write-Host "`n=== FINAL DEAL STATUS ===" -ForegroundColor Cyan
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -h localhost -p 5433 -U postgres -d crm_db -c "SELECT id, opportunity_number, opportunity_name, workflow_status, stage, closed_by, closed_at, discount_requested, discount_approved FROM opportunities WHERE id = $dealId;"

Write-Host "`n=== TEST COMPLETE ===" -ForegroundColor Cyan
Write-Host "
Workflow Summary:
1. Deal setup with product (DRAFT)
2. Customer submitted → PENDING_APPROVAL
3. Admin approved → APPROVED (discount: 15% requested → 10% approved)
4. Customer placed order → ORDER_PLACED + SalesOrder + Invoice created
5. Finance approved payment → Invoice PAID
6. Admin closed deal → CLOSED_WON
" -ForegroundColor White
