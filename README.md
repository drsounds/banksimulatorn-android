# Banking Simulator

**Banking Simulator** is a robust and energetic Android application designed to simulate modern personal banking experiences. This application serves as a showcase for high-energy financial management through a sleek, adaptive interface following the latest Material Design 3 guidelines.

## 🚀 Key Features

*   **Account Dashboard**: A comprehensive overview of checking and savings accounts with real-time balance updates and expressive visual indicators.
*   **Transaction Simulator**: An interactive interface to perform mock **Deposits**, **Withdrawals**, and **Transfers**. It includes atomic multi-account updates and validation (e.g., insufficient funds).
*   **Adaptive History View**: A sophisticated transaction history that utilizes the **List-Detail pattern**. It automatically adjusts to a single-pane view on phones and a multi-pane layout on tablets and foldables.
*   **Dynamic Material 3 Themes**: A vibrant, high-energy aesthetic with full support for **Dynamic Color** (Android 12+) and seamless Light/Dark mode transitions.
*   **Full Edge-to-Edge**: An immersive experience that respects system bars and utilizes the entire display area.

## 🛠 Tech Stack

*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Pure Compose, no XML)
*   **Navigation**: **Navigation 3** (State-driven, type-safe navigation)
*   **Adaptive Layouts**: Compose Material 3 Adaptive (ListDetailPaneScaffold)
*   **Database**: Room Persistence Library (with reactive Flow support)
*   **Asynchronous Logic**: Kotlin Coroutines & Flow
*   **Dependency Management**: Version Catalogs (libs.versions.toml)

## 📦 Project Structure

- `app/src/main/java/se/banksimulatorn/app/data`: Room database entities, DAO, and database configuration.
- `app/src/main/java/se/banksimulatorn/app/navigation`: Type-safe navigation routes and backstack management.
- `app/src/main/java/se/banksimulatorn/app/ui`: Jetpack Compose screens and ViewModels.
    - `dashboard/`: Account summary and balances.
    - `transactions/`: Transaction forms and simulators.
    - `history/`: Adaptive list-detail transaction logs.
    - `theme/`: Material 3 color schemes, typography, and shapes.

## 🔨 Building the Project

1.  **Clone the repository**:
    ```bash
    git clone <repository-url>
    ```
2.  **Open in Android Studio**:
    Open the project using **Android Studio Ladybug (2024.2.1)** or newer.
3.  **Gradle Sync**:
    Wait for the Gradle sync to complete. Ensure you have the **Android SDK 37** (or newer) installed.
4.  **Run**:
    Press the "Run" button in Android Studio or use the following command to build the debug APK:
    ```bash
    ./gradlew :app:assembleDebug
    ```

## 🎨 Design Philosophy

The app is built with an **Expressive M3** design language. This includes:
- Larger corner radii and spacious padding.
- High-contrast colors for financial states (e.g., green for deposits, red for withdrawals).
- Motion-driven UI transitions between screens and adaptive panes.
- An **Adaptive App Icon** featuring a vibrant gradient and minimalist iconography.

## 🚀 Google Play Release (GitHub Actions)

Two manually-triggered workflows handle release builds. Neither runs automatically on push — both are started from the **Actions** tab via "Run workflow".

- **`android-build.yml`** — builds and signs the release AAB, then uploads it as a downloadable workflow artifact (`release-aab`, kept 14 days). Run this on its own whenever you just need a signed AAB, e.g. to upload manually via the Play Console (including the required first-ever release — see below).
- **`android-play-release.yml`** — calls `android-build.yml` as its `build` job, then a separate `publish` job downloads that artifact and uploads it to a Google Play testing track (defaults to **internal**) via the Play Developer API. Because build and publish are separate jobs, if publishing fails (bad credentials, track misconfigured, etc.) GitHub's "Re-run failed jobs" re-runs only `publish` and reuses the already-built AAB — no rebuild needed.

**Signing model**: Google Play manages the actual app signing key ([Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756), enabled by default for new apps). CI only needs an **upload key** — a keystore used to sign the bundle you hand to Google, which Google then re-signs with the real distribution key it holds. Losing the upload key isn't fatal (Google can help you reset it via a support request), but treat it as a long-lived credential — it stays valid for every future release.

Configure these repository secrets before running it:

| Secret | Description |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | The upload keystore file, base64-encoded (`base64 -w0 upload-keystore.jks`). |
| `ANDROID_KEYSTORE_PASSWORD` | Store password for the keystore. |
| `ANDROID_KEY_ALIAS` | Alias of the upload key inside the keystore. |
| `ANDROID_KEY_PASSWORD` | Password for the key itself (same as the store password for a PKCS12 keystore). |
| `PLAY_SERVICE_ACCOUNT_JSON` | JSON key of a Google Play service account with "Release Manager" access to this app, pasted as plain text. |

The service account must be linked in Google Play Console under **Setup → API access**, with permission to manage releases on the `se.banksimulatorn.app` package.

**One-time setup before the first `android-play-release.yml` run:**
1. The Play Developer API cannot create an app's very first release — Play Console requires that to happen through its web UI. Run `android-build.yml`, download the `release-aab` artifact from the run, and manually upload it via **Play Console → Release → Internal testing → Create release** to establish the app listing and enroll it in Play App Signing.
2. After that first manual release exists, all subsequent releases can go through `android-play-release.yml`.

---
*Developed as a modern Android simulation project.*
