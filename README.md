# File Storage System

<div align="center">

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](https://github.com/SabirHuseynov01/file-storage)
[![Java](https://img.shields.io/badge/Java-11%2B-orange.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![MinIO](https://img.shields.io/badge/Storage-MinIO-red.svg)](https://min.io)

A robust and scalable **File Storage System** built with Spring Boot, providing secure file upload, download, and management capabilities with MinIO integration and comprehensive error handling.

[Features](#-features) • [Getting Started](#-getting-started) • [Installation](#-installation) • [API Documentation](#-api-documentation) • [Error Handling](#-error-handling)

</div>

---

## 📋 Overview

The File Storage System is an enterprise-grade file management platform designed for secure, scalable file operations. Built with Spring Boot and integrated with MinIO for object storage, it provides a RESTful API for file uploads, downloads, deletions, and metadata management. The system uses API key-based authentication to ensure secure access to file operations and includes comprehensive exception handling for robust error management.

---

## ✨ Features

- 📤 **File Upload** - Secure file upload with metadata tracking
- 📥 **File Download** - Efficient file retrieval and streaming
- 🗑️ **File Deletion** - Secure file removal with database cleanup
- 📊 **File Metadata** - Comprehensive file information storage and retrieval
- 🔐 **API Key Authentication** - Secure access control with API keys
- 🪣 **MinIO Integration** - Object storage with MinIO compatibility
- 📝 **File Tracking** - Track uploaded files, uploader, and timestamps
- 🔄 **Storage Provider Abstraction** - Pluggable storage backend support
- ⚠️ **Comprehensive Error Handling** - Global exception handling with detailed error responses
- 🐳 **Containerized** - Docker and Docker Compose ready
- 📊 **Scalable Architecture** - Production-ready microservice design

---

## 🏗️ Architecture

### Core Components

#### 1. **File Service** (`FileService`)
- Manages file operations (upload, download, delete, info retrieval)
- Handles file metadata persistence
- Integrates with storage provider interface

#### 2. **File Controller** (`FileController`)
- RESTful API endpoints for file operations
- Handles multipart file uploads
- Manages file downloads with proper headers
- Provides file information retrieval

#### 3. **Storage Provider** (`StorageProvider`)
- Abstract interface for storage backends
- Supports pluggable storage implementations
- Currently implements MinIO provider

#### 4. **MinIO Storage Provider** (`MinioStorageProvider`)
- MinIO object storage integration
- Automatic bucket creation
- File upload, download, and deletion
- Error handling and logging

#### 5. **API Key Service** (`ApiKeyService`)
- API key generation and management
- Secure key hashing using SHA-256
- API key validation and activation

#### 6. **Security Configuration** (`SecurityConfig`)
- API key-based authentication filter
- Stateless session management
- CORS and CSRF configuration
- Secure request filtering

#### 7. **Global Exception Handler** (`GlobalExceptionHandler`)
- Centralized exception handling
- Consistent error response format
- Proper HTTP status code mapping
- Comprehensive logging

#### 8. **File Metadata** (`FileMetadata`)
- JPA entity for file information
- Tracks original filename, stored name, content type
- Stores file size, storage provider, and bucket information
- Records uploader and upload timestamp

---

## 🔄 Data Flow

```
User Request
    ↓
API Key Validation (SecurityConfig + ApiKeyFilter)
    ↓
FileController
    ↓
FileService (Business Logic)
    ↓
StorageProvider (MinIO)
    ↓
MinIO Storage / Database
    ↓
(Exception Handler catches any errors)
    ↓
Standardized Error Response
```

---

## ⚠️ Error Handling

The system includes comprehensive exception handling with a centralized `GlobalExceptionHandler` that manages all errors and returns consistent, structured error responses.

### Custom Exceptions

#### 1. **FileNotFoundException**
Thrown when a requested file is not found in the database or storage.

```java
public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String fileId) {
        super("File not found with id: " + fileId);
    }
}
```

**Usage:**
```java
FileMetadata metadata = fileRepository.findById(fileID)
    .orElseThrow(() -> new FileNotFoundException(fileID.toString()));
```

**Example Response:**
```json
{
  "timestamp": "2026-03-06T10:30:45.123",
  "status": 404,
  "error": "Not Found",
  "message": "File not found with id: 550e8400-e29b-41d4-a716-446655440000"
}
```

---

#### 2. **StorageException**
Thrown when there's an error with the storage provider (MinIO operations).

```java
public class StorageException extends RuntimeException {
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**Usage:**
```java
try {
    minioClient.putObject(...);
} catch (Exception e) {
    throw new StorageException("Failed to upload file to MinIO: " + e.getMessage(), e);
}
```

**Example Response:**
```json
{
  "timestamp": "2026-03-06T10:30:45.123",
  "status": 500,
  "error": "Storage Error",
  "message": "Failed to upload file to MinIO: Connection timeout"
}
```

---

### Exception Handler Mappings

The `GlobalExceptionHandler` maps exceptions to appropriate HTTP status codes:

| Exception | HTTP Status | Status Code | Description |
|-----------|------------|-------------|-------------|
| `FileNotFoundException` | NOT_FOUND | 404 | File not found in database or storage |
| `StorageException` | INTERNAL_SERVER_ERROR | 500 | MinIO or storage operation failed |
| `MaxUploadSizeExceededException` | PAYLOAD_TOO_LARGE | 413 | Uploaded file exceeds size limit |
| `IllegalArgumentException` | BAD_REQUEST | 400 | Invalid request parameters |
| `Exception` (General) | INTERNAL_SERVER_ERROR | 500 | Unexpected/unhandled errors |

---

### Error Response Format

All error responses follow a consistent JSON format:

```json
{
  "timestamp": "2026-03-06T10:30:45.123456",
  "status": 404,
  "error": "Error Type",
  "message": "Detailed error message"
}
```

**Fields:**
- `timestamp` - When the error occurred (ISO 8601 format)
- `status` - HTTP status code
- `error` - Short error type description
- `message` - Detailed error message

---

### Detailed Exception Handlers

#### 1. File Not Found Handler (404)
```java
@ExceptionHandler(FileNotFoundException.class)
public ResponseEntity<Map<String, Object>> handleFileNotFound(FileNotFoundException ex) {
    log.warn("File not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", 404,
            "error", "Not Found",
            "message", ex.getMessage()
    ));
}
```

**When it occurs:**
- Attempting to download a non-existent file
- Requesting metadata for a deleted file
- Deleting a file that's already removed

---

#### 2. Storage Exception Handler (500)
```java
@ExceptionHandler(StorageException.class)
public ResponseEntity<Map<String, Object>> handleStorageException(StorageException ex) {
    log.error("Storage error: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", 500,
            "error", "Storage Error",
            "message", ex.getMessage()
    ));
}
```

**When it occurs:**
- MinIO connection failure
- Bucket creation/access errors
- File upload/download stream errors
- Storage provider unavailability

---

#### 3. Max Upload Size Handler (413)
```java
@ExceptionHandler(MaxUploadSizeExceededException.class)
public ResponseEntity<Map<String, Object>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
    log.warn("File too large: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", 413,
            "error", "File Too Large",
            "message", "File size exceeds the maximum allowed limit"
    ));
}
```

**When it occurs:**
- Uploaded file size exceeds configured limit
- Configure `spring.servlet.multipart.max-file-size` to adjust limit

**Configuration Example:**
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```

---

#### 4. Bad Request Handler (400)
```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Bad request: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", 400,
            "error", "Bad Request",
            "message", ex.getMessage()
    ));
}
```

**When it occurs:**
- Invalid request parameters
- Missing required fields
- Invalid data format

---

#### 5. General Exception Handler (500)
```java
@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
    log.error("Unexpected error: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", 500,
            "error", "Internal Server Error",
            "message", "An unexpected error occurred"
    ));
}
```

**When it occurs:**
- Unexpected/unhandled exceptions
- Database connection errors
- Runtime errors

---

### Error Response Examples

#### File Not Found Example
```bash
curl -X GET "http://localhost:8080/api/files/invalid-id/download" \
  -H "X-Api-Key: your-api-key"
```

**Response:**
```json
{
  "timestamp": "2026-03-06T10:30:45.123456",
  "status": 404,
  "error": "Not Found",
  "message": "File not found with id: invalid-id"
}
```

---

#### File Too Large Example
```bash
curl -X POST "http://localhost:8080/api/files" \
  -H "X-Api-Key: your-api-key" \
  -F "file=@large-file.zip"
```

**Response:**
```json
{
  "timestamp": "2026-03-06T10:30:45.123456",
  "status": 413,
  "error": "File Too Large",
  "message": "File size exceeds the maximum allowed limit"
}
```

---

#### Storage Error Example
```bash
curl -X POST "http://localhost:8080/api/files" \
  -H "X-Api-Key: your-api-key" \
  -F "file=@document.pdf"
```

**Response (MinIO unreachable):**
```json
{
  "timestamp": "2026-03-06T10:30:45.123456",
  "status": 500,
  "error": "Storage Error",
  "message": "Failed to upload file to MinIO: Connection timeout"
}
```

---

### Logging

All exceptions are logged with appropriate levels:

- **WARN Level**: 
  - `FileNotFoundException`
  - `MaxUploadSizeExceededException`
  - `IllegalArgumentException`

- **ERROR Level**:
  - `StorageException`
  - Generic `Exception`

**Log Output Example:**
```
2026-03-06 10:30:45.123 WARN  [...FileStorageApplication] File not found: File not found with id: 550e8400...
2026-03-06 10:30:46.456 ERROR [...FileStorageApplication] Storage error: Failed to upload file to MinIO: Connection timeout
```

---

### Best Practices for Error Handling

1. **Always Check API Responses**
   - Check HTTP status code first
   - Parse error JSON for detailed messages

2. **Implement Retry Logic**
   - Retry on 500 errors (storage/server errors)
   - Implement exponential backoff

3. **Handle Specific Exceptions**
   - Handle 404 errors when downloading files
   - Handle 413 errors by splitting large files
   - Handle 500 errors by retrying

4. **Example Error Handling Code**
   ```java
   try {
       // File upload operation
       fileService.uploadFile(file, apiKey);
   } catch (FileNotFoundException e) {
       // Handle file not found (shouldn't happen in upload)
       log.error("File error: {}", e.getMessage());
   } catch (StorageException e) {
       // Handle storage errors - could retry
       log.error("Storage failed, retrying...", e);
       // Implement retry logic
   } catch (Exception e) {
       // Handle unexpected errors
       log.error("Unexpected error: {}", e.getMessage());
   }
   ```

---

## 🚀 Getting Started

### Prerequisites

- **Java 11 or higher**
- **Docker** (v20.10+) - for containerization
- **Docker Compose** (v1.29+) - for orchestration
- **Gradle 7.0+** - for building
- **MinIO** - object storage service

### Quick Start

1. **Clone the Repository**
   ```bash
   git clone https://github.com/SabirHuseynov01/file-storage.git
   cd file-storage
   ```

2. **Build the Application**
   ```bash
   ./gradlew clean build
   ```

3. **Start with Docker Compose**
   ```bash
   docker-compose up -d
   ```

4. **Access the Application**
   ```
   http://localhost:8080
   ```

5. **Create an API Key**
   ```bash
   curl -X POST "http://localhost:8080/api/keys?name=my-api-key"
   ```

---

## 📦 Installation

### Using Docker Compose (Recommended)

```bash
# Build and start services
docker-compose up -d

# View logs
docker-compose logs -f file-storage

# Stop services
docker-compose down
```

### Manual Build and Run

```bash
# Build the project
./gradlew clean build

# Run the application
java -jar build/libs/file-storage-*.jar
```

### Configuration Files

Create `application.yml` or `application.properties`:

```yaml
spring:
  application:
    name: file-storage
  datasource:
    url: jdbc:postgresql://localhost:5432/filestorage
    username: admin
    password: admin
  jpa:
    hibernate:
      ddl-auto: update
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

storage:
  provider: minio
  minio:
    endpoint: http://localhost:9000
    accessKey: minioadmin
    secretKey: minioadmin
    bucket: filestorage

server:
  port: 8080
```

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### API Key Management

#### Create API Key
```http
POST /api/keys?name=key-name
```

**Response:**
```json
{
  "apiKey": "generated-api-key",
  "name": "key-name"
}
```

---

### File Operations

#### Upload File
```http
POST /api/files
Content-Type: multipart/form-data
X-Api-Key: your-api-key

Body: 
- file: (binary file)
```

**Successful Response (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "originalName": "document.pdf",
  "storedName": "550e8400-e29b-41d4-a716-446655440000_document.pdf",
  "contentType": "application/pdf",
  "sizeBytes": 102400,
  "storageProvider": "minio",
  "bucketName": "filestorage",
  "uploadedAt": "2026-03-05T10:30:00"
}
```

**Error Response (413 - File Too Large):**
```json
{
  "timestamp": "2026-03-06T10:30:45.123",
  "status": 413,
  "error": "File Too Large",
  "message": "File size exceeds the maximum allowed limit"
}
```

---

#### Download File
```http
GET /api/files/{fileId}/download
X-Api-Key: your-api-key
```

**Successful Response (200):**
- Binary file data with appropriate Content-Type and Content-Disposition headers

**Error Response (404 - Not Found):**
```json
{
  "timestamp": "2026-03-06T10:30:45.123",
  "status": 404,
  "error": "Not Found",
  "message": "File not found with id: 550e8400-e29b-41d4-a716-446655440000"
}
```

**Example:**
```bash
curl -H "X-Api-Key: your-api-key" \
  http://localhost:8080/api/files/550e8400-e29b-41d4-a716-446655440000/download \
  -o downloaded-file.pdf
```

---

#### Get File Information
```http
GET /api/files/{fileId}
X-Api-Key: your-api-key
```

**Successful Response (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "originalName": "document.pdf",
  "storedName": "550e8400-e29b-41d4-a716-446655440000_document.pdf",
  "contentType": "application/pdf",
  "sizeBytes": 102400,
  "storageProvider": "minio",
  "bucketName": "filestorage",
  "uploadedBy": {
    "id": "api-key-id",
    "name": "key-name",
    "isActive": true
  },
  "uploadedAt": "2026-03-05T10:30:00"
}
```

**Error Response (404 - Not Found):**
```json
{
  "timestamp": "2026-03-06T10:30:45.123",
  "status": 404,
  "error": "Not Found",
  "message": "File not found with id: 550e8400-e29b-41d4-a716-446655440000"
}
```

---

#### Delete File
```http
DELETE /api/files/{fileId}
X-Api-Key: your-api-key
```

**Successful Response (204 - No Content):**
```
Empty body
```

**Error Response (404 - Not Found):**
```json
{
  "timestamp": "2026-03-06T10:30:45.123",
  "status": 404,
  "error": "Not Found",
  "message": "File not found with id: 550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 🔧 Configuration

### Environment Variables

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/filestorage
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=admin

# File Upload Configuration
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=100MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=100MB

# Storage Provider Configuration
STORAGE_PROVIDER=minio
STORAGE_MINIO_ENDPOINT=http://localhost:9000
STORAGE_MINIO_ACCESS_KEY=minioadmin
STORAGE_MINIO_SECRET_KEY=minioadmin
STORAGE_MINIO_BUCKET=filestorage

# Server Configuration
SERVER_PORT=8080
```

### Application Properties

**application.yml**
```yaml
spring:
  application:
    name: file-storage
  
  datasource:
    url: jdbc:postgresql://db:5432/filestorage
    username: ${DB_USER:admin}
    password: ${DB_PASSWORD:admin}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

  servlet:
    multipart:
      max-file-size: ${MAX_FILE_SIZE:100MB}
      max-request-size: ${MAX_REQUEST_SIZE:100MB}

storage:
  provider: ${STORAGE_PROVIDER:minio}
  minio:
    endpoint: ${STORAGE_MINIO_ENDPOINT:http://minio:9000}
    accessKey: ${STORAGE_MINIO_ACCESS_KEY:minioadmin}
    secretKey: ${STORAGE_MINIO_SECRET_KEY:minioadmin}
    bucket: ${STORAGE_MINIO_BUCKET:filestorage}

server:
  port: ${SERVER_PORT:8080}
```

---

## 🛠️ Technology Stack

| Component | Technology |
|-----------|------------|
| **Language** | Java 11+ (98.3%) |
| **Framework** | Spring Boot 3.x |
| **Build Tool** | Gradle 7.0+ |
| **Database** | PostgreSQL |
| **ORM** | JPA/Hibernate |
| **Object Storage** | MinIO |
| **Security** | Spring Security with API Key Authentication |
| **Exception Handling** | Global Exception Handler |
| **Containerization** | Docker, Docker Compose (1.7%) |
| **API** | RESTful API |
| **Logging** | SLF4J with Logback |

---

## 📁 Project Structure

```
file-storage/
├── src/
│   ├── main/
│   │   ├── java/com/example/filestorage/
│   │   │   ├── file/
│   │   │   │   ├── FileController.java          # REST API endpoints
│   │   │   │   ├── FileService.java             # Business logic
│   │   │   │   ├── FileRepository.java          # JPA repository
│   │   │   │   └── FileMetadata.java            # Entity model
│   │   │   ├── apikey/
│   │   │   │   ├── ApiKeyController.java        # API key endpoints
│   │   │   │   ├── ApiKeyService.java           # API key logic
│   │   │   │   ├── ApiKeyRepository.java        # API key repository
│   │   │   │   ├── ApiKey.java                  # Entity model
│   │   │   │   └── ApiKeyCreatedResponse.java   # Response DTO
│   │   │   ├── storage/
│   │   │   │   ├── StorageProvider.java         # Storage interface
│   │   │   │   └── MinioStorageProvider.java    # MinIO implementation
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java  # Exception handling
│   │   │   │   ├── FileNotFoundException.java    # Custom exception
│   │   │   │   └── StorageException.java        # Custom exception
│   │   │   ├── filter/
│   │   │   │   └── ApiKeyFilter.java            # Security filter
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java          # Security configuration
│   │   │   └── FilestorageApplication.java      # Main application
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── Dockerfile
├── docker-compose.yaml
├── build.gradle
├── settings.gradle
├── gradlew
└── README.md
```

---

## 🔐 Security

### API Key Authentication
- All file operations require valid API key in `X-Api-Key` header
- API keys are hashed using SHA-256 before storage
- API keys can be activated/deactivated for access control

### Security Features
- **Stateless Authentication** - No session management
- **CSRF Protection** - Disabled for stateless API
- **CORS Support** - Configurable cross-origin requests
- **Input Validation** - File validation before storage
- **Secure Headers** - Proper Content-Type and Content-Disposition headers
- **Comprehensive Exception Handling** - No stack traces exposed to clients

### Best Practices
- Store API keys securely in environment variables
- Rotate API keys regularly
- Use HTTPS in production
- Implement rate limiting for API endpoints
- Monitor file access and operations
- Check error responses for debugging (timestamps and messages)

---

## 🧪 Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Integration Tests
```bash
./gradlew integrationTest
```

### Run All Tests
```bash
./gradlew clean test
```

---

## 📊 Database Schema

### File Metadata Table
```sql
CREATE TABLE file_metadata (
    id UUID PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(100),
    size_bytes BIGINT,
    storage_provider VARCHAR(50),
    bucket_name VARCHAR(100),
    uploaded_by UUID NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    FOREIGN KEY (uploaded_by) REFERENCES api_key(id)
);
```

### API Key Table
```sql
CREATE TABLE api_key (
    id UUID PRIMARY KEY,
    hashed_key VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL
);
```

---

## 🚀 Performance & Optimization

### File Upload Performance
- Streaming file upload to MinIO
- Efficient memory usage for large files
- Metadata storage in PostgreSQL
- Optional file compression

### Download Optimization
- Direct streaming from MinIO
- Configurable chunk size
- Proper HTTP caching headers
- Support for Range requests

### Scalability
- Horizontal scaling with load balancing
- Distributed file storage with MinIO clusters
- Database connection pooling
- Asynchronous file operations (future enhancement)

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork the Repository**
   ```bash
   git clone https://github.com/SabirHuseynov01/file-storage.git
   ```

2. **Create a Feature Branch**
   ```bash
   git checkout -b feature/AmazingFeature
   ```

3. **Commit Your Changes**
   ```bash
   git commit -m 'feat: add amazing feature'
   ```

4. **Push to the Branch**
   ```bash
   git push origin feature/AmazingFeature
   ```

5. **Open a Pull Request**

---

## 📝 Commit Guidelines

- Use conventional commits: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`
- Keep commits small and focused
- Reference issues: `Fixes #123`
- Examples:
  - `feat(upload): add file compression support`
  - `fix(exception): improve error handling for storage failures`
  - `docs(readme): update error handling documentation`

---

## 🐛 Bug Reports

Found a bug? Please create an issue with:
- Clear description of the problem
- Steps to reproduce
- Expected and actual behavior
- Error response (including timestamp and message)
- Environment details (Java version, OS, etc.)
- Screenshots or logs (if applicable)

---

## 📞 Support & Contact

For support or inquiries:
- 📧 Email: [huseynovsabir904@gmail.com]
- 💬 GitHub Issues: [Create an Issue](https://github.com/SabirHuseynov01/file-storage/issues)
- 💡 Discussions: [Start a Discussion](https://github.com/SabirHuseynov01/file-storage/discussions)

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 🎯 Roadmap

- [ ] Implement file versioning system
- [ ] Add async file upload/download support
- [ ] Implement file encryption at rest
- [ ] Add file scanning for malware detection
- [ ] Support multiple storage backends (AWS S3, Azure Blob)
- [ ] Implement request rate limiting
- [ ] Add file preview generation
- [ ] Implement file compression
- [ ] Add audit logging
- [ ] Add API documentation with Swagger/OpenAPI
- [ ] Create admin dashboard
- [ ] Implement custom exception responses with error codes

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Exception Handling](https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc)
- [MinIO Documentation](https://min.io/docs/)
- [Spring Security](https://spring.io/projects/spring-security)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)

---

## 👏 Acknowledgments

- Thanks to all contributors who have helped with this project
- Special thanks to the Spring Boot and MinIO communities
- Inspired by industry best practices for file storage systems

---

**Last Updated**: March 6, 2026  
**Version**: 1.1.0

<div align="center">

Made with ❤️ by [SabirHuseynov01](https://github.com/SabirHuseynov01)

⭐ Don't forget to star this repository if you find it helpful!

</div>
