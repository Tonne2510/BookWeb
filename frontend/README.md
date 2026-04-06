# BookWeb Frontend

Spring Boot frontend for book selling application with Thymeleaf templates.

## Build & Run

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

The application will run at `http://localhost:8080`

## Features

- Book listing with pagination
- Book search and filtering
- Book details page
- User authentication (register, login, forgot password)
- User profile management
- Review management
- OAuth2 integration (Google, GitHub)
- Responsive design

## Configuration

Update `application.properties` with:
- `graphql.endpoint`: GraphQL API endpoint (default: http://localhost:4000/graphql)
- `server.port`: Frontend port (default: 8080)

## Technologies

- Java 17
- Spring Boot 3.1.5
- Thymeleaf
- Bootstrap 5
- JWT for authentication
- HttpClient for API calls
- Lombok for boilerplate reduction
