# TexPro ERP

Android textile mill accounting app (sale orders, lots, vouchers, general ledger).

## Download the APK

After CI finishes, get **TexPro-ERP.apk** from:

1. **Actions** → **Build downloadable APK** → latest run → artifact `TexPro-ERP-apk`
2. Or **Releases** after you run the workflow manually (stable file: `sideload/TexPro-ERP.apk`)

Install on a phone: enable **Install unknown apps**, then open the APK. Details: [DEPLOYMENT.md](DEPLOYMENT.md).

Build locally:

```bash
./scripts/build-apk.sh
# dist/TexPro-ERP.apk
```

Google Play publication uses an AAB, not this APK. See [docs/PLAY_STORE_READINESS.md](docs/PLAY_STORE_READINESS.md).
