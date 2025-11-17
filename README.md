# Invoice Service - AI-Powered Expense Tracking

A comprehensive invoice scanning and classification system with cloud backend, mobile apps, and natural language query interface. Upload invoice photos, get automatic data extraction with AI, and ask questions like "How much did I spend on sweets?"

[![Status](https://img.shields.io/badge/status-active-success.svg)]()
[![License](https://img.shields.io/badge/license-MIT-blue.svg)]()

## 🎯 Overview

Invoice Service is a full-stack application that combines:
- **AI-Powered OCR & Analysis**: Upload invoice photos → Automatic data extraction with GPT-4 Vision
- **Natural Language Queries**: Ask "How much did I spend on groceries?" and get instant answers
- **Multi-Platform**: Spring Boot backend + Android (Kotlin) + iOS (Swift)
- **Smart Import**: Upload bank CSV files → Auto-create categorized transactions
- **Comprehensive Analytics**: Track spending by category, time period, and vendor

## ✨ Key Features

### Backend (Spring Boot)
- ✅ **Authentication**: JWT with refresh tokens, OAuth2 ready (Google, Apple)
- ✅ **Invoice Management**: CRUD, pagination, filtering, status tracking
- ✅ **AI Analysis**: GPT-4 Vision extracts vendor, date, amounts, line items
- ✅ **OCR Integration**: Tesseract for text extraction from images/PDFs
- ✅ **File Storage**: S3/MinIO with presigned URLs
- ✅ **Transaction Import**: CSV parser with flexible column mapping
- ✅ **MCP Query Interface**: Natural language spending queries
- ✅ **Categories**: 17+ default categories + custom user categories
- ✅ **Enhanced Analytics**: Spending trends, vendor analysis, forecasting, recurring expense detection
- ✅ **Budget Tracking**: Multi-period budgets with alerts and real-time tracking
- ✅ **Security Hardening**: Rate limiting, input sanitization, OWASP protection, security audit logging
- ✅ **Comprehensive Testing**: 65+ unit and integration tests with Testcontainers
- ✅ **Production Deployment**: Docker, CI/CD pipeline, monitoring with Prometheus/Grafana

### Android App (✅ Completed)
- ✅ **Authentication**: Login/Register with JWT tokens, secure token storage
- ✅ **Camera Scanner**: CameraX integration with image capture and upload
- ✅ **Invoice Management**: List, upload, detail views with status tracking
- ✅ **Analytics Dashboard**: Spending by category with time period filters
- ✅ **Category Management**: View, create, delete custom categories
- ✅ **MCP Query Interface**: Natural language spending queries with chat UI
- ✅ **Material Design 3**: Modern UI with light/dark mode support
- ✅ **MVVM Architecture**: Clean architecture with Hilt dependency injection
- ✅ **Offline Ready**: DataStore for secure preferences, Room database ready

### iOS App (✅ Completed)
- ✅ **Authentication**: Login/Register with JWT tokens, secure token storage
- ✅ **Document Scanner**: VisionKit integration with camera scanning
- ✅ **Invoice Management**: List, detail, edit views with pagination
- ✅ **Invoice Upload**: Camera scanning and manual entry
- ✅ **Analytics Dashboard**: Swift Charts with trends, budgets, forecasts
- ✅ **Category Management**: Create, edit, delete custom categories with icons and colors
- ✅ **MCP Query Interface**: Natural language AI queries with chat UI
- ✅ **Budget Tracking**: Progress indicators and spending alerts
- ✅ **SwiftUI**: Modern declarative UI with async/await networking
- ✅ **MVVM Architecture**: Clean architecture with dependency injection
- ✅ **Dark Mode**: Full dark mode support
- ✅ **Animations**: Smooth transitions and spring animations
- ✅ **Accessibility**: VoiceOver support with descriptive labels

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- PostgreSQL 15+ (via Docker)
- Redis 7+ (via Docker)

### Start Local Environment

```bash
# Clone repository
git clone https://github.com/your-org/invoice-service.git
cd invoice-service

# Start services (PostgreSQL, Redis, MinIO, MailHog)
docker-compose up -d

# Configure environment
cp .env.example .env
# Edit .env with your API keys

# Build and run backend
cd backend
./mvnw spring-boot:run
```

Backend API: `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui.html`
MailHog UI: `http://localhost:8025`
MinIO Console: `http://localhost:9001`

## 📱 Mobile Apps

### Android

```bash
cd android
./gradlew assembleDebug
# Open in Android Studio and run
```

### iOS

```bash
cd ios
open InvoiceApp.xcodeproj
# Build and run in Xcode (⌘+R)
```

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Mobile Apps (iOS/Android)                │
│          Camera • Upload • Analytics • Queries              │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTPS/REST
        ┌─────────────▼────────────────┐
        │     Spring Boot Backend      │
        │   JWT Auth • REST APIs       │
        └─────────────┬────────────────┘
                      │
        ┌─────────────┼────────────────┐
        │             │                │
   ┌────▼────┐   ┌───▼────┐     ┌────▼────┐
   │PostgreSQL│   │  Redis │     │ S3/MinIO│
   │ Database │   │  Cache │     │ Storage │
   └──────────┘   └────────┘     └─────────┘
                      │
        ┌─────────────┼────────────────┐
        │             │                │
   ┌────▼─────┐  ┌───▼──────┐   ┌────▼────┐
   │Tesseract │  │  GPT-4   │   │   MCP   │
   │   OCR    │  │  Vision  │   │ Queries │
   └──────────┘  └──────────┘   └─────────┘
```

## 📊 API Endpoints (57+ Total)

### Authentication (4 endpoints)
```
POST   /api/auth/register       # Register new user
POST   /api/auth/login          # Login with JWT
POST   /api/auth/refresh        # Refresh access token
POST   /api/auth/logout         # Logout and revoke tokens
```

### Invoices (11 endpoints)
```
GET    /api/invoices                           # List invoices (paginated)
GET    /api/invoices/status/{status}           # Filter by status
GET    /api/invoices/category/{categoryId}     # Filter by category
GET    /api/invoices/{id}                      # Get invoice details
POST   /api/invoices                           # Create manually
POST   /api/invoices/upload                    # Upload & AI analyze
PUT    /api/invoices/{id}                      # Update invoice
DELETE /api/invoices/{id}                      # Delete invoice
GET    /api/invoices/analytics/total-spending  # Total spending
GET    /api/invoices/analytics/spending-by-category/{id} # Category spending
```

### Categories (7 endpoints)
```
GET    /api/categories                    # List all categories
GET    /api/categories/type/{type}        # Filter by INCOME/EXPENSE
GET    /api/categories/{id}               # Get category
GET    /api/categories/{id}/subcategories # Get subcategories
POST   /api/categories                    # Create custom category
PUT    /api/categories/{id}               # Update category
DELETE /api/categories/{id}               # Delete category
```

### Transactions (7 endpoints)
```
GET    /api/transactions              # List transactions
GET    /api/transactions/{id}         # Get transaction
POST   /api/transactions              # Create transaction
PUT    /api/transactions/{id}         # Update transaction
DELETE /api/transactions/{id}         # Delete transaction
POST   /api/transactions/import/csv   # Import bank CSV
GET    /api/transactions/unreconciled # Get unreconciled
```

### Enhanced Analytics (6 endpoints)
```
GET    /api/analytics/trends/monthly       # Monthly spending trends with % changes
GET    /api/analytics/trends/weekly        # Weekly spending trends
GET    /api/analytics/vendors/top          # Top vendors by spending
GET    /api/analytics/recurring-expenses   # Detect recurring payments
GET    /api/analytics/compare              # Compare two time periods
GET    /api/analytics/forecast             # Spending forecast
```

### Budget Management (6 endpoints)
```
GET    /api/budgets              # List all budgets with spending
GET    /api/budgets/{id}         # Get budget details
POST   /api/budgets              # Create new budget
PUT    /api/budgets/{id}         # Update budget
DELETE /api/budgets/{id}         # Delete budget
GET    /api/budgets/exceeded     # Get exceeded budgets
GET    /api/budgets/nearing-limit # Get budgets near limit
```

### MCP Queries (2 endpoints)
```
POST   /api/mcp/query     # Ask natural language question
GET    /api/mcp/examples  # Get example queries
```

## 🤖 Natural Language Queries (MCP)

Ask questions in plain English:

```bash
curl -X POST http://localhost:8080/api/mcp/query \
  -H "Authorization: Bearer YOUR_JWT" \
  -H "Content-Type: application/json" \
  -d '{"query": "How much did I spend on sweets?"}'
```

**Supported Queries:**
- "How much did I spend on sweets?"
- "How much are house basic costs?"
- "How much was spent for the kids?"
- "What's my total spending this month?"
- "Show me a breakdown of all categories"
- "How much did I spend on Food & Dining last month?"

**Response:**
```json
{
  "query": "How much did I spend on sweets?",
  "answer": "You spent $45.32 on Sweets & Desserts in the last 30 days.",
  "intent": "SPENDING_BY_CATEGORY",
  "data": {
    "category": "Sweets & Desserts",
    "amount": 45.32,
    "startDate": "2025-10-18",
    "endDate": "2025-11-17"
  },
  "suggestions": [
    "Show me a breakdown of all categories",
    "What are my top expenses?"
  ]
}
```

## 🧠 AI-Powered Invoice Processing

### Upload Flow

1. **Upload**: POST multipart image/PDF to `/api/invoices/upload`
2. **Storage**: File saved to S3/MinIO with UUID name
3. **OCR**: Tesseract extracts text from image
4. **AI Analysis**: GPT-4 Vision analyzes image + text
5. **Extraction**: Returns structured JSON with invoice data
6. **Mapping**: Data saved to database with category suggestion
7. **Status**: COMPLETED (confidence ≥ 70%) or REVIEW_REQUIRED (< 70%)

### Example Analysis Result

```json
{
  "vendorName": "Starbucks",
  "invoiceNumber": "12345",
  "date": "2025-11-17",
  "totalAmount": 12.50,
  "currency": "USD",
  "taxAmount": 1.13,
  "suggestedCategory": "Food & Dining",
  "confidence": 0.95,
  "lineItems": [
    {
      "description": "Grande Latte",
      "quantity": 1,
      "unitPrice": 5.25,
      "totalPrice": 5.25
    }
  ]
}
```

## 📦 Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL 15 + Flyway migrations
- **Cache**: Redis 7
- **Storage**: AWS S3 / MinIO
- **Authentication**: JWT + OAuth2
- **AI**: OpenAI GPT-4 Vision
- **OCR**: Tesseract
- **API Docs**: Swagger/OpenAPI

### Mobile
- **Android**: Kotlin 1.9 + Jetpack Compose + Hilt
- **iOS**: Swift 5.9 + SwiftUI + Combine
- **Camera**: MLKit (Android) / VisionKit (iOS)

### DevOps
- **Containers**: Docker Compose
- **Monitoring**: Prometheus + Grafana (optional)
- **CI/CD**: GitHub Actions (ready)

## 🗄️ Database Schema

### Core Tables
- `users` - User accounts and authentication
- `categories` - Expense categories (17+ default)
- `invoices` - Invoice metadata and extracted data
- `line_items` - Individual invoice items
- `attachments` - File references (S3 keys)
- `transactions` - Bank transactions
- `import_batches` - Bulk import tracking
- `tags` - Custom user tags
- `budgets` - Budget tracking
- `refresh_tokens` - JWT refresh tokens
- `audit_logs` - Compliance logging

## 🔒 Security

- **Authentication**: JWT access tokens (15min) + refresh tokens (7 days)
- **Password**: BCrypt hashing
- **API**: Rate limiting, CORS, input validation
- **Data**: User isolation, row-level security
- **Files**: Presigned S3 URLs (1-hour expiry)
- **Audit**: All operations logged

## 📈 Default Categories

**Expense Categories (17):**
Food & Dining • Transportation • Shopping • Entertainment • Housing • Utilities • Healthcare • Education • Personal Care • Travel • Insurance • Financial • Kids • Pets • Gifts & Donations • Subscriptions • Other

**Income Categories (6):**
Salary • Freelance • Investment • Rental • Business • Other Income

## 🧪 Testing

```bash
# Backend tests
cd backend
./mvnw test                    # Unit tests
./mvnw verify                  # Integration tests

# Android tests
cd android
./gradlew test                 # Unit tests
./gradlew connectedAndroidTest # UI tests

# iOS tests
cd ios
xcodebuild test -scheme InvoiceApp
```

## 📚 Documentation

- [Backend README](backend/README.md) - Setup, API docs, troubleshooting
- [Android README](android/README.md) - Android setup and architecture
- [iOS README](ios/README.md) - iOS setup and architecture
- [Design Document](DESIGN.md) - Complete system design with 12 epics
- [Architecture](ARCHITECTURE.md) - Technical architecture details
- [Getting Started](GETTING_STARTED.md) - Developer onboarding guide

## ✅ Development Status

### 🎉 All Epics Completed (1-12)
- [x] **Epic 1**: Project setup and infrastructure
- [x] **Epic 2**: Core backend API (auth, invoices, categories, transactions)
- [x] **Epic 3**: AI invoice analysis with GPT-4 Vision + Tesseract OCR
- [x] **Epic 4**: Transaction import (CSV with flexible column mapping)
- [x] **Epic 5**: MCP query interface (natural language spending queries)
- [x] **Epic 6**: Android mobile app (fully functional with all features)
- [x] **Epic 7**: iOS mobile app (complete SwiftUI implementation)
- [x] **Epic 8**: Enhanced analytics (trends, vendor analysis, forecasting, budgets)
- [x] **Epic 9**: Testing & QA (65+ unit and integration tests)
- [x] **Epic 10**: Deployment & DevOps (Docker, CI/CD, monitoring)
- [x] **Epic 11**: Security hardening (rate limiting, OWASP protection, audit logging)
- [x] **Epic 12**: UX enhancements (animations, accessibility for Android & iOS)

### 📈 Project Statistics
- **Backend**: 65+ tests, 57+ API endpoints, 6 service layers
- **Android**: 27 Kotlin files, Material Design 3, MVVM + Hilt DI
- **iOS**: 36 Swift files, SwiftUI, MVVM with async/await
- **Total Lines of Code**: ~15,000+ across all platforms
- **Test Coverage**: Unit tests, integration tests with Testcontainers
- **Security**: OWASP Top 10 protection, rate limiting, audit logging
- **Performance**: Shimmer loading, skeleton screens, optimized animations
- **Accessibility**: Full screen reader support on both platforms

### 🎯 Future Features
- [ ] OFX/QFX bank import format
- [ ] Budget alerts and push notifications
- [ ] Export to Excel/PDF reports
- [ ] Multi-currency support
- [ ] Bank API integration (Plaid, Open Banking)
- [ ] Subscription tracking
- [ ] Tax preparation assistance
- [ ] Receipt email parsing
- [ ] Expense approval workflows
- [ ] Multi-user/team support

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open Pull Request

## 📄 License

Copyright © 2025 Invoice App. All rights reserved.

## 🙋 Support

- **Issues**: GitHub Issues
- **Email**: support@invoice.com

---

**Built with ❤️ using Spring Boot, Kotlin, Swift, and GPT-4**
