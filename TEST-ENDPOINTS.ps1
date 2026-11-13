#!/usr/bin/env pwsh
# Script to test CRM backend endpoints

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   CRM Backend Endpoint Tester" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8082"

# Test 1: Health Check
Write-Host "Test 1: Health Check" -ForegroundColor Yellow
Write-Host "  GET $baseUrl/actuator/health" -ForegroundColor Gray
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get
    if ($health.status -eq "UP") {
        Write-Host "  ✓ Backend is UP and running" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Backend status: $($health.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "  ✗ Cannot connect to backend on port 8082" -ForegroundColor Red
    Write-Host "  Make sure backend is running!" -ForegroundColor Red
    exit 1
}

# Test 2: Create User Endpoint
Write-Host ""
Write-Host "Test 2: Create User Endpoint" -ForegroundColor Yellow
Write-Host "  POST $baseUrl/api/auth/admin/create-user" -ForegroundColor Gray
$testUser = @{
    username = "testuser_$(Get-Random -Maximum 9999)"
    email = "test$(Get-Random -Maximum 9999)@example.com"
    role = "SALES_OFFICER"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/admin/create-user" -Method Post -ContentType "application/json" -Body $testUser
    if ($response.success) {
        Write-Host "  ✓ User created successfully!" -ForegroundColor Green
        Write-Host "    User ID: $($response.userId)" -ForegroundColor Gray
        Write-Host "    Username: $($response.username)" -ForegroundColor Gray
        Write-Host "    Email: $($response.email)" -ForegroundColor Gray
        Write-Host "    Role: $($response.role)" -ForegroundColor Gray
        if ($response.temporaryPassword) {
            Write-Host "    Temp Password: $($response.temporaryPassword)" -ForegroundColor Cyan
        }
        if ($response.emailSent) {
            Write-Host "    Email: Sent ✓" -ForegroundColor Green
        } else {
            Write-Host "    Email: Not sent (not configured)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ✗ Failed: $($response.error)" -ForegroundColor Red
    }
} catch {
    Write-Host "  ✗ Request failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Message -like "*403*") {
        Write-Host "" -ForegroundColor Red
        Write-Host "  ⚠️  403 FORBIDDEN - SECURITY IS BLOCKING THE ENDPOINT!" -ForegroundColor Red
        Write-Host "  This means you HAVEN'T RESTARTED the backend after the code changes." -ForegroundColor Red
        Write-Host "" -ForegroundColor Red
        Write-Host "  Solution:" -ForegroundColor Yellow
        Write-Host "    1. Stop the backend (Ctrl+C)" -ForegroundColor Gray
        Write-Host "    2. Run: .\RESTART-BACKEND.bat" -ForegroundColor Gray
        Write-Host "    3. Wait for 'Started CrmBackendApplication'" -ForegroundColor Gray
        Write-Host "    4. Run this test again" -ForegroundColor Gray
    }
}

# Test 3: Invite Customer Endpoint
Write-Host ""
Write-Host "Test 3: Invite Customer Endpoint" -ForegroundColor Yellow
Write-Host "  POST $baseUrl/api/customers/invite" -ForegroundColor Gray
$testCustomer = @{
    companyName = "Test Company $(Get-Random -Maximum 9999)"
    contactName = "Test Contact"
    email = "customer$(Get-Random -Maximum 9999)@example.com"
    phone = "1234567890"
    customerType = "PRIVATE"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/customers/invite" -Method Post -ContentType "application/json" -Body $testCustomer
    if ($response.success) {
        Write-Host "  ✓ Customer invited successfully!" -ForegroundColor Green
        Write-Host "    Customer ID: $($response.customerId)" -ForegroundColor Gray
        Write-Host "    Customer #: $($response.customerNumber)" -ForegroundColor Gray
        Write-Host "    Email: $($response.email)" -ForegroundColor Gray
        if ($response.temporaryPassword) {
            Write-Host "    Temp Password: $($response.temporaryPassword)" -ForegroundColor Cyan
        }
        if ($response.emailSent) {
            Write-Host "    Email: Sent ✓" -ForegroundColor Green
        } else {
            Write-Host "    Email: Not sent (not configured)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ✗ Failed: $($response.error)" -ForegroundColor Red
    }
} catch {
    Write-Host "  ✗ Request failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Message -like "*403*") {
        Write-Host "  ⚠️  Security is blocking this endpoint too!" -ForegroundColor Red
        Write-Host "  You need to RESTART the backend!" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Summary
Write-Host "Summary:" -ForegroundColor Yellow
Write-Host "  If all tests passed (✓) - Your backend is working!" -ForegroundColor Green
Write-Host "  If tests failed with 403 - RESTART backend using RESTART-BACKEND.bat" -ForegroundColor Red
Write-Host "  If tests failed with connection error - Make sure backend is running" -ForegroundColor Red
Write-Host ""
