# TexPro ERP

Android accounting app for textile converters and weaving / dyeing / stitching mills.

Track a customer **sale order** through **grey cloth purchase**, **dyeing and printing**, **CMT stitching**, and **finished-goods invoicing**. Each step posts categorized vouchers to a double-entry general ledger so lot cost, COGS, and margin stay on the books.

| | |
| --- | --- |
| Platform | Android 7.0+ (`minSdk` 24, `targetSdk` 36) |
| Package | `com.aistudio.ledgerproerp.qzmxyt` |
| Version | 1.0 |
| Stack | Kotlin, Jetpack Compose, Room, Firebase (optional cloud) |

---

## Download the APK

The installable file is **`TexPro-ERP.apk`** (about 28 MB). It is a sideload build, not a Google Play package.

**Latest CI artifact:** [Actions run #37852544710](https://github.com/bilalpiaic/TexPro-ERP/actions/runs/37852544710) → download **TexPro-ERP-apk** → unzip `TexPro-ERP.apk`.

Or: **Actions → Build downloadable APK** → open a green run → artifact `TexPro-ERP-apk`.

After a manual **Run workflow**, a stable release URL is:

```text
https://github.com/bilalpiaic/TexPro-ERP/releases/download/sideload/TexPro-ERP.apk
```

### Install on a phone

1. Copy `TexPro-ERP.apk` to the device (USB, Drive, or send it to yourself).
2. Open **Settings → Security** and allow **Install unknown apps** for Files / Chrome / Drive.
3. Tap the APK and install **TexPro ERP**.
4. First launch includes **sample mill data** so you can walk the workflow before entering live books.

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

**Organizations** — create mill profiles locally. Optional Google Sign-In and Firestore sync for the active tenant.

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

Put Firebase config at `app/google-services.json` (gitignored) if you want cloud login. Without it, local accounting still builds and runs.

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
