# Deployment

## Standalone APK (sideload)

This repo builds an installable **`TexPro-ERP.apk`** you can copy to a phone. It is not a Google Play Store package.

### Download from GitHub Actions

1. Open **Actions** → **Build downloadable APK**.
2. Open a green run (or click **Run workflow**).
3. Download the `TexPro-ERP-apk` artifact and unzip it.
4. Copy `TexPro-ERP.apk` to an Android phone (USB, Drive, or Telegram/WhatsApp to yourself).
5. On the phone: **Settings → Security → Install unknown apps** for the app you used to open the file, then tap the APK.

### Persistent download (GitHub Release)

- Push a tag such as `v1.0.0`, **or** run the workflow with **Run workflow**.
- That publishes `TexPro-ERP.apk` on the repository **Releases** page.
- Manual runs update the `sideload` release, so the file URL stays:

```text
https://github.com/bilalpiaic/TexPro-ERP/releases/download/sideload/TexPro-ERP.apk
```

(Available after the first successful `workflow_dispatch` or tag build.)

### Build on your machine

Requires JDK 17+ and Android SDK 36.

```bash
chmod +x scripts/build-apk.sh
./scripts/build-apk.sh
# output: dist/TexPro-ERP.apk
```

The sideload APK is the **debug-signed** build so it installs without a Play upload keystore. Google Sign-In needs `app/google-services.json` from Firebase; the rest of the mill ledger works offline.

## Google Play (AAB, not this APK)

Play production still requires a signed **Android App Bundle**. See `docs/PLAY_STORE_READINESS.md`. Do not upload `TexPro-ERP.apk` to Play production.
