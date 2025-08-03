# PowerShell script to fix virtual memory for Gradle builds
# Run as Administrator

Write-Host "🔧 Virtual Memory Fix for Gradle Build Issues" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green

# Check if running as administrator
if (-NOT ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Host "⚠️  This script requires Administrator privileges!" -ForegroundColor Red
    Write-Host "Please run PowerShell as Administrator and execute this script again." -ForegroundColor Yellow
    pause
    exit 1
}

# Get current system info
$physicalRAM = (Get-CimInstance -ClassName Win32_ComputerSystem).TotalPhysicalMemory
$physicalRAMGB = [math]::Round($physicalRAM / 1GB, 2)

Write-Host "📊 System Information:" -ForegroundColor Cyan
Write-Host "   Physical RAM: $physicalRAMGB GB" -ForegroundColor White

# Get current paging file settings
$currentPageFile = Get-CimInstance -ClassName Win32_PageFileSetting
Write-Host "📄 Current Paging File Settings:" -ForegroundColor Cyan
if ($currentPageFile) {
    foreach ($pf in $currentPageFile) {
        Write-Host "   File: $($pf.Name)" -ForegroundColor White
        Write-Host "   Initial Size: $($pf.InitialSize) MB" -ForegroundColor White
        Write-Host "   Maximum Size: $($pf.MaximumSize) MB" -ForegroundColor White
    }
} else {
    Write-Host "   No custom paging file found (system managed)" -ForegroundColor Yellow
}

# Calculate recommended paging file size
# For development with Gradle: 1.5 times physical RAM for initial, 2 times for maximum
$recommendedInitialMB = [math]::Round($physicalRAMGB * 1.5 * 1024)
$recommendedMaxMB = [math]::Round($physicalRAMGB * 2 * 1024)

Write-Host ""
Write-Host "💡 Recommended Paging File Settings for Gradle:" -ForegroundColor Cyan
Write-Host "   Initial Size: $recommendedInitialMB MB" -ForegroundColor White
Write-Host "   Maximum Size: $recommendedMaxMB MB" -ForegroundColor White

Write-Host ""
$confirm = Read-Host "Do you want to apply these settings? (Y/N)"

if ($confirm -eq 'Y' -or $confirm -eq 'y') {
    try {
        Write-Host ""
        Write-Host "🔄 Applying virtual memory changes..." -ForegroundColor Yellow
        
        # Remove existing custom paging files
        if ($currentPageFile) {
            Write-Host "   Removing existing paging file settings..." -ForegroundColor Gray
            Get-CimInstance -ClassName Win32_PageFileSetting | Remove-CimInstance
        }
        
        # Disable automatic paging file management
        Write-Host "   Disabling automatic paging file management..." -ForegroundColor Gray
        $cs = Get-CimInstance -ClassName Win32_ComputerSystem
        Set-CimInstance -CimInstance $cs -Property @{AutomaticManagedPagefile = $false}
        
        # Create new paging file on C: drive
        Write-Host "   Creating new paging file configuration..." -ForegroundColor Gray
        $newPageFile = New-CimInstance -ClassName Win32_PageFileSetting -Property @{
            Name = "c:\pagefile.sys"
            InitialSize = $recommendedInitialMB
            MaximumSize = $recommendedMaxMB
        }
        
        Write-Host ""
        Write-Host "✅ Virtual memory settings updated successfully!" -ForegroundColor Green
        Write-Host "📋 New Settings:" -ForegroundColor Cyan
        Write-Host "   File: c:\pagefile.sys" -ForegroundColor White
        Write-Host "   Initial Size: $recommendedInitialMB MB" -ForegroundColor White
        Write-Host "   Maximum Size: $recommendedMaxMB MB" -ForegroundColor White
        
        Write-Host ""
        Write-Host "⚠️  IMPORTANT: You must restart your computer for changes to take effect!" -ForegroundColor Red
        Write-Host ""
        
        $restart = Read-Host "Do you want to restart now? (Y/N)"
        if ($restart -eq 'Y' -or $restart -eq 'y') {
            Write-Host "🔄 Restarting computer in 10 seconds..." -ForegroundColor Yellow
            Start-Sleep -Seconds 10
            Restart-Computer -Force
        } else {
            Write-Host "⏰ Please restart your computer manually when convenient." -ForegroundColor Yellow
        }
        
    } catch {
        Write-Host ""
        Write-Host "❌ Error applying virtual memory settings:" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        Write-Host ""
        Write-Host "💡 Alternative: You can also change this manually:" -ForegroundColor Cyan
        Write-Host "   1. Open System Properties (sysdm.cpl)" -ForegroundColor White
        Write-Host "   2. Go to Advanced tab > Performance Settings > Advanced > Virtual Memory" -ForegroundColor White
        Write-Host "   3. Uncheck 'Automatically manage paging file size'" -ForegroundColor White
        Write-Host "   4. Select C: drive and set Custom size:" -ForegroundColor White
        Write-Host "      Initial: $recommendedInitialMB MB" -ForegroundColor White
        Write-Host "      Maximum: $recommendedMaxMB MB" -ForegroundColor White
        Write-Host "   5. Click Set, OK, and restart" -ForegroundColor White
    }
}
} else {
    Write-Host ""
    Write-Host "❌ Virtual memory changes cancelled." -ForegroundColor Yellow
    Write-Host "💡 You can run this script again anytime to apply the changes." -ForegroundColor Cyan
}

Write-Host ""
Write-Host "🔧 Additional Gradle Memory Optimization Tips:" -ForegroundColor Cyan
Write-Host "   • Close other memory-intensive applications before building" -ForegroundColor White
Write-Host "   • Consider using --no-daemon flag for one-off builds" -ForegroundColor White
Write-Host "   • Monitor Task Manager during builds to see memory usage" -ForegroundColor White

pause
