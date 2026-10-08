# TexPro ERP — Google Play Store readiness

Review date: 8 October 2026  
App: **TexPro ERP** (`com.aistudio.ledgerproerp.qzmxyt`)  
Platform: native Android (Kotlin, Jetpack Compose, Room, Firebase)

**Verdict: not ready to publish to production.** Target API 36 is already correct. Remaining blockers are Play Console setup, Firebase/signing files that cannot live in git, store assets, and a few product risks reviewers will see.

This document is the punch list. Code in this branch hardens the APK/AAB for policy (privacy, account deletion, backup, network, release shrinking). It does not replace a Play Console account or a hosted privacy URL.

---

## What already meets Play technical gates

| Requirement (2026) | Status |
| --- | --- |
| New apps must target API 36 (Android 16) by 31 Aug 2026 | Pass — `targetSdk = 36`, `compileSdk` 36 |
| `minSdk` 24 | Pass — covers Play’s practical device range |
| Upload as Android App Bundle (AAB), not APK | Build with `bundleRelease` (see CI). Do **not** upload the GitHub debug APK |
| 16 KB page size for native code | Pass by default — no NDK / `.so` libraries |
| 64-bit | Pass by default — Kotlin/JVM only |
| Unique `applicationId` | Present, but it is an AI Studio id (see below) |
| Launcher icon + app label | Present (`TexPro ERP`) |
| Exported launcher activity | Pass |
| No SMS, contacts, camera, location, or photos permissions | Pass — keep it that way unless the listing explains a new feature |

---

## Blockers before first production upload

### 1. Play Console account and testing track

- Register a [Google Play developer account](https://play.google.com/console) (one-time fee).
- Personal accounts created after 13 Nov 2023 must run a **closed test with at least 12 opted-in testers for 14 continuous days** before production.
- Use the internal testing track first, then closed testing, then production.
- The existing `bilalpiaic-deploy-apk` workflow publishes a **debug APK** to GitHub. That is useful for testers, **not** for Play.

### 2. Change or accept the application id before the first upload

Current id:

```text
com.aistudio.ledgerproerp.qzmxyt
```

Play treats this as permanent. After the first upload you cannot change it. Prefer a domain you control, for example `com.texproerp.app` or `pk.com.texpro.erp`, **before** production. If you keep the AI Studio id, you must still own the Play listing and the Firebase project that matches it.

The Kotlin namespace is still `com.example`. Play does not reject that, but you should rename packages in a later cleanup so the codebase matches the store id.

### 3. Upload keystore + Play App Signing

Release signing is wired in `app/build.gradle.kts` when these exist:

- File: `KEYSTORE_PATH` or `my-upload-key.jks`
- Env: `STORE_PASSWORD`, `KEY_PASSWORD`, optional `KEY_ALIAS` (default `upload`)

Generate an upload key locally (never commit `*.jks`):

```bash
keytool -genkey -v -keystore my-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

In Play Console enable **Play App Signing**. Upload the AAB signed with the upload key. Google holds the app-signing key.

GitHub Actions secrets to add for a signed bundle:

- `KEYSTORE_BASE64`
- `STORE_PASSWORD`
- `KEY_PASSWORD`
- `KEY_ALIAS`

### 4. `google-services.json` and SHA-1 fingerprints

The repo has no `google-services.json`. Google Sign-In will not work for Play reviewers (or you) until you:

1. Create / select a Firebase project.
2. Register Android app id `com.aistudio.ledgerproerp.qzmxyt` (or the new id).
3. Add the **debug** and **upload / Play App Signing** SHA-1 and SHA-256 certificate fingerprints.
4. Download `google-services.json` into `app/` (gitignored).
5. Confirm OAuth client / `default_web_client_id` for Credential Manager.
6. Deploy `firestore.rules` from this repo to that project.

Without this, tapping **Sign in with Google** fails. The rest of the ledger still runs locally, which is enough for a basic review if you disclose that cloud login is optional.

### 5. Hosted privacy policy + Data safety form

Play requires a **public HTTPS privacy policy URL** on the store listing. In-app policy text now lives under Cloud & Account → Privacy Policy. Host `docs/legal/privacy-policy.md` (GitHub Pages, your mill site, or equivalent) and paste the URL into Play Console.

Complete **App content → Data safety**. Suggested declarations for the current code:

| Data type | Collected? | Shared? | Required / optional | Purpose |
| --- | --- | --- | --- | --- |
| Name | Yes (Google display name, if signed in) | Yes — Firebase | Optional | App functionality |
| Email | Yes (if signed in) | Yes — Firebase | Optional | App functionality, account management |
| User IDs | Yes (Firebase UID) | Yes — Firebase | Optional | App functionality |
| Financial info | Yes — mill ledgers (on device; cloud only after Sync) | Yes — Firestore if user syncs | Optional | App functionality |
| App activity | No analytics SDK today | No | — | — |
| Location / photos / contacts | No | No | — | — |

Turn **ads** and **data sold** off. Encryption in transit: yes (HTTPS to Google). Users can request deletion: yes (in-app + web).

### 6. Account deletion (in-app + web)

Play policy for apps that create accounts:

- In-app deletion — **added** in Cloud & Account → Delete account
- Web URL — host `docs/legal/account-deletion.md` and enter it in App content → Account deletion
- Associated cloud user profile is deleted; local ledgers stay until uninstall (disclosed)

Deploy the updated `firestore.rules` so users can delete `/users/{userId}`.

### 7. Store listing assets (not in git)

Create and upload:

- 512×512 high-res icon
- 1024×500 feature graphic
- At least two phone screenshots of real screens (Dashboard, Lots, Journal, Reports)
- Short and full description from `docs/store-listing.md`

The adaptive icon uses a JPEG drawable. Replace it with a clean PNG brand mark before store screenshots.

### 8. Build a signed AAB

```bash
./gradlew bundleRelease
# output: app/build/outputs/bundle/release/app-release.aab
```

This repo currently has Gradle wrapper properties but no `gradlew` script. Use Android Studio’s Generate App Bundle, or the GitHub Action `Build Play App Bundle`, which installs Gradle 9.3.1.

Do not enable Play production until you have tested `bundleRelease` on a device (`bundletool` or internal testing track).

---

## Issues fixed in this branch

- Declared `INTERNET` and `ACCESS_NETWORK_STATE`
- Disabled Auto Backup of financial databases
- Blocked cleartext HTTP
- Release R8 minify/shrink; upload signing only when a keystore is present
- In-app privacy policy
- In-app account deletion (Firebase Auth + Firestore profile)
- Firestore rules: user delete allowed; organization list limited to members
- Cloud sync actually uploads tenant data (it previously showed success without writing)
- Cloud header no longer claims “connected” while signed out
- App Check debug artifact is debug-only
- Keystores and `google-services.json` gitignored
- Play bundle CI workflow

---

## Remaining product / review risks (not fully fixed here)

1. **Tenant isolation is local-UI only.** Switching organizations changes the header but the same Room ledger is shown for every mill. Reviewers who create two companies will see shared books. Real SaaS isolation needs per-org queries (or separate databases) before you market multi-tenant security.

2. **Seed demo data looks like a live mill.** First launch inserts TexPro Fabrics Ltd. and Crescent Weaving with NTN numbers, balances, and sale orders. Disclose this in the listing (“sample data for the workflow”). Consider a first-run “Start with sample mill / empty books” choice.

3. **Package name `com.example` and leftover AI Studio strings** (Firestore database id `ai-studio-android-texproer-…`). Fine for a prototype; rename before a long-term brand.

4. **Unused Firebase AI / Gemini dependency** (`firebase-ai`, `.env.example` GEMINI key). Not called from Kotlin. Remove it before Play if you will not ship AI, so Data safety stays accurate.

5. **Hard-coded voucher account ids** (`accountId = 4`, `9`, …) in `ErpRepository`. Demo chart of accounts matches those ids; a mill that edits the chart can post to the wrong accounts. Not a Play policy issue, but testers will hit it.

6. **Destructive Room migrations** (`fallbackToDestructiveMigration()`). A future schema bump wipes mill data. Ship a real migration before production users depend on the app.

7. **Misleading “Enterprise / zero-trust” copy** was removed from the account dialog. Do not put it back on the store listing.

8. **Restore Credentials / device-to-device sign-in** is a coming Play quality rule for apps with login. Not required for this first upload; plan it after you ship.

9. **No Gradle wrapper scripts** (`gradlew`). Android Studio can generate them. CI uses `gradle/actions/setup-gradle`.

---

## Play Console declaration cheat sheet

| Console section | Recommended value |
| --- | --- |
| App category | Business |
| Target audience | 18+ business users; not for children |
| News / COVID / government apps | No |
| Financial features | Yes — accounting / business finance for the user’s own mill (not a bank, wallet, or brokerage) |
| Ads | No |
| Content rating | IARC questionnaire; business records only |
| Privileged / sensitive permissions | None beyond Internet |
| Government apps | No |
| Data safety | See table above |
| Privacy policy URL | Hosted copy of `docs/legal/privacy-policy.md` |
| Account deletion URL | Hosted copy of `docs/legal/account-deletion.md` |

---

## Suggested publish sequence

1. Decide final `applicationId` and Firebase project.
2. Put `google-services.json` in `app/` and deploy Firestore rules.
3. Generate upload keystore; enable Play App Signing.
4. Host privacy + account-deletion pages.
5. Build `bundleRelease`, upload to **internal testing**, install on a phone, walk Dashboard → Lots → Journal → Reports → Google sign-in → Delete account.
6. Complete Data safety, content rating, and store listing.
7. Closed testing (12 testers / 14 days if your developer account requires it).
8. Promote to production.

Until steps 1–6 are done, treat the app as an internal mill prototype, not a Play Store release.
