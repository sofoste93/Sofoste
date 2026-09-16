# Sofoste Android

Native Android client for the public Sofoste Music API at `https://sofoste.de/api/v1/`.
The application is an independent Kotlin/Jetpack Compose project and does not connect
directly to MariaDB.

## Current orbit

- Public home signal, media, projects and journal.
- English, French and German content selection.
- Sofoste Aurora visual system with loading, empty and retry states.
- Public cards open their canonical localized page on `sofoste.de`.
- Production HTTPS by default; local emulator traffic is allowlisted only for `10.0.2.2`.
- Native student login and one-use invitation activation.
- Private student cockpit with overview, authenticated avatar and personal agenda.
- Native shared lesson archive, activity inbox with explicit read acknowledgement,
  and personal billing/reminder detail with the allowlisted PayPal action.
- The student cookie is isolated from the public client, encrypted with Android
  Keystore at rest, capped to the server's eight-hour session and excluded from backup.
- Password recovery and profile editing open the existing localized HTTPS student space.

The next orbit prepares notification preferences and generic operating-system push
delivery. Private lesson text must never be placed on a lock screen.

## Build

Open this directory in Android Studio, let Gradle sync, then run the `app`
configuration. From PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

To point a development build at the PHP router from an Android emulator:

```powershell
.\gradlew.bat assembleDebug -PSOFOSTE_API_BASE_URL=http://10.0.2.2:8000/api/v1/
```

Run the Android checks with:

```powershell
.\gradlew.bat lintDebug assembleDebug
.\gradlew.bat connectedDebugAndroidTest
```

The device test uses the instrumentation package rather than application storage. It
verifies that the synthetic student cookie is encrypted, scoped to `sofoste.de` and
clearable without reading or changing a real student session.

No database credentials, API secrets or production tokens belong in this repository.
