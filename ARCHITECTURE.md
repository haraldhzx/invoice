# Invoice System Architecture

## System Overview

The Invoice System is a cloud-native, distributed application designed to provide intelligent invoice scanning, classification, and analysis capabilities across mobile and web platforms.

## Architecture Principles

1. **Microservices-Ready**: While starting as a monolith, the architecture supports future decomposition into microservices
2. **API-First**: All functionality exposed via well-documented REST APIs
3. **Cloud-Native**: Designed for cloud deployment with scalability in mind
4. **Mobile-First**: Optimized user experience for mobile devices
5. **AI-Powered**: Leveraging multimodal LLMs for intelligent data extraction
6. **Secure by Design**: Security and privacy at every layer

---

## High-Level Architecture

```
                                    ┌─────────────────────────────┐
                                    │     Load Balancer/CDN       │
                                    │      (CloudFlare/AWS)       │
                                    └──────────────┬──────────────┘
                                                   │
                    ┌──────────────────────────────┼──────────────────────────────┐
                    │                              │                              │
          ┌─────────▼─────────┐         ┌─────────▼─────────┐         ┌─────────▼─────────┐
          │   Mobile Apps     │         │   Web Portal      │         │   Admin Portal    │
          │  (iOS/Android)    │         │    (React)        │         │     (React)       │
          └─────────┬─────────┘         └─────────┬─────────┘         └─────────┬─────────┘
                    │                              │                              │
                    └──────────────────────────────┼──────────────────────────────┘
                                                   │
                                        ┌──────────▼──────────┐
                                        │    API Gateway      │
                                        │   (Spring Cloud)    │
                                        │   Authentication    │
                                        │   Rate Limiting     │
                                        └──────────┬──────────┘
                                                   │
        ┌──────────────────────────────────────────┼──────────────────────────────────────────┐
        │                                          │                                          │
        │                         Spring Boot Application                                     │
        │                                                                                      │
        │  ┌─────────────┐  ┌──────────────┐  ┌─────────────┐  ┌──────────────┐            │
        │  │   Invoice   │  │  Category    │  │   User      │  │  Analytics   │            │
        │  │   Service   │  │  Service     │  │  Service    │  │   Service    │            │
        │  └──────┬──────┘  └──────┬───────┘  └──────┬──────┘  └──────┬───────┘            │
        │         │                │                 │                │                      │
        │  ┌──────▼──────────────────────────────────▼────────────────▼───────┐            │
        │  │                    Data Access Layer (JPA/Hibernate)              │            │
        │  └──────────────────────────────────┬──────────────────────────────┘            │
        └─────────────────────────────────────┼──────────────────────────────────────────┘
                                               │
                    ┌──────────────────────────┼──────────────────────────┐
                    │                          │                          │
          ┌─────────▼──────────┐    ┌─────────▼──────────┐    ┌─────────▼──────────┐
          │    PostgreSQL      │    │      Redis         │    │   Object Storage   │
          │     Database       │    │   (Cache/Queue)    │    │   (S3/GCS/Azure)   │
          │                    │    │                    │    │                    │
          │  - Users           │    │  - Sessions        │    │  - Invoice Images  │
          │  - Invoices        │    │  - Job Queue       │    │  - PDFs            │
          │  - Categories      │    │  - Rate Limits     │    │  - Attachments     │
          │  - Transactions    │    │  - Cache           │    │                    │
          └────────────────────┘    └────────────────────┘    └────────────────────┘


                    ┌────────────────────────────────────────────────────┐
                    │           External Services                        │
                    │                                                    │
                    │  ┌────────────┐  ┌────────────┐  ┌────────────┐  │
                    │  │  LLM APIs  │  │   OCR      │  │    MCP     │  │
                    │  │            │  │  Service   │  │  Protocol  │  │
                    │  │ - OpenAI   │  │            │  │            │  │
                    │  │ - Anthropic│  │ - Tesseract│  │            │  │
                    │  │ - Google   │  │ - Cloud    │  │            │  │
                    │  │   Gemini   │  │   Vision   │  │            │  │
                    │  └────────────┘  └────────────┘  └────────────┘  │
                    └────────────────────────────────────────────────────┘
```

---

## Backend Architecture

### Spring Boot Application Structure

```
invoice-service/
├── src/main/java/com/invoiceapp/
│   ├── InvoiceApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── JwtConfig.java
│   │   ├── RedisConfig.java
│   │   ├── StorageConfig.java
│   │   ├── LlmConfig.java
│   │   └── WebConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── InvoiceController.java
│   │   ├── CategoryController.java
│   │   ├── TransactionController.java
│   │   ├── AnalyticsController.java
│   │   ├── McpController.java
│   │   └── UserController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── InvoiceService.java
│   │   ├── CategoryService.java
│   │   ├── TransactionService.java
│   │   ├── AnalyticsService.java
│   │   ├── McpQueryService.java
│   │   ├── LlmService.java
│   │   ├── OcrService.java
│   │   ├── StorageService.java
│   │   ├── EmailService.java
│   │   └── NotificationService.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── InvoiceRepository.java
│   │   ├── CategoryRepository.java
│   │   ├── TransactionRepository.java
│   │   └── ImportBatchRepository.java
│   ├── model/
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── Invoice.java
│   │   │   ├── Category.java
│   │   │   ├── Transaction.java
│   │   │   ├── LineItem.java
│   │   │   ├── Attachment.java
│   │   │   └── ImportBatch.java
│   │   ├── dto/
│   │   │   ├── InvoiceDto.java
│   │   │   ├── CategoryDto.java
│   │   │   ├── TransactionDto.java
│   │   │   └── AnalyticsDto.java
│   │   └── enums/
│   │       ├── InvoiceStatus.java
│   │       ├── CategoryType.java
│   │       └── TransactionType.java
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── UserDetailsServiceImpl.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── UnauthorizedException.java
│   │   └── ValidationException.java
│   └── util/
│       ├── DateUtils.java
│       ├── CurrencyUtils.java
│       └── ValidationUtils.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/
│       ├── V1__create_users_table.sql
│       ├── V2__create_invoices_table.sql
│       ├── V3__create_categories_table.sql
│       └── V4__create_transactions_table.sql
└── src/test/
    ├── java/com/invoiceapp/
    │   ├── controller/
    │   ├── service/
    │   └── repository/
    └── resources/
```

### Key Components

#### 1. Authentication & Authorization
- **JWT Tokens**: Access tokens (15 min expiry), refresh tokens (7 days)
- **OAuth2**: Google, Apple Sign-In integration
- **Role-Based Access**: USER, ADMIN, PREMIUM roles
- **Security Headers**: CORS, CSP, HSTS

#### 2. Invoice Processing Pipeline
```
Upload → Validation → OCR → LLM Analysis → Classification → Storage → Notification
```

1. **Upload**: Client uploads image/PDF
2. **Validation**: File type, size, virus scan
3. **OCR**: Extract text using Tesseract/Cloud Vision
4. **LLM Analysis**: Send to multimodal LLM for data extraction
5. **Classification**: Categorize based on content
6. **Storage**: Save to database and cloud storage
7. **Notification**: Notify user of completion

#### 3. Data Storage Strategy

**PostgreSQL Tables**:
- `users`: User accounts and authentication
- `invoices`: Invoice metadata and extracted data
- `categories`: Expense categories (system + custom)
- `transactions`: Bank transactions and supermarket bills
- `line_items`: Individual invoice line items
- `attachments`: File references
- `import_batches`: Bulk import tracking
- `audit_logs`: Security and compliance audit trail

**Redis Cache**:
- Session storage
- Rate limiting counters
- Recently accessed invoices
- LLM response caching
- Job queue for async processing

**Object Storage**:
- Original invoice images/PDFs
- Generated thumbnails
- Export files (CSV, PDF reports)
- User profile images

#### 4. LLM Integration

**Provider Abstraction**:
```java
public interface LlmProvider {
    InvoiceAnalysisResult analyzeInvoice(byte[] image, String extractedText);
    String processQuery(String query, Map<String, Object> context);
}

public class OpenAiLlmProvider implements LlmProvider { ... }
public class AnthropicLlmProvider implements LlmProvider { ... }
public class GoogleGeminiProvider implements LlmProvider { ... }
```

**Prompt Template**:
```
You are an invoice analysis assistant. Analyze the following invoice and extract:
1. Vendor name
2. Invoice date
3. Total amount and currency
4. Tax amount
5. Line items (description, quantity, price)
6. Suggested expense category

Image: [base64_image]
OCR Text: [extracted_text]

Return the data as JSON with the following structure:
{
  "vendorName": "",
  "date": "",
  "totalAmount": 0,
  "currency": "",
  ...
}
```

#### 5. MCP Query Interface

**Architecture**:
```
User Query → Query Parser → Intent Recognition → Query Generator → Database → Response Formatter → User
```

**Example Queries**:
- "How much did I spend on sweets?" → `SELECT SUM(amount) FROM invoices WHERE category IN ('snacks', 'candy', 'desserts')`
- "House basic costs?" → Aggregate utilities, rent, maintenance categories
- "Spent for kids?" → Filter by 'children' tag or specific categories

---

## Mobile Architecture

### Android (Kotlin + Jetpack Compose)

```
app/
├── data/
│   ├── remote/
│   │   ├── api/
│   │   │   └── InvoiceApiService.kt
│   │   ├── dto/
│   │   └── interceptor/
│   ├── local/
│   │   ├── database/
│   │   │   ├── AppDatabase.kt
│   │   │   ├── dao/
│   │   │   └── entity/
│   │   └── preferences/
│   └── repository/
│       ├── InvoiceRepository.kt
│       ├── CategoryRepository.kt
│       └── AuthRepository.kt
├── domain/
│   ├── model/
│   ├── usecase/
│   └── repository/
├── presentation/
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   └── LoginViewModel.kt
│   ├── invoice/
│   │   ├── list/
│   │   ├── detail/
│   │   ├── scan/
│   │   └── manual/
│   ├── analytics/
│   └── settings/
├── di/
│   ├── AppModule.kt
│   ├── NetworkModule.kt
│   └── DatabaseModule.kt
└── util/
    ├── Constants.kt
    └── Extensions.kt
```

**Key Technologies**:
- **UI**: Jetpack Compose, Material Design 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Networking**: Retrofit + OkHttp
- **Database**: Room
- **Image Loading**: Coil
- **Camera**: CameraX + MLKit
- **Async**: Coroutines + Flow

### iOS (Swift + SwiftUI)

```
InvoiceApp/
├── Data/
│   ├── Network/
│   │   ├── APIService.swift
│   │   ├── Endpoints.swift
│   │   └── NetworkManager.swift
│   ├── Local/
│   │   ├── CoreData/
│   │   │   ├── InvoiceApp.xcdatamodeld
│   │   │   └── CoreDataManager.swift
│   │   └── UserDefaults/
│   └── Repositories/
│       ├── InvoiceRepository.swift
│       ├── CategoryRepository.swift
│       └── AuthRepository.swift
├── Domain/
│   ├── Models/
│   ├── UseCases/
│   └── RepositoryProtocols/
├── Presentation/
│   ├── Auth/
│   │   ├── LoginView.swift
│   │   └── LoginViewModel.swift
│   ├── Invoice/
│   │   ├── List/
│   │   ├── Detail/
│   │   ├── Scanner/
│   │   └── ManualEntry/
│   ├── Analytics/
│   └── Settings/
├── DI/
│   └── DependencyContainer.swift
└── Utilities/
    ├── Extensions/
    └── Constants.swift
```

**Key Technologies**:
- **UI**: SwiftUI, SF Symbols
- **Architecture**: MVVM + Clean Architecture
- **Networking**: URLSession / Alamofire
- **Database**: Core Data
- **Image Loading**: SDWebImage / Kingfisher
- **Camera**: AVFoundation + VisionKit
- **Async**: async/await, Combine

---

## Security Architecture

### Authentication Flow

```
1. User Login
   ├─→ Email/Password → Validate → Generate JWT (access + refresh)
   ├─→ Google OAuth → Validate → Create/Link Account → Generate JWT
   └─→ Apple Sign-In → Validate → Create/Link Account → Generate JWT

2. API Request
   ├─→ Client sends access token in Authorization header
   ├─→ Server validates token (signature, expiry, claims)
   ├─→ Extract user from token
   └─→ Process request with user context

3. Token Refresh
   ├─→ Access token expires
   ├─→ Client sends refresh token
   ├─→ Server validates refresh token
   ├─→ Generate new access token (and optionally new refresh token)
   └─→ Return new tokens
```

### Data Security

**Encryption**:
- **At Rest**: PostgreSQL with encryption enabled, AES-256
- **In Transit**: TLS 1.3, certificate pinning on mobile
- **Application Level**: Sensitive fields encrypted (e.g., bank account numbers)

**Access Control**:
- Row-level security in PostgreSQL
- User can only access their own invoices/transactions
- Admin role for system management
- Audit logging for sensitive operations

### API Security

- **Rate Limiting**: 100 requests/minute per user
- **CORS**: Whitelist specific origins
- **Input Validation**: Strong validation on all inputs
- **SQL Injection**: Parameterized queries (JPA)
- **XSS Prevention**: Output encoding, CSP headers
- **CSRF Protection**: CSRF tokens for state-changing operations

---

## Scalability & Performance

### Horizontal Scaling

**Application Servers**:
- Stateless Spring Boot instances
- Load balancer distributes traffic
- Auto-scaling based on CPU/memory

**Database**:
- Read replicas for analytics queries
- Connection pooling (HikariCP)
- Query optimization and indexing

**Cache**:
- Redis cluster for high availability
- Cache invalidation strategies
- Distributed caching

### Performance Optimization

**Backend**:
- Lazy loading for large collections
- Database query optimization (indexes, explain plans)
- Async processing for LLM calls (message queue)
- Response compression (gzip)
- API response caching

**Mobile**:
- Image compression before upload
- Pagination for list views
- Local caching (Room/Core Data)
- Lazy image loading
- Background sync for offline support

**LLM Cost Optimization**:
- Cache common invoice patterns
- Batch processing when possible
- Use smaller models for simple extractions
- Rate limiting on LLM calls
- User tier limits (free vs premium)

---

## Monitoring & Observability

### Metrics

**Application Metrics**:
- Request rate, latency, error rate
- Database query performance
- Cache hit/miss ratio
- LLM API call count and cost
- Background job processing time

**Business Metrics**:
- User registrations
- Invoices processed per day
- Query accuracy rate
- User retention
- Feature usage

### Logging

**Log Levels**:
- ERROR: System errors requiring attention
- WARN: Potential issues
- INFO: Important business events
- DEBUG: Detailed diagnostic info

**Structured Logging**:
```json
{
  "timestamp": "2025-11-17T10:30:00Z",
  "level": "INFO",
  "service": "invoice-service",
  "traceId": "abc123",
  "userId": "user-456",
  "action": "invoice_processed",
  "invoiceId": "inv-789",
  "duration": 2500,
  "confidence": 0.95
}
```

### Alerting

- API error rate > 5%
- Database connection pool exhausted
- LLM API failures
- High response latency (p95 > 1s)
- Low disk space
- SSL certificate expiry

---

## Disaster Recovery

### Backup Strategy

**Database**:
- Daily full backups
- Point-in-time recovery (WAL archiving)
- Retention: 30 days
- Test restores monthly

**Object Storage**:
- Cross-region replication
- Versioning enabled
- Lifecycle policies (archive old files)

### Business Continuity

- Multi-region deployment (active-passive)
- Failover procedures documented
- RTO: 4 hours
- RPO: 24 hours

---

## Technology Decisions

### Why Spring Boot?
- Mature ecosystem for Java/Kotlin
- Excellent database integration (Spring Data JPA)
- Strong security features (Spring Security)
- Easy cloud deployment
- Large community and support

### Why PostgreSQL?
- ACID compliance
- JSON/JSONB support for flexible schemas
- Full-text search capabilities
- Mature and reliable
- Strong support for transactions

### Why Redis?
- Fast in-memory caching
- Pub/sub for real-time features
- Job queue (with Redis Queue)
- Session storage
- Rate limiting

### Why Kotlin (Android)?
- Modern, concise language
- Null safety
- Coroutines for async
- Official Google support
- Interop with Java

### Why Swift (iOS)?
- Native iOS development
- Modern language features
- Strong type safety
- Excellent performance
- Required for App Store

---

## Deployment Architecture

### Production Environment

```
                    ┌─────────────────┐
                    │   CloudFlare    │
                    │   (CDN + WAF)   │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │  Load Balancer  │
                    │   (AWS ALB)     │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
   ┌────▼────┐         ┌────▼────┐         ┌────▼────┐
   │  App    │         │  App    │         │  App    │
   │ Server 1│         │ Server 2│         │ Server 3│
   │  (ECS)  │         │  (ECS)  │         │  (ECS)  │
   └────┬────┘         └────┬────┘         └────┬────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
   ┌────▼────┐         ┌───▼────┐         ┌───▼────┐
   │   RDS   │         │ Redis  │         │   S3   │
   │  (PG)   │         │ Cluster│         │ Bucket │
   └─────────┘         └────────┘         └────────┘
```

**Infrastructure as Code**: Terraform / CloudFormation
**Container Orchestration**: ECS / Kubernetes
**Secrets Management**: AWS Secrets Manager / HashiCorp Vault

---

## Conclusion

This architecture provides a solid foundation for building a scalable, secure, and intelligent invoice management system. The modular design allows for future enhancements and the adoption of microservices architecture as the system grows.
