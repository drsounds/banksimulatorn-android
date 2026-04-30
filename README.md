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

---
*Developed as a modern Android simulation project.*
