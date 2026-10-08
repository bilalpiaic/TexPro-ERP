# TexPro ERP account deletion

Host this page on a public HTTPS URL and enter that URL in Play Console → App content → Account deletion. Google Play requires both an in-app path and a web path for apps that let users create accounts.

## Delete from the app

1. Open TexPro ERP.
2. Tap the organization header at the top of the screen.
3. Open the **Cloud & Account** tab.
4. Sign in with Google if you are not already signed in.
5. Tap **Delete account** and confirm.

This deletes your Firebase Authentication user and the Firestore profile document for your user ID.

## Delete by email (web request)

Email the support address listed on the Google Play store listing from the Google account you used to sign in. Include:

- The Google account email
- The subject line `TexPro ERP account deletion`

We will delete the Firebase Auth user and Firestore user profile associated with that email.

## What is not deleted automatically

- Local mill ledgers stored on devices you installed the app on. Uninstall the app or clear app storage on each device to remove those files.
- Organization records that other members still belong to. An organization owner can request those records be removed separately.
