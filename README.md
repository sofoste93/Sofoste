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

The authenticated student space, lesson archive, billing reminders and activity
notifications are the next orbit. Its server contract already exists under
`/api/v1/student/*` and requires a private cookie jar plus CSRF handling.

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

No database credentials, API secrets or production tokens belong in this repository.
