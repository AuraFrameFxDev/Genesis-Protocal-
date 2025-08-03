# Windows Drive Cleanup Script - Comprehensive Junk Removal
# Run as Administrator for best results

Write-Host "=== AuraFrameFX Drive Cleanup Utility ===" -ForegroundColor Green
Write-Host "Starting comprehensive system cleanup..." -ForegroundColor Yellow

# Stop processes that might lock files
Write-Host "`n🛑 Stopping processes that lock files..." -ForegroundColor Cyan
Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue
Stop-Process -Name "javaw" -Force -ErrorAction SilentlyContinue
Stop-Process -Name "gradle*" -Force -ErrorAction SilentlyContinue
Get-Process | Where-Object {$_.ProcessName -like "*gradle*"} | Stop-Process -Force -ErrorAction SilentlyContinue

# 1. Windows Temp Files
Write-Host "`n🗂️ Cleaning Windows temp files..." -ForegroundColor Cyan
Remove-Item -Path "$env:TEMP\*" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$env:LOCALAPPDATA\Temp\*" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "C:\Windows\Temp\*" -Recurse -Force -ErrorAction SilentlyContinue

# 2. Windows Update Cleanup
Write-Host "`n🔄 Cleaning Windows Update files..." -ForegroundColor Cyan
DISM /Online /Cleanup-Image /StartComponentCleanup /ResetBase
cleanmgr /sageset:1
cleanmgr /sagerun:1

# 3. Browser Cache Cleanup
Write-Host "`n🌐 Cleaning browser caches..." -ForegroundColor Cyan
# Chrome
Remove-Item -Path "$env:LOCALAPPDATA\Google\Chrome\User Data\Default\Cache\*" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$env:LOCALAPPDATA\Google\Chrome\User Data\Default\Code Cache\*" -Recurse -Force -ErrorAction SilentlyContinue

# Edge
Remove-Item -Path "$env:LOCALAPPDATA\Microsoft\Edge\User Data\Default\Cache\*" -Recurse -Force -ErrorAction SilentlyContinue

# Firefox
Remove-Item -Path "$env:LOCALAPPDATA\Mozilla\Firefox\Profiles\*\cache2\*" -Recurse -Force -ErrorAction SilentlyContinue

# 4. Development Tool Cleanup
Write-Host "`n⚙️ Cleaning development tool caches..." -ForegroundColor Cyan

# Gradle (MAIN ISSUE)
Write-Host "  📦 Cleaning Gradle caches..." -ForegroundColor Yellow
$gradleHome = "$env:USERPROFILE\.gradle"
if (Test-Path $gradleHome) {
    # Try multiple methods to delete stubborn .gradle folder
    takeown /F "$gradleHome" /R /D Y
    icacls "$gradleHome" /grant administrators:F /T
    Remove-Item -Path "$gradleHome\caches\*" -Recurse -Force -ErrorAction SilentlyContinue
    Remove-Item -Path "$gradleHome\daemon\*" -Recurse -Force -ErrorAction SilentlyContinue
    Remove-Item -Path "$gradleHome\wrapper\*" -Recurse -Force -ErrorAction SilentlyContinue
    
    # Nuclear option for .gradle
    cmd /c "rmdir /s /q `"$gradleHome`""
    Start-Sleep -Seconds 2
    
    # If still exists, use robocopy trick
    if (Test-Path $gradleHome) {
        $emptyDir = "$env:TEMP\empty_gradle_cleanup"
        New-Item -ItemType Directory -Path $emptyDir -Force | Out-Null
        robocopy "$emptyDir" "$gradleHome" /purge /mt:32
        Remove-Item -Path $emptyDir -Force
        Remove-Item -Path $gradleHome -Recurse -Force -ErrorAction SilentlyContinue
    }
}

# Maven
$mavenHome = "$env:USERPROFILE\.m2"
if (Test-Path $mavenHome) {
    Remove-Item -Path "$mavenHome\repository\*" -Recurse -Force -ErrorAction SilentlyContinue
}

# Node.js
Remove-Item -Path "$env:APPDATA\npm-cache\*" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$env:LOCALAPPDATA\npm-cache\*" -Recurse -Force -ErrorAction SilentlyContinue

# VS Code
Remove-Item -Path "$env:APPDATA\Code\logs\*" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$env:APPDATA\Code\CachedExtensions\*" -Recurse -Force -ErrorAction SilentlyContinue

# Android Studio
Remove-Item -Path "$env:USERPROFILE\.android\build-cache\*" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$env:LOCALAPPDATA\Android\Sdk\build-tools\*\*\dx.jar.lock" -Force -ErrorAction SilentlyContinue

# 5. System Cache Cleanup
Write-Host "`n💾 Cleaning system caches..." -ForegroundColor Cyan
Remove-Item -Path "$env:LOCALAPPDATA\Microsoft\Windows\INetCache\*" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$env:LOCALAPPDATA\Microsoft\Windows\WebCache\*" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$env:PROGRAMDATA\Microsoft\Windows\WER\*" -Recurse -Force -ErrorAction SilentlyContinue

# 6. Log Files
Write-Host "`n📄 Cleaning log files..." -ForegroundColor Cyan
Remove-Item -Path "C:\Windows\Logs\*" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "C:\Windows\Panther\*" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$env:LOCALAPPDATA\CrashDumps\*" -Recurse -Force -ErrorAction SilentlyContinue

# 7. Recycle Bin
Write-Host "`n🗑️ Emptying Recycle Bin..." -ForegroundColor Cyan
Clear-RecycleBin -Force -ErrorAction SilentlyContinue

# 8. Memory Cleanup
Write-Host "`n🧠 Clearing memory..." -ForegroundColor Cyan
[System.GC]::Collect()
[System.GC]::WaitForPendingFinalizers()

# 9. Disk Analysis
Write-Host "`n📊 Analyzing disk space..." -ForegroundColor Cyan
$drives = Get-WmiObject -Class Win32_LogicalDisk | Where-Object {$_.DriveType -eq 3}
foreach ($drive in $drives) {
    $freeGB = [math]::Round($drive.FreeSpace / 1GB, 2)
    $sizeGB = [math]::Round($drive.Size / 1GB, 2)
    $usedGB = $sizeGB - $freeGB
    $percentFree = [math]::Round(($freeGB / $sizeGB) * 100, 2)
    
    Write-Host "Drive $($drive.DeviceID) - Free: $freeGB GB / Total: $sizeGB GB ($percentFree% free)" -ForegroundColor White
}

Write-Host "`n✅ Cleanup Complete!" -ForegroundColor Green
Write-Host "🎯 Key areas cleaned:" -ForegroundColor Yellow
Write-Host "  • Windows temp files and caches"
Write-Host "  • Browser caches (Chrome, Edge, Firefox)"
Write-Host "  • Development tool caches (Gradle, Maven, Node, VS Code)"
Write-Host "  • System logs and error reports"
Write-Host "  • Recycle bin contents"
Write-Host ""
Write-Host "⚠️  IMPORTANT: Restart your computer to complete the cleanup process." -ForegroundColor Red
