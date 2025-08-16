# Booking Platform - Microservices Architecture

A comprehensive booking platform built with Spring Boot microservices architecture, featuring API Gateway, service discovery, and Keycloak authentication.

## Architecture

### Services
- **API Gateway** (Port 8080) - Spring Cloud Gateway with OAuth2 security
- **User Service** (Port 8081) - User management and authentication
- **Booking Service** (Port 8082) - Booking management and processing
- **Business Service** (Port 8083) - Business entity management
- **Payment Service** (Port 8084) - Payment processing

### Infrastructure
- **Eureka Server** (Port 8761) - Service discovery
- **Keycloak** (Port 8090) - Authentication and authorization
- **PostgreSQL** - Separate databases for each service

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose

### Build and Run

1. **Build all services:**
```bash
mvn clean install
```

2. **Start infrastructure with Docker Compose:**
```bash
docker-compose up -d
```

### Keycloak Setup

1. Access Keycloak admin console: http://localhost:8090
2. Login with admin/admin
3. Create realm named 'booking-platform'
4. Configure clients and users as needed

### Service Endpoints

All services are accessible through the API Gateway:
- User Service: http://localhost:8080/api/users
- Booking Service: http://localhost:8080/api/bookings  
- Business Service: http://localhost:8080/api/businesses
- Payment Service: http://localhost:8080/api/payments

### Database Ports
- User DB: localhost:5432
- Booking DB: localhost:5433
- Business DB: localhost:5434
- Payment DB: localhost:5435

## Development

Each service is a Maven module with its own configuration and can be developed independently. The common module contains shared utilities and DTOs used across services.