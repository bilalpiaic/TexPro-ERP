# APK deployment

The repository builds a downloadable debug APK with GitHub Actions.

## Download an APK from a branch build

1. Open the repository's **Actions** tab.
2. Select **Build Android APK** and open a successful run.
3. Download the `texpro-erp-debug-apk` artifact.
4. Extract the downloaded archive and install `app-debug.apk` on an Android device.

The workflow runs for pushes to `main` and feature branches, pull requests, and manual workflow dispatches. Artifacts are retained for 30 days.

## Create a persistent release download

Push a version tag after merging the desired code:

```text
v1.0.0
```

The same workflow builds the APK and attaches `app-debug.apk` to a GitHub Release for that tag. Release APKs are debug builds intended for testing and internal distribution, not Google Play publication.
