# Invoice App - Android

Android application for invoice scanning and classification using Kotlin and Jetpack Compose.

## Tech Stack

- **Kotlin** - Programming language
- **Jetpack Compose** - Modern UI toolkit
- **Material Design 3** - UI components
- **Hilt** - Dependency injection
- **Retrofit** - Networking
- **Room** - Local database
- **CameraX** - Camera integration
- **ML Kit** - Document scanning
- **Coil** - Image loading

## Architecture

- **MVVM** - Model-View-ViewModel pattern
- **Clean Architecture** - Separation of concerns
- **Repository Pattern** - Data abstraction
- **Use Cases** - Business logic encapsulation

## Project Structure

```
app/src/main/java/com/invoiceapp/
├── data/               # Data layer
│   ├── remote/         # API services
│   ├── local/          # Room database
│   └── repository/     # Repository implementations
├── domain/             # Domain layer
│   ├── model/          # Domain models
│   ├── usecase/        # Business logic
│   └── repository/     # Repository interfaces
├── ui/                 # Presentation layer
│   ├── auth/           # Login/Register screens
│   ├── invoice/        # Invoice screens
│   ├── scanner/        # Camera scanner
│   ├── analytics/      # Analytics/Charts
│   └── settings/       # Settings screen
├── di/                 # Dependency injection modules
└── util/               # Utilities and helpers
```

## Key Features

- 📸 Invoice scanning with camera
- 🤖 AI-powered data extraction
- 📊 Spending analytics with charts
- 🏷️ Category management
- 💾 Offline support
- 🔄 Auto-sync
- 🔒 Biometric authentication
- 🌙 Dark mode support

## Setup

1. Install Android Studio Hedgehog or later
2. Clone the repository
3. Open the `android/` folder in Android Studio
4. Update API endpoint in `app/build.gradle` if needed
5. Sync Gradle
6. Run on emulator or device

## Configuration

Update `BuildConfig.API_BASE_URL` for different environments:

- **Debug**: `http://10.0.2.2:8080/api` (localhost from emulator)
- **Release**: `https://api.invoice.com/api` (production)

## Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

## Requirements

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Kotlin**: 1.9.20+
- **Gradle**: 8.2+
- **JDK**: 17+

## Development Status

🚧 **Under Development**

Core functionality planned:
- ✅ Project structure setup
- ✅ Dependencies configured
- ⏳ API client implementation
- ⏳ Authentication flow
- ⏳ Invoice scanning UI
- ⏳ Category management
- ⏳ Analytics dashboard
- ⏳ Settings screen

## License

Copyright © 2025 Invoice App
