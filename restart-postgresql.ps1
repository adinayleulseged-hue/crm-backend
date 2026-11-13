# Script to restart PostgreSQL and clear connections
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  PostgreSQL Connection Reset Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Try to find and restart PostgreSQL service
$pgServices = Get-Service -Name "postgresql*" -ErrorAction SilentlyContinue

if ($pgServices) {
    Write-Host "Found PostgreSQL service(s):" -ForegroundColor Green
    $pgServices | ForEach-Object { Write-Host "  - $($_.Name) [$($_.Status)]" }
    Write-Host ""
    
    Write-Host "Attempting to restart PostgreSQL..." -ForegroundColor Yellow
    try {
        Restart-Service -Name "postgresql*" -Force -ErrorAction Stop
        Write-Host "✅ PostgreSQL restarted successfully!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Waiting 5 seconds for PostgreSQL to be ready..." -ForegroundColor Cyan
        Start-Sleep -Seconds 5
        Write-Host "✅ Ready! You can now start the backend." -ForegroundColor Green
    } catch {
        Write-Host "❌ Failed to restart (may need Administrator rights)" -ForegroundColor Red
        Write-Host "Error: $_" -ForegroundColor Red
        Write-Host ""
        Write-Host "Please run this script as Administrator, or:" -ForegroundColor Yellow
        Write-Host "1. Press Win+R, type 'services.msc', press Enter" -ForegroundColor Yellow
        Write-Host "2. Find 'postgresql' service" -ForegroundColor Yellow
        Write-Host "3. Right-click -> Restart" -ForegroundColor Yellow
    }
} else {
    Write-Host "❌ PostgreSQL service not found automatically" -ForegroundColor Red
    Write-Host ""
    Write-Host "Manual restart options:" -ForegroundColor Yellow
    Write-Host "1. Using pgAdmin:" -ForegroundColor Cyan
    Write-Host "   - Right-click server -> Disconnect" -ForegroundColor White
    Write-Host "   - Wait 5 seconds" -ForegroundColor White
    Write-Host "   - Right-click -> Connect" -ForegroundColor White
    Write-Host ""
    Write-Host "2. Using Windows Services:" -ForegroundColor Cyan
    Write-Host "   - Press Win+R, type 'services.msc'" -ForegroundColor White
    Write-Host "   - Find 'postgresql' in the list" -ForegroundColor White
    Write-Host "   - Right-click -> Restart" -ForegroundColor White
    Write-Host ""
    Write-Host "3. Check if PostgreSQL is running in Docker:" -ForegroundColor Cyan
    Write-Host "   docker ps | findstr postgres" -ForegroundColor White
    Write-Host "   docker restart <container-name>" -ForegroundColor White
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "After restarting PostgreSQL, run:" -ForegroundColor Green
Write-Host "  cd crm-backend" -ForegroundColor White
Write-Host "  mvn spring-boot:run" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
