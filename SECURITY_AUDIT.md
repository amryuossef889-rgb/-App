# Security and Reliability Audit — Sunnah Android App

Audit target: `main` branch snapshot reviewed on 2026-10-09.
Working branch: `audit/security-ui-refresh` (proposed changes in PR #3).

## Confirmed issue fixed in this branch

- `SunnahOverlayService` is now declared as non-exported with the Android 14+ `specialUse` foreground-service type, subtype property, and matching permission.
- Restricted the exported boot receiver with `android.permission.RECEIVE_BOOT_COMPLETED`.
- Removed `fallbackToDestructiveMigration()` so a future schema mismatch cannot silently erase local progress. A future schema change must ship a tested migration; Room will fail explicitly rather than destroy data.
- Replaced template backup rules with explicit includes for the app database (which contains user progress) and the DataStore settings file for both cloud backup and device transfer.
- Enabled R8/resource shrinking for release and made release signing conditional on a supplied keystore plus all required environment variables. No signing secrets or keystore are committed.
- Updated the home screen with a branded petrol-teal daily Sunnah hero and set new installs to the dark theme; refined the global navy/teal/ivory palette.

## Remaining items requiring verification / remediation

1. **CI supply-chain hardening partially remediated.** Workflow actions are pinned to immutable commit SHAs, and hadith source downloads now use the resolved immutable upstream commit instead of a movable release tag. A separate checksum manifest for every downloaded JSON file is still not maintained.
2. **Migration coverage remains open.** Destructive fallback is removed, but no schema version bump or migration has been introduced. Add and test a Room migration whenever entities/schema change; a schema mismatch now fails safely instead of deleting user data.
3. **Release validation is in progress.** R8/resource shrinking is enabled; verify release build and run smoke tests. A production-signed APK requires a private keystore and passwords supplied through GitHub Actions secrets or local environment variables.
4. **Functional test coverage remains to be verified.** Test notification permission denial, exact-alarm permission denial, reboot rescheduling, overlay permission revocation, database initialization, migration behavior, and Arabic search on supported Android versions.
5. **Full security review still required.** This is a focused code review, not a guarantee that the entire codebase is vulnerability-free. Run dependency scanning, static analysis, and full automated/device tests before release.

## UI direction

Use the supplied reference as the target: Arabic RTL layout; deep navy/teal surfaces; restrained turquoise highlights; warm ivory reading screen; clear hadith source/grade metadata; accessible type sizing; consistent bottom navigation; explicit empty/loading/error states. Preserve repository/database and notification behavior while refactoring composables.
