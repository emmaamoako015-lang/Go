# Fix Permission Denied Error When Building APK

## ❌ Problem: "Permission Denied" Error During Build

This error occurs when the build system doesn't have permission to access files or directories.

---

## ✅ Solution 1: Make Gradle Executable (Linux/Mac)

If you get "Permission Denied" on `./gradlew`:

```bash
chmod +x gradlew
chmod +x gradlew.bat
```

Then try building again:
```bash
./gradlew assembleRelease
```

---

## ✅ Solution 2: Clear Gradle Cache

Sometimes cache files cause permission issues:

```bash
# On Linux/Mac
rm -rf ~/.gradle/caches

# On Windows (PowerShell)
Remove-Item -Path "$env:USERPROFILE\.gradle\caches" -Recurse -Force
```

Then rebuild:
```bash
./gradlew clean assembleRelease
```

---

## ✅ Solution 3: Check File Permissions in Project

Make sure your project files have correct permissions:

```bash
# On Linux/Mac - give read/write permissions
chmod -R 755 GlobalStream-chatapp/

# Or more specifically for gradle
chmod 755 gradlew
chmod 755 gradlew.bat
```

---

## ✅ Solution 4: Run with Sudo (Last Resort)

If nothing else works:

```bash
sudo ./gradlew assembleRelease
```

**Note:** This gives admin access - only use if necessary.

---

## ✅ Solution 5: Delete Build Directories

Remove old build files that might have permission issues:

```bash
# Remove build output
rm -rf app/build
rm -rf build

# Clean and rebuild
./gradlew clean
./gradlew assembleRelease
```

---

## ✅ Solution 6: Check Keystore File Permissions

If using a keystore file for signing:

```bash
# On Linux/Mac
chmod 644 my-upload-key.jks

# Make sure it's readable
ls -l my-upload-key.jks
```

---

## 🪟 Windows Specific Fix

If on Windows and getting permission denied:

1. **Run Command Prompt as Administrator**
   - Press `Win + X`
   - Select "Command Prompt (Admin)" or "PowerShell (Admin)"

2. **Navigate to project:**
   ```bash
   cd path\to\GlobalStream-chatapp
   ```

3. **Build:**
   ```bash
   gradlew assembleRelease
   ```

4. **Or use gradlew.bat:**
   ```bash
   gradlew.bat assembleRelease
   ```

---

## 📋 Complete Fix Checklist

- [ ] Run `chmod +x gradlew` (Linux/Mac)
- [ ] Delete `~/.gradle/caches` folder
- [ ] Run `./gradlew clean` 
- [ ] Delete `app/build` folder
- [ ] Run `./gradlew assembleRelease` again
- [ ] If still failing, run as admin/sudo

---

## 🔍 Verify Permissions Are Set Correctly

Check current permissions:

```bash
# On Linux/Mac
ls -la gradlew
ls -la gradlew.bat

# Should show:
# -rwxr-xr-x (has execute permission)
```

If you see `-rw-r--r--` (no `x`), run:
```bash
chmod +x gradlew
```

---

## 💡 Quick Commands Summary

```bash
# Linux/Mac - Full fix
chmod +x gradlew
rm -rf ~/.gradle/caches
rm -rf app/build build
./gradlew clean assembleRelease

# Windows - Run as Admin in PowerShell
gradlew.bat clean assembleRelease

# Or use full path
.\gradlew.bat clean assembleRelease
```

---

## ✅ After Fix

Once you see no permission errors, your APK will be built at:
```
app/build/outputs/apk/release/app-release.apk
```

Try these solutions in order. Let me know which one works! 🚀
