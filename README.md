# The Development of Prefect Management System for the Office of Student Discipline (OSD) – Mobile

## Description

The OSD Mobile App is the student-facing Android companion to the Prefect Management System for the Office of Student Discipline. While prefects manage cases through the desktop application, this mobile app gives students direct, secure access to their own disciplinary information.

The application enables students to view their disciplinary records, check the details and sanctions of each offense, file and track appeals, and view their personal and guardian information. By bringing these services to students' phones, the system improves transparency, reduces the need for in-person inquiries at the Office of Student Discipline, and speeds up communication between students and prefects.

## Features

- Dashboard: Shows a welcome summary with the student's program, section, and student ID, plus a quick overview of offenses and appeals.
- Disciplinary Records: Lets students view their list of offenses and open each one for its details and disciplinary action.
- Appeals: Allows students to file an appeal against a recorded offense and track the status of their submitted appeals.
- Profile: Displays personal information and registered guardians.
- Chat: Provides in-app chat support for questions about disciplinary matters.
- Dark Mode: Lets students switch between light and dark themes.
- User Authentication: Secure login and logout using token-based (JWT) authentication.

## Technologies Used

- Kotlin
- Jetpack Compose (Material for Mobile UI)
- MVVM Architecture
- Repository Design Pattern
- Retrofit & OkHttp (REST API communication)
- Jetpack DataStore (session and preference storage)
- Navigation Compose
- Gradle (Kotlin DSL)

## Setup Development Environment

Follow these steps to run the app locally:

1. Install Required Software
   - Android Studio (Ladybug or newer)
   - JDK 17
   - Android SDK 34
   - Git
2. Clone the Repository
   ```bash
   git clone https://github.com/bsit5012025/rc-osd-mobile.git
   ```
3. Open the Project in Android Studio
   - Launch Android Studio.
   - Select **Open** and navigate to the project folder.
   - Wait for the Gradle sync to finish and all dependencies to download.
4. Start the Backend API
   - Make sure the OSD backend server and its Oracle Database are running.
   - By default, the app connects to the API on port `8080`.
5. Configure the API Connection
   - Open `app/build.gradle.kts`.
   - Update the `API_BASE_URL` value if needed:
     - Android Emulator: `http://10.0.2.2:8080/` (default, points to your computer's localhost)
     - Physical device: `http://<your-computer-IP>:8080/` (phone and computer must be on the same network)
6. Build and Run the App
   - Create or start an Android Virtual Device (AVD), or connect a physical device with USB debugging enabled.
   - Click *Run ▶ to build and install the app.
   - Log in using a registered student account.

## System Requirements

- Android 8.0 (Oreo, API level 26) or higher
- Minimum 2 GB RAM
- Internet or local network connection to the OSD backend server

## Contributors

- Carl Justine Cain
- Mark Joshua Camama
- John Zenith Cruz
- Geoffrey Allen De Rojas
