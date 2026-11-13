#!/usr/bin/env pwsh
# Script to restart the CRM backend with new changes

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   CRM Backend Restart Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Find and kill existing Java processes on port 8082
Write-Host "Step 1: Stopping existing backend..." -ForegroundColor Yellow
$port8082 = Get-NetTCPConnection -LocalPort 8082 -ErrorAction SilentlyContinue | Select-Object -First 1
if ($port8082) {
    $processId = $port8082.OwningProcess
    Write-Host "  Found process $processId using port 8082" -ForegroundColor Gray
    Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
    Write-Host "  ✓ Stopped process $processId" -ForegroundColor Green
    Start-Sleep -Seconds 2
} else {
    Write-Host "  No process found on port 8082" -ForegroundColor Gray
}

# Step 2: Navigate to backend directory
Write-Host ""
Write-Host "Step 2: Navigating to backend directory..." -ForegroundColor Yellow
$backendPath = Split-Path -Parent $PSScriptRoot
if (Test-Path "$backendPath/pom.xml") {
    Set-Location $backendPath
    Write-Host "  ✓ In directory: $backendPath" -ForegroundColor Green
} else {
    Write-Host "  ✗ Cannot find pom.xml in $backendPath" -ForegroundColor Red
    exit 1
}

# Step 3: Clean and compile
Write-Host ""
Write-Host "Step 3: Cleaning and compiling..." -ForegroundColor Yellow
Write-Host "  Running: mvn clean compile" -ForegroundColor Gray
$compileResult = mvn clean compile -DskipTests 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Compilation successful" -ForegroundColor Green
} else {
    Write-Host "  ✗ Compilation failed!" -ForegroundColor Red
    Write-Host $compileResult
    exit 1
}

# Step 4: Start backend
Write-Host ""
Write-Host "Step 4: Starting backend..." -ForegroundColor Yellow
Write-Host "  Running: mvn spring-boot:run" -ForegroundColor Gray
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Backend is starting..." -ForegroundColor Cyan
Write-Host "Watch for: 'Started CrmBackendApplication'" -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

mvn spring-boot:run
