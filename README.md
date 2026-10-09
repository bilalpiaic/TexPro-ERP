# TexPro ERP

Android accounting app for textile converters and weaving / dyeing / stitching mills.

Track a customer **sale order** through **grey cloth purchase**, **dyeing and printing**, **CMT stitching**, and **finished-goods invoicing**. Each step posts categorized vouchers to a double-entry general ledger so lot cost, COGS, and margin stay on the books.

| | |
| --- | --- |
| Platform | Android 7.0+ (`minSdk` 24, `targetSdk` 36) |
| Package | `com.aistudio.ledgerproerp.qzmxyt` |
| Version | 1.2.2 |
| Stack | Kotlin, Jetpack Compose, Room, Firebase (optional cloud) |

---

## Download the APK

The installable file is **`TexPro-ERP.apk`**. It is a sideload build (arm64, not debuggable), not a Google Play package.

**Do not install** `app-debug.apk` from the `v1.0.1` GitHub Release. That debug APK is blocked on Samsung Galaxy A56 / One UI 7.

Download from **Releases** after tag `v1.2.2`:

```text
https://github.com/bilalpiaic/TexPro-ERP/releases/download/v1.2.2/TexPro-ERP.apk
```

Until that tag exists: **Actions → Build downloadable APK** → open a green run on this branch → artifact `TexPro-ERP-apk` → unzip `TexPro-ERP.apk`.

### Install on a phone

Use **`TexPro-ERP.apk` 1.2.2**. Do not use `app-debug.apk` (`v1.0.1`). Version 1.1.0 closed on open; 1.1.1 and later run offline without Firebase. Layouts reflow on phone width so amounts and labels stay readable.

1. Uninstall any previous **TexPro ERP** / failed install.
2. Copy `TexPro-ERP.apk` to the phone (USB, Drive, or send it to yourself).
3. **Samsung (A56 / One UI):** Settings → **Security and privacy** → **Auto Blocker** → turn **Off**. Then Settings → **Security and privacy** → **Install unknown apps** → allow **My Files** (or Chrome / Drive).
4. Open **Play Store → profile → Play Protect**. If it blocks the install, choose **Install anyway**.
5. Tap `TexPro-ERP.apk` and install.
6. First launch includes **sample mill data**.

The mill ledger works offline. **Sign in with Google** needs `app/google-services.json` from Firebase.

More deploy detail: [DEPLOYMENT.md](DEPLOYMENT.md).

---

## What the app does

**Sale orders** — book customer orders with quality, blend, width, pieces, meters, and price.

**Lots flow** — one lot per order through:

1. Grey cloth purchase (meters × rate → inventory + AP mill)
2. Issue to processor (WIP)
3. Receive dyed / printed fabric (shrinkage + processing bill)
4. CMT stitching (meters → packed units)
5. Dispatch and sales invoice (AR, revenue, COGS, gross margin)

**Journal** — post the seven voucher types: JV, CR, CP, BP, BR, Sale, Purchase. Unbalanced entries are rejected.

**Reports** — trial balance, profit and loss, balance sheet, inventory valuation, and lot profitability.

**Organizations** — create mill profiles locally. **Sign in with Google** opens the Google account picker. After sign-in, **Save to Drive** stores that mill's ledger in your Google Drive under `TexPro ERP / {org code} - {org name} / texpro-ledger.json`. **Load Drive** restores it. Optional Firestore sync still works when `google-services.json` is present.

Accounting rules and voucher mapping: [ERP.md](ERP.md).

---

## Build from source

Requires **JDK 17+** and **Android SDK 36**.

```bash
git clone https://github.com/bilalpiaic/TexPro-ERP.git
cd TexPro-ERP
./scripts/build-apk.sh
# output: dist/TexPro-ERP.apk
```

Or open the project in Android Studio and run the `app` configuration.

| Gradle task | Output |
| --- | --- |
| `./gradlew :app:copySideloadApk` | `dist/TexPro-ERP.apk` (installable, debug-signed) |
| `./gradlew :app:assembleDebug` | `app/build/outputs/apk/debug/` |
| `./gradlew :app:bundleRelease` | Play App Bundle (needs an upload keystore) |

Put Firebase config at `app/google-services.json` (gitignored) if you want Firestore sync. Google Sign-In and Drive save/load work without it, as long as this app's package and SHA-1 are registered in Google Cloud Console.

---

## Project layout

```text
app/src/main/java/com/example/   UI screens, ViewModel, Room, Firestore
app/src/main/AndroidManifest.xml
docs/PLAY_STORE_READINESS.md     Google Play checklist
docs/store-listing.md            Store listing copy
docs/legal/                      Privacy policy and account deletion
.github/workflows/build-apk.yml  Sideload APK CI
scripts/build-apk.sh
```

---

## Privacy

Ledgers stay on the device unless you sign in and tap **Sync Cloud**. In-app policy: organization header → **Cloud & Account → Privacy Policy**. Hosted drafts: [docs/legal/privacy-policy.md](docs/legal/privacy-policy.md) and [docs/legal/account-deletion.md](docs/legal/account-deletion.md).

---

## Google Play

Do not upload `TexPro-ERP.apk` to Play production. New apps need a signed **Android App Bundle**. Checklist: [docs/PLAY_STORE_READINESS.md](docs/PLAY_STORE_READINESS.md).
