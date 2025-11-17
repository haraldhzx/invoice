# Invoice App - iOS

iOS application for invoice scanning and classification using Swift and SwiftUI.

## Tech Stack

- **Swift 5.9+** - Programming language
- **SwiftUI** - Modern UI framework
- **Combine** - Reactive programming
- **async/await** - Concurrency
- **Core Data** - Local persistence
- **URLSession** - Networking
- **VisionKit** - Document scanning
- **AVFoundation** - Camera

## Architecture

- **MVVM** - Model-View-ViewModel pattern
- **Clean Architecture** - Separation of concerns
- **Repository Pattern** - Data abstraction
- **Dependency Injection** - Loose coupling

## Project Structure

```
InvoiceApp/
├── Data/                   # Data layer
│   ├── Network/            # API services
│   ├── Local/              # Core Data
│   └── Repositories/       # Repository implementations
├── Domain/                 # Domain layer
│   ├── Models/             # Domain models
│   ├── UseCases/           # Business logic
│   └── RepositoryProtocols/# Repository interfaces
├── Presentation/           # Presentation layer
│   ├── Auth/               # Login/Register views
│   ├── Invoice/            # Invoice views
│   ├── Scanner/            # Document scanner
│   ├── Analytics/          # Charts and analytics
│   └── Settings/           # Settings view
├── DI/                     # Dependency injection
└── Utilities/              # Utilities and helpers
```

## Key Features

- 📸 Invoice scanning with VisionKit
- 🤖 AI-powered data extraction
- 📊 Spending analytics with Swift Charts
- 🏷️ Category management
- 💾 Offline support with Core Data
- 🔄 Auto-sync
- 🔒 Face ID / Touch ID authentication
- 🌙 Dark mode support
- 📱 iPad support

## Setup

1. Install Xcode 15 or later
2. Clone the repository
3. Open `InvoiceApp.xcodeproj` in Xcode
4. Update API endpoint in `Config.swift` if needed
5. Build and run (⌘+R)

## Configuration

Update API endpoint in `Config.swift`:

```swift
enum Config {
    static let apiBaseURL: String = {
        #if DEBUG
        return "http://localhost:8080/api"
        #else
        return "https://api.invoice.com/api"
        #endif
    }()
}
```

## Building

```bash
# Build for simulator
xcodebuild -scheme InvoiceApp -destination 'platform=iOS Simulator,name=iPhone 15'

# Build for device
xcodebuild -scheme InvoiceApp -destination 'generic/platform=iOS'

# Run tests
xcodebuild test -scheme InvoiceApp -destination 'platform=iOS Simulator,name=iPhone 15'
```

## Requirements

- **iOS**: 16.0+
- **Xcode**: 15.0+
- **Swift**: 5.9+
- **macOS**: Ventura or later

## Dependencies

Managed via Swift Package Manager:

- Networking client (planned)
- Image caching (planned)
- Charts framework (built-in)

## Development Status

🚧 **Under Development**

Core functionality planned:
- ✅ Project structure setup
- ⏳ API client implementation
- ⏳ Authentication flow
- ⏳ Document scanner with VisionKit
- ⏳ Invoice list and detail views
- ⏳ Category management
- ⏳ Analytics with Swift Charts
- ⏳ Settings and preferences
- ⏳ Widget support

## App Store

Not yet published.

## License

Copyright © 2025 Invoice App
