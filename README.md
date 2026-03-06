# File Storage System

<div align="center">

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](https://github.com/SabirHuseynov01/file-storage)
[![Java](https://img.shields.io/badge/Java-11%2B-orange.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![MinIO](https://img.shields.io/badge/Storage-MinIO-red.svg)](https://min.io)

A robust and scalable **File Storage System** built with Spring Boot, providing secure file upload, download, and management capabilities with MinIO integration.

[Features](#-features) • [Getting Started](#-getting-started) • [Installation](#-installation) • [API Documentation](#-api-documentation) • [Configuration](#-configuration)

</div>

---

## 📋 Overview

The File Storage System is an enterprise-grade file management platform designed for secure, scalable file operations. Built with Spring Boot and integrated with MinIO for object storage, it provides a RESTful API for file uploads, downloads, deletions, and metadata management. The system uses API key-based authentication to ensure secure access to file operations.

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

#### 7. **File Metadata** (`FileMetadata`)
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

**Response:**
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

---

#### Download File
```http
GET /api/files/{fileId}/download
X-Api-Key: your-api-key
```

**Response:**
- Binary file data with appropriate Content-Type and Content-Disposition headers

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

**Response:**
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

---

#### Delete File
```http
DELETE /api/files/{fileId}
X-Api-Key: your-api-key
```

**Response:**
```
204 No Content
```

---

## 🔧 Configuration

### Environment Variables

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/filestorage
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=admin

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
| **Language** | Java 11+ |
| **Framework** | Spring Boot 3.x |
| **Build Tool** | Gradle 7.0+ |
| **Database** | PostgreSQL |
| **ORM** | JPA/Hibernate |
| **Object Storage** | MinIO |
| **Security** | Spring Security with API Key Authentication |
| **Containerization** | Docker, Docker Compose |
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

### Best Practices
- Store API keys securely in environment variables
- Rotate API keys regularly
- Use HTTPS in production
- Implement rate limiting for API endpoints
- Monitor file access and operations

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
  - `fix(security): validate api key format`
  - `docs(readme): update configuration section`

---

## 🐛 Bug Reports

Found a bug? Please create an issue with:
- Clear description of the problem
- Steps to reproduce
- Expected and actual behavior
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
- [ ] Create admin dashboard

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
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

**Last Updated**: March 5, 2026  
**Version**: 1.0.0

<div align="center">

Made with ❤️ by [SabirHuseynov01](https://github.com/SabirHuseynov01)

⭐ Don't forget to star this repository if you find it helpful!

</div>
