# Invoice Service Backend

Spring Boot backend service for the Invoice Scanning and Classification system.

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **PostgreSQL 15** - Primary database
- **Redis 7** - Caching and session management
- **Flyway** - Database migrations
- **JWT** - Authentication
- **Spring Security** - Authorization
- **Docker** - Containerization

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose
- PostgreSQL 15+ (or use Docker)
- Redis 7+ (or use Docker)

## Quick Start

### 1. Start Local Services

Start PostgreSQL, Redis, and other services:

```bash
cd ..
docker-compose up -d
```

This will start:
- PostgreSQL on port 5432
- Redis on port 6379
- MailHog on ports 1025 (SMTP) and 8025 (Web UI)
- MinIO on ports 9000 (API) and 9001 (Console)

### 2. Configure Environment

Copy the example environment file and update it:

```bash
cp ../.env.example ../.env
```

Edit `.env` and set the required values (especially JWT_SECRET, API keys, etc.)

### 3. Build and Run

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

## API Documentation

Once the application is running, access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

API docs are also available at:

```
http://localhost:8080/api-docs
```

## Database Migrations

Database migrations are managed by Flyway and run automatically on startup.

### View Migration Status

```bash
./mvnw flyway:info
```

### Rollback (Manual)

Flyway doesn't support automatic rollbacks. To rollback:

1. Create a new migration with the rollback SQL
2. Run the application to apply the rollback

### Create New Migration

Create a new file in `src/main/resources/db/migration/`:

```
V5__add_new_feature.sql
```

Naming convention: `V{version}__{description}.sql`

## Testing

### Run All Tests

```bash
./mvnw test
```

### Run Specific Test

```bash
./mvnw test -Dtest=InvoiceServiceTest
```

### Integration Tests

```bash
./mvnw verify
```

Integration tests use Testcontainers to spin up PostgreSQL and Redis containers.

## Project Structure

```
backend/
├── src/main/java/com/invoiceapp/
│   ├── InvoiceApplication.java         # Main application class
│   ├── config/                         # Configuration classes
│   │   ├── SecurityConfig.java
│   │   ├── JwtProperties.java
│   │   ├── WebConfig.java
│   │   └── RedisConfig.java
│   ├── controller/                     # REST API endpoints
│   ├── service/                        # Business logic
│   ├── repository/                     # Data access layer
│   ├── model/                          # Domain models
│   │   ├── entity/                     # JPA entities
│   │   ├── dto/                        # Data transfer objects
│   │   └── enums/                      # Enumerations
│   ├── security/                       # Security components
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── UserDetailsServiceImpl.java
│   ├── exception/                      # Exception handling
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── UnauthorizedException.java
│   │   └── ValidationException.java
│   └── util/                           # Utility classes
├── src/main/resources/
│   ├── application.yml                 # Main configuration
│   ├── application-dev.yml             # Dev environment config
│   ├── application-prod.yml            # Production config
│   └── db/migration/                   # Database migrations
│       ├── V1__create_users_table.sql
│       ├── V2__create_categories_table.sql
│       ├── V3__create_invoices_table.sql
│       └── V4__create_transactions_table.sql
└── src/test/                           # Test files
```

## Environment Variables

Key environment variables (see `.env.example` for full list):

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | localhost |
| `DB_PORT` | PostgreSQL port | 5432 |
| `DB_NAME` | Database name | invoice_db |
| `DB_USERNAME` | Database user | invoice_user |
| `DB_PASSWORD` | Database password | - |
| `REDIS_HOST` | Redis host | localhost |
| `REDIS_PORT` | Redis port | 6379 |
| `JWT_SECRET` | JWT signing secret | - |
| `OPENAI_API_KEY` | OpenAI API key | - |
| `AWS_ACCESS_KEY_ID` | AWS access key | - |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key | - |
| `S3_BUCKET_NAME` | S3 bucket name | - |

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh` - Refresh access token
- `POST /api/auth/logout` - Logout

### Invoices

- `GET /api/invoices` - List invoices (paginated)
- `POST /api/invoices` - Create invoice manually
- `GET /api/invoices/{id}` - Get invoice details
- `PUT /api/invoices/{id}` - Update invoice
- `DELETE /api/invoices/{id}` - Delete invoice
- `POST /api/invoices/upload` - Upload invoice image/PDF

### Categories

- `GET /api/categories` - List categories
- `POST /api/categories` - Create custom category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category

### Transactions

- `GET /api/transactions` - List transactions
- `POST /api/transactions/import` - Import bank transactions
- `GET /api/transactions/{id}` - Get transaction details
- `PUT /api/transactions/{id}` - Update transaction
- `DELETE /api/transactions/{id}` - Delete transaction

### Analytics

- `GET /api/analytics/spending` - Spending analytics
- `GET /api/analytics/categories` - Category breakdown
- `GET /api/analytics/trends` - Spending trends

## Security

### JWT Authentication

All API endpoints (except `/api/auth/**`) require JWT authentication.

Include the JWT token in the Authorization header:

```
Authorization: Bearer <access_token>
```

### Password Hashing

Passwords are hashed using BCrypt with a cost factor of 10.

### CORS

CORS is configured to allow requests from:
- http://localhost:3000 (React dev server)
- http://localhost:8081 (Android/iOS emulator)

Update `ALLOWED_ORIGINS` in `.env` for additional origins.

## Monitoring

### Health Check

```
GET http://localhost:8080/actuator/health
```

### Metrics

Prometheus metrics available at:

```
GET http://localhost:8080/actuator/prometheus
```

### Logging

Logs are written to:
- Console (stdout)
- `logs/spring.log` (file)

Log levels can be configured in `application.yml`

## Development

### Hot Reload

The application supports hot reload with Spring Boot DevTools:

```bash
./mvnw spring-boot:run
```

Changes to Java files will automatically restart the application.

### Database Console

Access H2 console (dev mode only):

```
http://localhost:8080/h2-console
```

### Mail Testing

MailHog captures all emails sent by the application:

```
http://localhost:8025
```

### Object Storage (MinIO)

MinIO console for file management:

```
http://localhost:9001
Username: minioadmin
Password: minioadmin123
```

## Troubleshooting

### Database Connection Failed

Check if PostgreSQL is running:

```bash
docker ps | grep postgres
```

Test connection:

```bash
psql -h localhost -U invoice_user -d invoice_db
```

### Redis Connection Failed

Check if Redis is running:

```bash
docker ps | grep redis
```

Test connection:

```bash
redis-cli -h localhost -p 6379 ping
```

### Port Already in Use

Find and kill the process using port 8080:

```bash
lsof -i :8080
kill -9 <PID>
```

### Migration Failed

Reset the database (development only):

```bash
docker-compose down -v
docker-compose up -d
./mvnw spring-boot:run
```

## Production Deployment

### Build JAR

```bash
./mvnw clean package -DskipTests
```

JAR file will be in `target/invoice-service-0.1.0-SNAPSHOT.jar`

### Run JAR

```bash
java -jar target/invoice-service-0.1.0-SNAPSHOT.jar
```

### Docker Build

```bash
docker build -t invoice-service:latest .
docker run -p 8080:8080 invoice-service:latest
```

### Environment-Specific Configuration

Set the active profile:

```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar invoice-service.jar
```

Or via command line:

```bash
java -jar invoice-service.jar --spring.profiles.active=prod
```

## Contributing

1. Create a feature branch
2. Make changes
3. Write tests
4. Run tests: `./mvnw test`
5. Submit pull request

## License

Copyright © 2025 Invoice App
