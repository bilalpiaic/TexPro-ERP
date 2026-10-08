# TexPro ERP Privacy Policy

Last updated: 8 October 2026

TexPro ERP (“the app”) is a textile manufacturing accounting application for mill operators. This policy describes how the app handles information when you install it from Google Play or run a signed release build.

## Data we process

### Stored only on your device
Sale orders, production lots, vouchers, chart of accounts, and organization profiles are stored in a local Android database (`texpro_textile_erp.db`). Cloud backup of this database is disabled.

### Collected if you sign in with Google
- Google account identifier (Firebase Auth UID)
- Email address
- Display name

These values are sent to Firebase Authentication. The app may also write a user profile document in Cloud Firestore so you can attach the active organization to your account.

### Uploaded if you choose Sync Cloud
When you tap **Sync Cloud** while signed in, the active organization’s accounts, vouchers, lots, and sale orders are uploaded to Cloud Firestore under that organization ID.

## How we use data

We use this information only to:

- Provide mill accounting, lot tracking, and financial reports
- Authenticate you with Google
- Sync a tenant you explicitly choose to upload

We do not sell personal data, show third-party ads, or use mill ledgers for advertising or credit scoring.

## Sharing

Cloud processing uses Google Firebase (Authentication and Firestore) as the infrastructure provider. We do not share mill data with other third parties except as required by law.

## Retention and deletion

- Local data remains until you uninstall the app or clear app storage.
- Signed-in users can delete their account in the app: open the organization header → **Cloud & Account** → **Delete account**. That removes the Firebase Auth user and the Firestore user profile.
- Organization ledgers that other members still use are not automatically deleted.

A web copy of this deletion process is in `docs/legal/account-deletion.md`. Host that page publicly and paste the URL into Play Console → App content → Account deletion.

## Children

The app is intended for business users. It is not directed at children.

## Contact

Use the support email listed on the Google Play store listing for privacy requests.
