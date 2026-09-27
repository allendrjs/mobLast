# Sharing the APK via Firebase App Distribution

This wires the mobile app up to Firebase App Distribution so a release build
can be shared with testers without going through the Play Store. It does
**not** add Firebase to the app itself — no Analytics, Auth, Firestore, etc.
This is a build-time tool only; nothing in the running app changes.

## One-time setup (do this once)

1. **Create (or reuse) a Firebase project** at https://console.firebase.google.com.
   If you already have a Firebase project you use for distributing other
   APKs, you can add this app to that same project instead of making a new one.

2. **Register the Android app** in that project: Project settings → Add app →
   Android. Use the applicationId `org.rocs.osda.mobile` (must match exactly).
   You don't need to download `google-services.json` for this — App
   Distribution alone doesn't require it.

3. **Copy the App ID.** After registering, Firebase shows an App ID that
   looks like `1:1234567890:android:abcd1234efgh5678`. Put it in
   `app/build.gradle.kts`, replacing `REPLACE_WITH_FIREBASE_APP_ID`
   — or better, keep it out of the committed file and pass it at build time
   instead (see "Running a distribution build" below).

4. **Create a tester group.** In the Firebase console, go to App
   Distribution → Testers & Groups → add a group named `testers` (or
   change the `groups = "testers"` line in `app/build.gradle.kts` to
   whatever you name it) and add the email addresses of whoever should
   receive builds.

5. **Install the Firebase CLI and log in**, once, on whichever machine will
   run the upload:
   ```powershell
   npm install -g firebase-tools
   firebase login
   ```
   This opens a browser to sign in with the Google account that owns the
   Firebase project. The Gradle plugin reuses this login automatically —
   no separate credentials file needed for a single developer running
   builds locally.

## Running a distribution build

```powershell
cd C:\Users\Geo\StudioProjects\rc-osd-mobile
./gradlew assembleRelease appDistributionUploadRelease -PfirebaseAppId=1:1234567890:android:abcd1234efgh5678
```

(`-PfirebaseAppId=...` overrides the placeholder in `build.gradle.kts` for
this run, so the real App ID doesn't have to sit in a file that's tracked in
git. Once you've filled in the real value directly in `build.gradle.kts`,
you can drop this flag.)

Testers in the group get an email with a link to install the build directly
on their device (through the Firebase App Distribution app, which walks
them through allowing installs from an unknown source the first time).

## If you'd rather automate this (optional, later)

This can run from a GitHub Action on every push to `master` instead of
manually from your machine, using a Firebase service account key stored as
a GitHub secret instead of `firebase login`. Worth doing once builds are
frequent enough that manual uploads get tedious — not necessary to set up
now.
