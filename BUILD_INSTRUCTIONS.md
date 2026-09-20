# GlobalStream Chat App - APK Build Instructions

## Prerequisites
- Android SDK 24 or higher
- Gradle 8.0+
- Java 11+

## Quick Start - Build Debug APK

### Step 1: Build APK
```bash
./gradlew assembleDebug
```

### Step 2: Locate APK
The debug APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Step 3: Install on Device/Emulator
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Build Release APK (for Production)

### Option A: Without Keystore (if credentials set as environment variables)
```bash
./gradlew assembleRelease
```

### Option B: Create Keystore First
If you don't have a keystore yet, create one:

```bash
keytool -genkey -v -keystore my-upload-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

This will prompt you for:
- Keystore password
- Key password
- Your name, organization, etc.

Then build:
```bash
./gradlew assembleRelease
```

### Step 3: Locate Release APK
```
app/build/outputs/apk/release/app-release.apk
```

---

## Build for Google Play Store (AAB Bundle)

### Create App Bundle
```bash
./gradlew bundleRelease
```

Output:
```
app/build/outputs/bundle/release/app-release.aab
```

Upload this to Google Play Console for distribution.

---

## Environment Variables (Optional)

For CI/CD pipelines, set these environment variables instead of storing keystore locally:

```bash
export KEYSTORE_PATH=/path/to/keystore.jks
export STORE_PASSWORD=your_store_password
export KEY_PASSWORD=your_key_password
```

Then run:
```bash
./gradlew assembleRelease
```

---

## Troubleshooting

### "Could not connect to Kotlin compile daemon"
Already handled in gradle.properties:
```
kotlin.compiler.execution.strategy=in-process
```

### "Gradle daemon is stopped"
Clean and rebuild:
```bash
./gradlew clean assembleDebug
```

### Firebase Issues
If you see Firebase errors, make sure `google-services.json` is in `app/` directory.

The build is configured to warn if missing:
```
googleServices.missing.passthrough=true
```

### Memory Issues
Adjust in `gradle.properties`:
```
org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8
```

Change `4g` to `2g` or `6g` based on your system RAM.

---

## Build Variants

### Debug Build (for development)
```bash
./gradlew assembleDebug
```

### Release Build (for production)
```bash
./gradlew assembleRelease
```

### Run on Device
```bash
./gradlew installDebug
./gradlew runDebug
```

---

## APK Size Optimization

Current configuration includes:
- ✅ PNG crunching disabled (faster builds)
- ✅ Proguard/R8 minification enabled (smaller APK)
- ✅ Dependency info excluded from APK

The APK size is optimized for production.

---

## Next Steps

1. **Test Debug APK**: Install and test on device/emulator
2. **Create Keystore**: Set up signing credentials for release
3. **Build Release APK**: Use `./gradlew assembleRelease`
4. **Test Release APK**: Verify all features work
5. **Publish**: Upload to Google Play Store or distribute via APK

---

For more information, see:
- [Android Gradle Build Guide](https://developer.android.com/build)
- [Signing Your App](https://developer.android.com/training/articles/keystore)
- [Google Play Console](https://play.google.com/console)
