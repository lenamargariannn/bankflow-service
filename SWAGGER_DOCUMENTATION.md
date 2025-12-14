# Swagger/OpenAPI Documentation

## Overview
Swagger/OpenAPI documentation has been successfully added to all BankFlow API endpoints. The interactive API documentation is available through Swagger UI.

## Access Swagger UI

### Local Development
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/v1/api-docs

### Production (Cloud Run)
- **Swagger UI**: https://bankflow-service-574624095538.us-central1.run.app/api/swagger-ui.html
- **OpenAPI JSON**: https://bankflow-service-574624095538.us-central1.run.app/api/v1/api-docs

## API Endpoints Documented

### 1. Authentication Controller (`/v1/auth`)
Handles authentication and user registration.

#### Endpoints:
- **POST /v1/auth/login** - User login with username and password
  - Returns JWT token for authentication
  - No authentication required
  
- **POST /v1/auth/signup** - Register new user
  - Creates user account with username, email, password, and full name
  - No authentication required
  
- **POST /v1/auth/validate** - Validate JWT token
  - Checks if token is valid and not expired
  - No authentication required

### 2. Customer Controller (`/v1/customers`)
Manages customer information and accounts.
**Authentication Required**: Bearer JWT Token

#### Endpoints:
- **GET /v1/customers/{username}** - Get customer details by username
  
- **PUT /v1/customers/{username}** - Update customer information
  - Only provided fields will be updated
  
- **GET /v1/customers/{username}/accounts** - List all accounts for a customer
  
- **POST /v1/customers/{username}/accounts** - Create new account for customer
  - Supports optional initial deposit

### 3. Account Controller (`/v1/accounts`)
Handles account operations, transactions, and money transfers.
**Authentication Required**: Bearer JWT Token

#### Endpoints:
- **GET /v1/accounts/{accountNumber}** - Get account details
  
- **POST /v1/accounts/{accountNumber}/deposit** - Deposit money
  
- **POST /v1/accounts/{accountNumber}/withdraw** - Withdraw money
  
- **POST /v1/accounts/transfer** - Transfer money between accounts
  
- **GET /v1/accounts/{accountNumber}/transactions** - Get all transactions for an account
  
- **GET /v1/accounts/{accountNumber}/transactions/{transactionId}** - Get specific transaction

## Using Swagger UI

### Step 1: Start the Application
```bash
# Local development
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Or run the JAR
java -jar target/bankflow-service-1.0-SNAPSHOT.jar
```

### Step 2: Access Swagger UI
Open your browser and navigate to: http://localhost:8080/api/swagger-ui.html

### Step 3: Authenticate (for protected endpoints)

1. First, use the **POST /v1/auth/signup** endpoint to create a user account
2. Then use **POST /v1/auth/login** to get a JWT token
3. Copy the JWT token from the response
4. Click the **"Authorize"** button at the top right of Swagger UI
5. Enter: `Bearer YOUR_JWT_TOKEN` (or just paste the token)
6. Click **"Authorize"** then **"Close"**

### Step 4: Test Endpoints
- Click on any endpoint to expand it
- Click **"Try it out"**
- Fill in the required parameters
- Click **"Execute"**
- View the response below

## Authentication Flow Example

### 1. Register User
```
POST /api/v1/auth/signup
Body:
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe"
}
```

### 2. Login
```
POST /api/v1/auth/login
Body:
{
  "username": "john_doe",
  "password": "SecurePass123!"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400
}
```

### 3. Use Token for Protected Endpoints
Add header to all subsequent requests:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Features

✅ **Comprehensive Documentation**: All endpoints are documented with:
- Summary and description
- Request parameters and body schemas
- Response codes and schemas
- Authentication requirements

✅ **Interactive Testing**: Test all endpoints directly from the browser

✅ **Schema Validation**: View all DTOs and their validation rules

✅ **Authentication Integration**: JWT Bearer token authentication supported

✅ **Example Values**: All DTOs have pre-configured example values that auto-populate in Swagger UI

✅ **Response Examples**: See example responses for all endpoints

## Configuration

The Swagger configuration is defined in:
- **Config Class**: `src/main/java/com/bankflow/config/OpenApiConfig.java`
- **Application Config**: `src/main/resources/application.yml`

### Swagger Settings (application.yml):
```yaml
springdoc:
  api-docs:
    path: /v1/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
    disable-swagger-default-url: true
```

## Security Configuration

Swagger endpoints are publicly accessible (no authentication required):
- `/swagger-ui/**`
- `/swagger-ui.html`
- `/v1/api-docs/**`
- `/v1/api-docs`
- `/swagger-resources/**`
- `/webjars/**`
- `/api-docs/**`

This is configured in `SecurityConfig.java` to allow developers to view the API documentation without authentication. The Swagger UI can be accessed by anyone without needing to log in or provide a JWT token.

**Important**: All Swagger endpoints are configured with `.permitAll()` so they work unauthenticated.

## Additional Information

### Response Codes
- **200**: Success
- **201**: Created
- **400**: Bad Request (validation error)
- **401**: Unauthorized (missing or invalid token)
- **403**: Forbidden
- **404**: Not Found

### Tags
All endpoints are organized by tags:
- **Authentication**: Auth-related endpoints
- **Customers**: Customer management
- **Accounts**: Account operations and transactions

## Troubleshooting

### Cannot access Swagger UI
- Ensure the application is running
- Check the port (default: 8080)
- Verify the context path is `/api`
- URL should be: http://localhost:8080/api/swagger-ui.html

### 401 Unauthorized on protected endpoints
- Make sure you've obtained a JWT token via login
- Click "Authorize" and enter your token
- Token format: `Bearer YOUR_TOKEN` or just `YOUR_TOKEN`

### 404 on endpoints
- Remember the context path is `/api`
- Full path example: http://localhost:8080/api/v1/customers/john_doe

