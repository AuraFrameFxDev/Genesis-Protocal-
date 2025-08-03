# Gradle Specific Cleanup - Nuclear Option
# This script will forcefully remove all Gradle-related files

Write-Host "=== Gradle Nuclear Cleanup ===" -ForegroundColor Red
Write-Host "⚠️  This will remove ALL Gradle caches and daemons" -ForegroundColor Yellow

# Kill all Java processes
Write-Host "`n🛑 Terminating all Java/Gradle processes..." -ForegroundColor Cyan
Get-Process | Where-Object {$_.ProcessName -like "*java*"} | Stop-Process -Force -ErrorAction SilentlyContinue
Get-Process | Where-Object {$_.ProcessName -like "*gradle*"} | Stop-Process -Force -ErrorAction SilentlyContinue

# Wait for processes to terminate
Start-Sleep -Seconds 3

# Method 1: Standard deletion
Write-Host "`n📁 Attempting standard deletion..." -ForegroundColor Cyan
$gradleDir = "$env:USERPROFILE\.gradle"
if (Test-Path $gradleDir) {
    Remove-Item -Path $gradleDir -Recurse -Force -ErrorAction SilentlyContinue
}

# Method 2: Take ownership and delete
if (Test-Path $gradleDir) {
    Write-Host "`n🔓 Taking ownership and setting permissions..." -ForegroundColor Cyan
    takeown /F "$gradleDir" /R /D Y | Out-Null
    icacls "$gradleDir" /grant administrators:F /T | Out-Null
    Remove-Item -Path $gradleDir -Recurse -Force -ErrorAction SilentlyContinue
}

# Method 3: CMD rmdir
if (Test-Path $gradleDir) {
    Write-Host "`n💥 Using CMD rmdir..." -ForegroundColor Cyan
    cmd /c "rmdir /s /q `"$gradleDir`"" 2>$null
    Start-Sleep -Seconds 2
}

# Method 4: Robocopy purge (most effective for stubborn files)
if (Test-Path $gradleDir) {
    Write-Host "`n🚀 Using robocopy purge method..." -ForegroundColor Cyan
    $emptyDir = "$env:TEMP\empty_gradle_$(Get-Random)"
    New-Item -ItemType Directory -Path $emptyDir -Force | Out-Null
    robocopy "$emptyDir" "$gradleDir" /purge /mt:32 /r:0 /w:0 | Out-Null
    Remove-Item -Path $emptyDir -Force -ErrorAction SilentlyContinue
    Remove-Item -Path $gradleDir -Recurse -Force -ErrorAction SilentlyContinue
}

# Method 5: Handle individual locked files
if (Test-Path $gradleDir) {
    Write-Host "`n🔒 Handling locked files individually..." -ForegroundColor Cyan
    Get-ChildItem -Path $gradleDir -Recurse -Force | ForEach-Object {
        try {
            if ($_.PSIsContainer -eq $false) {
                [System.IO.File]::Delete($_.FullName)
            }
        } catch {
            # Try with handle.exe if available
            if (Get-Command handle.exe -ErrorAction SilentlyContinue) {
                handle.exe -p java -a | Out-Null
            }
        }
    }
    Remove-Item -Path $gradleDir -Recurse -Force -ErrorAction SilentlyContinue
}

# Verify cleanup
if (Test-Path $gradleDir) {
    Write-Host "`n❌ .gradle folder still exists. Manual intervention required." -ForegroundColor Red
    Write-Host "Try restarting Windows and running this script again." -ForegroundColor Yellow
} else {
    Write-Host "`n✅ .gradle folder successfully removed!" -ForegroundColor Green
}

# Clean project-specific Gradle files
Write-Host "`n🧹 Cleaning project .gradle folders..." -ForegroundColor Cyan
Get-ChildItem -Path "C:\" -Name ".gradle" -Directory -Recurse -Force -ErrorAction SilentlyContinue | ForEach-Object {
    $projectGradleDir = "C:\$_"
    Write-Host "  Removing: $projectGradleDir"
    Remove-Item -Path $projectGradleDir -Recurse -Force -ErrorAction SilentlyContinue
}

Write-Host "`n🎯 Gradle cleanup complete!" -ForegroundColor Green
