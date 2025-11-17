# Getting Started with Invoice System Development

This guide will help you set up your development environment and start working on the Invoice System project.

## Prerequisites

### Required Software

#### Backend Development
- **Java 17+** - [Download](https://adoptium.net/)
- **Maven 3.8+** or **Gradle 8+** - Build tools
- **Docker & Docker Compose** - For local services
- **PostgreSQL 15+** - Database (via Docker recommended)
- **Redis 7+** - Cache and queue (via Docker recommended)
- **Git** - Version control
- **IDE**: IntelliJ IDEA (recommended) or Eclipse

#### Android Development
- **Android Studio** - Latest stable version
- **Android SDK** - API level 26+ (Android 8.0+)
- **JDK 17**
- **Android Emulator** or physical device

#### iOS Development
- **macOS** - Required for iOS development
- **Xcode 15+** - Latest stable version
- **CocoaPods** or **Swift Package Manager**
- **iOS Simulator** or physical device

### Optional Tools
- **Postman** or **Insomnia** - API testing
- **DBeaver** or **pgAdmin** - Database management
- **Redis Commander** - Redis management GUI

---

## Project Setup

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/invoice-service.git
cd invoice-service
```

### 2. Backend Setup

#### Environment Configuration

Create `.env` file in the project root:

```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=invoice_db
DB_USERNAME=invoice_user
DB_PASSWORD=your_secure_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# JWT
JWT_SECRET=your_jwt_secret_key_min_256_bits
JWT_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# Cloud Storage (AWS S3 example)
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_REGION=us-east-1
S3_BUCKET_NAME=invoice-attachments

# LLM API Keys
OPENAI_API_KEY=your_openai_key
ANTHROPIC_API_KEY=your_anthropic_key
GOOGLE_GEMINI_API_KEY=your_gemini_key

# OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
APPLE_CLIENT_ID=your_apple_client_id
APPLE_CLIENT_SECRET=your_apple_client_secret

# Application
SERVER_PORT=8080
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8081
```

#### Start Local Services with Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: invoice_postgres
    environment:
      POSTGRES_DB: invoice_db
      POSTGRES_USER: invoice_user
      POSTGRES_PASSWORD: your_secure_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    container_name: invoice_redis
    command: redis-server --requirepass your_redis_password
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  mailhog:
    image: mailhog/mailhog
    container_name: invoice_mailhog
    ports:
      - "1025:1025"  # SMTP
      - "8025:8025"  # Web UI

volumes:
  postgres_data:
  redis_data:
```

Start services:

```bash
docker-compose up -d
```

#### Build and Run Backend

**Using Maven:**
```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

**Using Gradle:**
```bash
cd backend
./gradlew clean build
./gradlew bootRun
```

The API will be available at `http://localhost:8080`

#### Verify Backend

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

---

### 3. Android App Setup

#### Open Project
1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to `android/` folder
4. Wait for Gradle sync to complete

#### Configure API Endpoint

Update `local.properties`:

```properties
api.base.url=http://10.0.2.2:8080/api
# 10.0.2.2 is the host machine IP from Android Emulator
# Use your actual IP for physical devices
```

Or update `gradle.properties` for different build variants:

```properties
dev.api.url=http://10.0.2.2:8080/api
staging.api.url=https://staging-api.invoice.com/api
prod.api.url=https://api.invoice.com/api
```

#### Build and Run

1. Select emulator or connected device
2. Click Run (Shift+F10) or Debug (Shift+F9)
3. App will install and launch

---

### 4. iOS App Setup

#### Install Dependencies

**Using CocoaPods:**
```bash
cd ios
pod install
```

**Using Swift Package Manager:**
Dependencies are managed in Xcode project settings.

#### Open Project
```bash
open InvoiceApp.xcworkspace
# or if using SPM:
open InvoiceApp.xcodeproj
```

#### Configure API Endpoint

Update `Config.swift`:

```swift
enum Config {
    static let apiBaseURL: String = {
        #if DEBUG
        return "http://localhost:8080/api"
        #elseif STAGING
        return "https://staging-api.invoice.com/api"
        #else
        return "https://api.invoice.com/api"
        #endif
    }()
}
```

#### Build and Run

1. Select simulator or connected device
2. Click Run (⌘+R) or Test (⌘+U)
3. App will build, install, and launch

---

## Development Workflow

### Backend Development

#### Project Structure
```
backend/
├── src/main/java/com/invoiceapp/
│   ├── controller/     # REST API endpoints
│   ├── service/        # Business logic
│   ├── repository/     # Data access
│   ├── model/          # Domain models
│   ├── config/         # Configuration
│   └── security/       # Security components
├── src/main/resources/
│   ├── application.yml # Configuration
│   └── db/migration/   # Database migrations
└── src/test/           # Unit and integration tests
```

#### Running Tests
```bash
./mvnw test                    # All tests
./mvnw test -Dtest=InvoiceServiceTest  # Specific test
./mvnw verify                  # Integration tests
```

#### Database Migrations

Migrations are managed by Flyway and run automatically on startup.

Create new migration:
```bash
# Create file: src/main/resources/db/migration/V5__add_tags_table.sql
CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### API Documentation

Swagger UI available at: `http://localhost:8080/swagger-ui.html`

Generate API docs:
```bash
./mvnw springdoc-openapi:generate
```

### Mobile Development

#### Android Architecture

```
app/
├── data/          # Data layer (API, DB, Repository)
├── domain/        # Business logic (Use Cases, Models)
├── presentation/  # UI layer (Compose, ViewModels)
└── di/            # Dependency Injection
```

#### Running Android Tests
```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest

# Specific test
./gradlew test --tests InvoiceRepositoryTest
```

#### iOS Architecture

```
InvoiceApp/
├── Data/          # Data layer (API, Core Data, Repository)
├── Domain/        # Business logic (Use Cases, Models)
├── Presentation/  # UI layer (SwiftUI, ViewModels)
└── DI/            # Dependency Injection
```

#### Running iOS Tests
```bash
# Command line
xcodebuild test -scheme InvoiceApp -destination 'platform=iOS Simulator,name=iPhone 15'

# Or use Xcode (⌘+U)
```

---

## Common Tasks

### Adding a New API Endpoint

1. **Create DTO** (Data Transfer Object):
```java
// CreateInvoiceRequest.java
@Data
public class CreateInvoiceRequest {
    @NotNull
    private String vendorName;

    @NotNull
    private LocalDate date;

    @NotNull
    @Positive
    private BigDecimal amount;

    private String currency = "USD";
}
```

2. **Update Controller**:
```java
@PostMapping
public ResponseEntity<InvoiceDto> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
    InvoiceDto invoice = invoiceService.createInvoice(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
}
```

3. **Implement Service**:
```java
@Transactional
public InvoiceDto createInvoice(CreateInvoiceRequest request) {
    Invoice invoice = new Invoice();
    invoice.setVendorName(request.getVendorName());
    invoice.setDate(request.getDate());
    invoice.setAmount(request.getAmount());
    // ... set other fields

    invoice = invoiceRepository.save(invoice);
    return mapToDto(invoice);
}
```

4. **Write Tests**:
```java
@Test
void testCreateInvoice() {
    CreateInvoiceRequest request = new CreateInvoiceRequest();
    request.setVendorName("Test Vendor");
    request.setDate(LocalDate.now());
    request.setAmount(new BigDecimal("100.00"));

    InvoiceDto result = invoiceService.createInvoice(request);

    assertNotNull(result.getId());
    assertEquals("Test Vendor", result.getVendorName());
}
```

### Adding a New Mobile Screen (Android)

1. **Create ViewModel**:
```kotlin
@HiltViewModel
class InvoiceDetailViewModel @Inject constructor(
    private val repository: InvoiceRepository
) : ViewModel() {

    private val _invoice = MutableStateFlow<Invoice?>(null)
    val invoice: StateFlow<Invoice?> = _invoice.asStateFlow()

    fun loadInvoice(id: String) {
        viewModelScope.launch {
            _invoice.value = repository.getInvoice(id)
        }
    }
}
```

2. **Create Composable Screen**:
```kotlin
@Composable
fun InvoiceDetailScreen(
    invoiceId: String,
    viewModel: InvoiceDetailViewModel = hiltViewModel()
) {
    val invoice by viewModel.invoice.collectAsState()

    LaunchedEffect(invoiceId) {
        viewModel.loadInvoice(invoiceId)
    }

    invoice?.let { inv ->
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = inv.vendorName, style = MaterialTheme.typography.headlineMedium)
            Text(text = inv.date.toString())
            Text(text = "$${inv.amount}")
        }
    }
}
```

3. **Add Navigation**:
```kotlin
NavHost(navController = navController, startDestination = "invoices") {
    composable("invoices") { InvoiceListScreen(navController) }
    composable("invoice/{id}") { backStackEntry ->
        InvoiceDetailScreen(backStackEntry.arguments?.getString("id")!!)
    }
}
```

---

## Debugging

### Backend Debugging

**IntelliJ IDEA:**
1. Set breakpoints in code
2. Run → Debug 'Application'
3. Or attach to running process: Run → Attach to Process

**Remote Debugging:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### Android Debugging

1. Set breakpoints in Kotlin code
2. Run → Debug 'app'
3. Use Logcat for logs: `Log.d("TAG", "message")`
4. Android Studio Profiler for performance

### iOS Debugging

1. Set breakpoints in Swift code
2. Product → Run (⌘+R) with debugger attached
3. Use `print()` or `os_log()` for logging
4. Xcode Instruments for performance profiling

---

## Troubleshooting

### Backend Issues

**Database Connection Failed:**
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check connection
psql -h localhost -U invoice_user -d invoice_db
```

**Port Already in Use:**
```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>
```

### Android Issues

**Gradle Sync Failed:**
1. File → Invalidate Caches and Restart
2. Delete `.gradle` folder and sync again
3. Check `gradle.properties` and `local.properties`

**Cannot Connect to API:**
- Use `10.0.2.2` instead of `localhost` for emulator
- Check firewall settings
- Ensure backend is running

### iOS Issues

**Build Failed:**
1. Clean build folder: Product → Clean Build Folder (⇧⌘K)
2. Delete derived data: `rm -rf ~/Library/Developer/Xcode/DerivedData`
3. Update CocoaPods: `pod update`

**Cannot Connect to API:**
- Add `NSAppTransportSecurity` exception in Info.plist for HTTP
- Check network permissions
- Ensure backend is running and accessible

---

## Next Steps

1. **Review Design Documents**: Read `DESIGN.md` and `ARCHITECTURE.md`
2. **Pick a Task**: Choose from the epics in `DESIGN.md`
3. **Create Feature Branch**: `git checkout -b feature/your-feature-name`
4. **Develop and Test**: Follow TDD when possible
5. **Submit PR**: Push to origin and create pull request
6. **Code Review**: Address feedback and iterate

---

## Resources

### Documentation
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Android Developer Guide](https://developer.android.com/guide)
- [iOS Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/)

### APIs
- [OpenAI API](https://platform.openai.com/docs)
- [Anthropic Claude API](https://docs.anthropic.com/)
- [Google Gemini API](https://ai.google.dev/docs)

### Tools
- [Postman Collections](./postman/)
- [Database Schema](./docs/database-schema.md)
- [API Examples](./docs/api-examples.md)

---

## Getting Help

- **Issues**: Create an issue on GitHub
- **Discussions**: Use GitHub Discussions for questions
- **Slack**: Join #invoice-dev channel
- **Email**: dev-team@invoice.com

Happy coding! 🚀
