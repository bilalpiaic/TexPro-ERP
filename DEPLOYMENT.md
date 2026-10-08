# Deployment

## Google Play (required for store publication)

New apps must upload an **Android App Bundle** (`.aab`), not an APK.

Follow `docs/PLAY_STORE_READINESS.md` for the full Play Console checklist.

```bash
# Android Studio: Build → Generate Signed App Bundle
# or CI: Actions → Build Play App Bundle
```

The signed AAB is at `app/build/outputs/bundle/release/app-release.aab` when an upload keystore is present. Without keystore secrets, CI still produces an unsigned/debug-signed bundle you can inspect but **must not** upload to Play production.

## Debug APK (testers only)

A separate workflow on branch `bilalpiaic-deploy-apk` builds `app-debug.apk` for sideloading. Debug APKs are not accepted as the production Play artifact.

1. Open **Actions**.
2. Run **Build Android APK** or **Build Play App Bundle**.
3. Download the artifact.

## GitHub Release tags

Tags such as `v1.0.0` are fine for internal tester APKs. Production users should install from Google Play after the closed test period.
