# Sofoste Android

Native Android client for the public Sofoste Music API at `https://sofoste.de/api/v1/`.
The application is an independent Kotlin/Jetpack Compose project and does not connect
directly to MariaDB.

## Download

The current public build is **Sofoste 0.5.0** for Android 7.0 or newer. Download it
only from the official GitHub release:

- [Download the latest signed APK](https://github.com/sofoste93/Sofoste/releases/latest/download/sofoste-latest.apk)
- [Browse every release](https://github.com/sofoste93/Sofoste/releases)

Android may ask you to allow installations from the browser or file manager used to
open the APK. This permission can be disabled again immediately after installation.
The release page publishes the SHA-256 checksum for independent verification.

### Install without the Play Store

1. Tap **Download the latest signed APK** and wait for the download to finish.
2. Tap **Open**. If that action does not appear, open the downloads in your browser
   or file manager, or use **Browse every release** and select `sofoste-latest.apk`
   from the latest release.
3. If Android blocks the installation, open the suggested settings and temporarily
   allow installations from the browser or file manager you used.
4. Return to the APK, start the installation and accept the Play Protect security
   check if Android offers it.
5. Install and open Sofoste. You can then revoke the browser installation permission
   in Android settings.

Only install APKs published in this official repository.

## Current orbit

- Public home signal, media, projects and journal.
- English, French and German content selection.
- Sofoste Aurora visual system with loading, empty and retry states.
- Public cards open their canonical localized page on `sofoste.de`.
- Production HTTPS by default; local emulator traffic is allowlisted only for `10.0.2.2`.
- Native student login and one-use invitation activation.
- Private student cockpit with overview, authenticated avatar and a manageable personal agenda.
- Native shared lesson archive, activity inbox with the complete shared note and explicit read acknowledgement,
  and personal billing/reminder detail with the allowlisted PayPal action.
- Native student profile, private avatar upload/removal and authenticated password change.
- Isolated four-hour administrator sign-in with a private crew profile, avatar,
  password rotation and read-only Mission Control telemetry.
- One active crew identity per device: entering the classroom closes the local admin
  session, and entering Mission Control closes the local student session.
- Icon navigation keeps Mission Control behind the discreet crew control in the header.
- Triton notification preferences for shared activity and 24-hour agenda reminders,
  backed by a revocable 90-day device token encrypted with Android Keystore.
- WorkManager checks the minimal private signal endpoint when a network is available;
  lock-screen notifications remain generic and never include lesson text.
- The student cookie is isolated from the public client, encrypted with Android
  Keystore at rest, capped to the server's eight-hour session and excluded from backup.
- Password recovery opens the existing localized HTTPS student space.

Triton uses background polling rather than immediate provider-backed push, so Android
may delay a signal under battery-saving conditions.

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

## Release integrity

Public APKs are built from the tagged source, optimized with R8 and signed with the
same long-lived Sofoste release certificate. The certificate SHA-256 digest is:

```text
65:F0:8B:EC:E0:FF:CE:86:C6:C2:58:03:F4:42:86:44:8B:50:77:D0:89:E2:E8:EA:EA:BA:FC:56:2F:34:AF:97
```

Version history and artifact checksums are recorded in [docs/RELEASES.md](docs/RELEASES.md).
Please report security issues using the private process in [SECURITY.md](SECURITY.md).

No database credentials, API secrets or production tokens belong in this repository.
