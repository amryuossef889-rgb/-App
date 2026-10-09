# Security and Reliability Audit — Sunnah Android App

Audit target: `main` branch snapshot reviewed on 2026-10-09.
Working branch: `audit/security-ui-refresh`.

## Confirmed issue fixed in this branch

- `SunnahOverlayService` is started as a foreground service, but it was not declared in `AndroidManifest.xml`. Android can reject service startup and the persistent overlay feature may fail. Added a non-exported service declaration with the Android 14+ `specialUse` foreground-service type and required subtype property.
- Restricted the exported boot receiver with `android.permission.RECEIVE_BOOT_COMPLETED` so arbitrary third-party apps cannot directly invoke it.

## Remaining items requiring verification / remediation

1. **Backup policy needs an explicit decision.** The manifest enables Android backup while backup rule files contain only templates/comments. Verify which app data is included and explicitly exclude transient/private state where appropriate.
2. **Database migration risk.** `AppDatabase` uses `fallbackToDestructiveMigration()`; a schema version change without a migration can delete user progress and library data. Replace with tested migrations before changing the schema version.
3. **Release hardening.** Release builds currently disable minification. Evaluate R8/minification and run release smoke tests before enabling it; signing depends on environment variables and a local keystore path, so validate the release pipeline without committing signing secrets.
4. **CI supply-chain hardening.** GitHub Actions are referenced by major-version tags, and the workflow downloads pinned-version source JSON without checksum verification. Pin actions to reviewed commit SHAs and verify downloaded source checksums.
5. **Functional test coverage.** Verify notification permission denial, exact-alarm permission denial, reboot rescheduling, overlay permission revocation, database initialization, migrations, and Arabic search on supported Android versions.
6. **Full security review still required.** This review is based on selected source files and repository metadata; it is not a guarantee that the entire codebase is free of vulnerabilities. Run dependency scanning, static analysis, and a full build/test suite before release.

## UI direction

Use the supplied reference as the target: Arabic RTL layout; deep navy/teal surfaces; restrained turquoise highlights; warm ivory reading screen; clear hadith source/grade metadata; accessible type sizing; consistent bottom navigation; explicit empty/loading/error states. Preserve repository/database and notification behavior while refactoring composables.
