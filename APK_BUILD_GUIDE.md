# GlobalStream Chat App - Complete APK Build Guide

## 📱 Quick Commands

### Build Debug APK (Testing)
```bash
./gradlew assembleDebug
```
**Output:** `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK (Production)
```bash
./gradlew assembleRelease
```
**Output:** `app/build/outputs/apk/release/app-release.apk`

### Build for Google Play (AAB Bundle)
```bash
./gradlew bundleRelease
```
**Output:** `app/build/outputs/bundle/release/app-release.aab`

---

## 🔧 Setup Signing Credentials

Your app is already configured with signing in `app/build.gradle.kts`.

### Option 1: Environment Variables (Recommended for CI/CD)

Set these before building:
```bash
export KEYSTORE_PATH=/path/to/keystore.jks
export STORE_PASSWORD=your_password
export KEY_PASSWORD=your_key_password
```

Then build normally:
```bash
./gradlew assembleRelease
```

### Option 2: Create Local Keystore

Generate a new keystore:
```bash
keytool -genkey -v -keystore my-upload-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

Place it in the project root directory, then build:
```bash
./gradlew assembleRelease
```

### Option 3: Use Debug Keystore (for development only)

The debug configuration uses:
- Path: `debug.keystore`
- Store Password: `android`
- Key Alias: `androiddebugkey`
- Key Password: `android`

---

## 📦 Permissions in AndroidManifest.xml

Your app has the following permissions enabled:
- `INTERNET` - For chat communication
- `ACCESS_NETWORK_STATE` - Check network status
- `RECORD_AUDIO` - For voice features
- `CAMERA` - For media sharing
- `VIBRATE` - For notifications

To disable any, edit `app/src/main/AndroidManifest.xml`.

---

## 🚀 Step-by-Step Release Build

### Step 1: Clean Previous Builds
```bash
./gradlew clean
```

### Step 2: Set Signing Credentials
Option A - Environment variables:
```bash
export KEYSTORE_PATH=my-upload-key.jks
export STORE_PASSWORD=your_password
export KEY_PASSWORD=your_key_password
```

Option B - Place keystore in root:
```
GlobalStream-chatapp/
├── my-upload-key.jks  ← Place here
├── app/
└── build.gradle.kts
```

### Step 3: Build Release APK
```bash
./gradlew assembleRelease
```

### Step 4: Verify APK
```bash
ls -lh app/build/outputs/apk/release/app-release.apk
```

### Step 5: Install on Device
```bash
adb install app/build/outputs/apk/release/app-release.apk
```

---

## 🎯 Build Variants

### Debug Build
- Includes debugging symbols
- Unsigned (uses debug keystore)
- Larger file size
- Use for development/testing

```bash
./gradlew assembleDebug
```

### Release Build
- Optimized with R8/ProGuard minification
- Signed with your key
- Smaller file size
- For production/Google Play

```bash
./gradlew assembleRelease
```

---

## 🏪 Publishing to Google Play Store

### Step 1: Build App Bundle
```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

### Step 2: Sign If Needed
Already handled by build configuration.

### Step 3: Upload to Play Console
1. Go to [Google Play Console](https://play.google.com/console)
2. Create new app or select existing
3. Go to Release → Production
4. Upload `app-release.aab`
5. Review and publish

---

## 🔍 Build Configuration Details

From your `app/build.gradle.kts`:

```
Application ID: com.aistudio.chatmessenger.xrtqpa
Min SDK: 24 (Android 7.0)
Target SDK: 36 (Android 15)
Version Code: 1
Version Name: 1.0
Compile SDK: 36
```

### Features Enabled:
- ✅ Jetpack Compose UI
- ✅ Kotlin DSL
- ✅ Firebase Integration
- ✅ Room Database
- ✅ Retrofit Networking
- ✅ Coroutines
- ✅ KSP Code Generation

### Optimizations:
- ✅ PNG crunching disabled (faster builds)
- ✅ Minification enabled (smaller APK)
- ✅ Dependency info excluded from APK
- ✅ Build cache enabled
- ✅ Parallel builds enabled

---

## 📊 APK Information

After building, inspect your APK:

```bash
# List contents
unzip -l app/build/outputs/apk/release/app-release.apk

# Check size
du -sh app/build/outputs/apk/release/app-release.apk

# Get detailed info
aapt dump badging app/build/outputs/apk/release/app-release.apk
```

---

## ❌ Troubleshooting

### 1. Keystore Not Found
```
Error: my-upload-key.jks not found
```
**Solution:** Ensure keystore path is correct or use environment variables.

### 2. Gradle Daemon Stopped
```bash
./gradlew clean assembleDebug
```

### 3. Out of Memory
Edit `gradle.properties`:
```
org.gradle.jvmargs=-Xmx6g -Dfile.encoding=UTF-8
```

### 4. Firebase Errors
```
Warning: Could not load google-services.json
```
This is non-blocking. App will work without Firebase services.

### 5. Kotlin Compile Daemon Issues
Already fixed in `gradle.properties`:
```
kotlin.compiler.execution.strategy=in-process
```

---

## 📋 Checklist Before Release

- [ ] Update version code and name in `app/build.gradle.kts`
- [ ] Test on multiple devices/Android versions
- [ ] Check app crashes with logcat
- [ ] Verify all features work offline
- [ ] Test internet connectivity
- [ ] Check permissions are requested properly
- [ ] Create signing keystore
- [ ] Build release APK successfully
- [ ] Sign APK and verify signature
- [ ] Install on device and test
- [ ] Prepare release notes
- [ ] Upload to Google Play Console

---

## 📚 Resources

- [Android Build Guide](https://developer.android.com/build)
- [App Signing](https://developer.android.com/training/articles/keystore)
- [Google Play Console](https://play.google.com/console)
- [Kotlin DSL Guide](https://docs.gradle.org/current/userguide/kotlin_dsl.html)
- [Firebase Setup](https://firebase.google.com/docs/android/setup)

---

## ✅ Build Status

Your project is **ready for APK building**!

- ✅ Gradle configured
- ✅ Dependencies resolved
- ✅ Signing configured
- ✅ AndroidManifest.xml ready
- ✅ Permissions set
- ✅ Build optimization enabled

**Next Step:** Run `./gradlew assembleRelease` to build your APK!
