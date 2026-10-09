# Security and Reliability Audit — Sunnah Android App

Audit target: `main` branch snapshot reviewed on 2026-10-09.
Merged to `main` in commit `c149a022aaacdc483808e29bc484bf23e77063f0`; launcher artwork and release version were subsequently updated.

## Confirmed issue fixed in this branch

- `SunnahOverlayService` is now declared as non-exported with the Android 14+ `specialUse` foreground-service type, subtype property, and matching permission.
- Restricted the exported boot receiver with `android.permission.RECEIVE_BOOT_COMPLETED`.
- Removed `fallbackToDestructiveMigration()` so a future schema mismatch cannot silently erase local progress. A future schema change must ship a tested migration; Room will fail explicitly rather than destroy data.
- Replaced template backup rules with explicit includes for the app database (which contains user progress) and the DataStore settings file for both cloud backup and device transfer.
- Enabled R8/resource shrinking for release and made release signing conditional on a supplied keystore plus all required environment variables. No signing secrets or keystore are committed.
- Updated the home screen with a branded petrol-teal daily Sunnah hero and shortcuts for the Sunnah catalogue and favorites; refined the global navy/teal/ivory palette and set new installs to dark mode.
- Launcher icon resources are generated from the existing `app/src/main/res/drawable/app_icon.png` asset for all legacy densities, and adaptive launcher foreground uses that repository artwork. App version is now `1.1.0` (`versionCode` 2).
- Added persistent favorites using Preferences DataStore, with add/remove controls on Sunnah details and a dedicated favorites screen. This avoids changing the Room schema for a user-created feature.
- Removed `USE_EXACT_ALARM`, which is restricted to eligible alarm/calendar use cases; reminders retain `SCHEDULE_EXACT_ALARM` with an inexact fallback when exact scheduling is denied.

## Remaining items requiring verification / remediation

1. **CI supply-chain hardening partially remediated.** Workflow actions are pinned to immutable commit SHAs, and hadith source downloads now use the resolved immutable upstream commit instead of a movable release tag. A separate checksum manifest for every downloaded JSON file is still not maintained.
2. **Migration coverage remains open.** Destructive fallback is removed, but no schema version bump or migration has been introduced. Add and test a Room migration whenever entities/schema change; a schema mismatch now fails safely instead of deleting user data.
3. **Release signing remains environment-dependent.** R8/resource shrinking is enabled and release builds are produced. A production-signed APK requires a private keystore and passwords supplied through GitHub Actions secrets or local environment variables; no private signing material is committed.
4. **Functional test coverage remains to be verified.** Test notification permission denial, exact-alarm permission denial, reboot rescheduling, overlay permission revocation, database initialization, migration behavior, and Arabic search on supported Android versions.
5. **Full security review still required.** This is a focused code review, not a guarantee that the entire codebase is vulnerability-free. Run dependency scanning, static analysis, and full automated/device tests before release.

## Production signing setup

The workflow can produce a signed Release APK and AAB when these protected GitHub Actions repository secrets are configured:

- `ANDROID_KEYSTORE_BASE64`: the release keystore file encoded as Base64.
- `ANDROID_STORE_PASSWORD`: keystore password.
- `ANDROID_KEY_PASSWORD`: private-key password.
- `ANDROID_KEY_ALIAS`: alias of the signing key.

Keep the keystore and passwords private. Never commit them to the repository or print them in workflow logs. If the secrets are absent, CI still builds Release outputs, but those outputs are not signed with a production key; use the signed Debug APK only for testing.

## UI direction

Use the supplied reference as the target: Arabic RTL layout; deep navy/teal surfaces; restrained turquoise highlights; warm ivory reading screen; clear hadith source/grade metadata; accessible type sizing; consistent bottom navigation; explicit empty/loading/error states. The launcher uses the repository's existing app_icon.png asset. Preserve repository/database and notification behavior while refactoring composables.
