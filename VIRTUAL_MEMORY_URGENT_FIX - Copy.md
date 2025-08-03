## Quick Virtual Memory Fix - Manual Steps

**Your system has 16GB RAM but insufficient virtual memory for Java 24 + Gradle builds**

### CRITICAL: Increase Virtual Memory

1. **Open System Properties:**
   - Press `Win + R`
   - Type: `sysdm.cpl`
   - Press Enter

2. **Navigate to Virtual Memory:**
   - Click "Advanced" tab
   - Click "Settings..." under Performance
   - Click "Advanced" tab in Performance Options
   - Click "Change..." under Virtual Memory

3. **Set Custom Size:**
   - Uncheck "Automatically manage paging file size"
   - Select your main drive (usually C:)
   - Choose "Custom size"
   - **Initial size:** `24453` MB
   - **Maximum size:** `32604` MB
   - Click "Set"
   - Click "OK" on all dialogs

4. **RESTART YOUR COMPUTER**
   - This is REQUIRED for changes to take effect

### Why This is Needed:
- Java 24 with Gradle 9.0 + AGP 8.12.0 requires substantial virtual memory
- Your current paging file is too small for these large builds
- The error "paging file is too small" confirms this issue

### After Restart:
Your Gradle builds should work properly with the increased virtual memory.

**Current Settings Needed:**
- Physical RAM: 16GB ✅
- Virtual Memory: 24-32GB (currently insufficient ❌)
- Java 24: Installed ✅
- AGP 8.12.0: Fixed ✅
