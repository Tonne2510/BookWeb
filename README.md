# BookWeb - Complete Book Store Application

A full-stack book selling web application with complete authentication, CRUD operations, GraphQL API, and responsive frontend.

## 📁 Project Structure

```
BookWeb/
├── backend/                 # Node.js + Express + GraphQL
│   ├── db/                 # Database configuration
│   ├── models/             # Sequelize models (7 tables)
│   ├── schema/             # GraphQL type definitions and resolvers
│   ├── config/             # Passport OAuth configuration
│   ├── middleware/         # Authentication middleware
│   ├── utils/              # Helper utilities
│   └── server.js           # Main server file
│
└── frontend/               # Spring Boot + Thymeleaf
    ├── src/main/java/
    │   └── com/bookweb/
    │       ├── controller/  # Request handlers
    │       ├── service/     # Business logic
    │       ├── model/       # DTOs
    │       ├── config/      # Configuration
    │       └── util/        # Utilities
    ├── src/main/resources/
    │   ├── templates/       # Thymeleaf HTML templates
    │   ├── static/          # CSS, JS, images
    │   └── application.properties
    └── pom.xml             # Maven configuration
```

## 🎯 Features

### Authentication & Authorization
- ✅ User registration and login
- ✅ JWT token-based authentication
- ✅ Google OAuth2 integration
- ✅ GitHub OAuth2 integration
- ✅ Forgot password functionality
- ✅ Password reset via token
- ✅ Role-based access control (admin, moderator, user)
- ✅ Account status management (active, inactive, blocked)

### CRUD Operations (6+ Tables)
1. **User** - User management with roles and status
2. **Category** - Book categories with status toggle
3. **Author** - Author information management
4. **Book** - Complete book inventory with pricing and discounts
5. **Review** - User reviews with ratings
6. **Order** - Order management
7. **OrderItem** - Order line items

### Additional Features
- ✅ Search functionality (by title, description, ISBN)
- ✅ Advanced filtering by category, author, price range
- ✅ Sorting (by date, title, price, rating)
- ✅ Pagination for large datasets
- ✅ Book ratings and view counter
- ✅ Discount management
- ✅ Stock management
- ✅ Order status tracking
- ✅ Review status moderation (approved, pending, rejected)
- ✅ User/Product status toggles
- ✅ Isolated CRUD operations (no table interdependencies)

### GraphQL API
- Full GraphQL implementation
- Query types for data retrieval
- Mutation types for data manipulation
- Proper error handling and validation
- Token-based authentication via HTTP headers

## 🛠️ Technology Stack

### Backend
- **Runtime**: Node.js
- **Framework**: Express.js
- **API**: Apollo GraphQL Server
- **Database**: MySQL
- **ORM**: Sequelize
- **Authentication**: JWT + Passport.js
- **Password Hashing**: Bcryptjs

### Frontend
- **Framework**: Spring Boot 3.1.5
- **View Engine**: Apache Thymeleaf
- **Build Tool**: Maven
- **Language**: Java 17
- **HTTP Client**: Apache HttpClient5
- **UI Framework**: Bootstrap 5

## 🚀 Getting Started

### Backend Setup

1. **Install dependencies**
   ```bash
   cd backend
   npm install
   ```

2. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env with your database credentials and OAuth keys
   ```

3. **Create database**
   ```sql
   CREATE DATABASE bookweb;
   ```

4. **Start server**
   ```bash
   npm run dev
   ```
   Server runs at `http://localhost:4000/graphql`

### Frontend Setup

1. **Navigate to frontend**
   ```bash
   cd frontend
   ```

2. **Build with Maven**
   ```bash
   mvn clean install
   ```

3. **Run application**
   ```bash
   mvn spring-boot:run
   ```
   Frontend runs at `http://localhost:8080`

## 📊 Database Schema

### Users Table
```
- id (UUID, PK)
- email (String, Unique)
- password (String, hashed)
- firstName, lastName
- phone, address
- avatar
- role (enum: user, admin, moderator)
- status (enum: active, inactive, blocked)
- googleId, githubId (for OAuth)
- resetToken, resetTokenExpire
- lastLogin
- timestamps
```

### Books Table
```
- id (UUID, PK)
- title, slug
- description
- isbn (Unique)
- price, discount
- coverImage
- publisher, pages
- publicationDate
- stock, rating, views
- status (enum: active, inactive, archived)
- categoryId (FK), authorId (FK)
- timestamps
```

### Categories Table
```
- id (UUID, PK)
- name (Unique), slug
- description, image
- status (enum: active, inactive)
- timestamps
```

### Authors Table
```
- id (UUID, PK)
- name (Unique), slug
- bio, imageUrl
- dateOfBirth, nationality
- status (enum: active, inactive)
- timestamps
```

### Reviews Table
```
- id (UUID, PK)
- rating (1-5)
- title, content
- helpfulCount
- status (enum: approved, pending, rejected)
- userId (FK), bookId (FK)
- timestamps
```

### Orders Table
```
- id (UUID, PK)
- orderNumber (Unique)
- totalPrice, totalDiscount
- shippingAddress, shippingCost
- paymentMethod (enum: credit_card, bank_transfer, cash_on_delivery)
- status (enum: pending, processing, shipped, delivered, cancelled)
- notes
- userId (FK)
- timestamps
```

### OrderItems Table
```
- id (UUID, PK)
- quantity, price, discount
- bookId (FK), orderId (FK)
```

## 📝 API Examples

### Authentication
```graphql
mutation {
  register(
    email: "user@example.com"
    password: "password123"
    firstName: "John"
    lastName: "Doe"
  ) {
    token
    user { id email firstName lastName }
  }
}

mutation {
  login(email: "user@example.com", password: "password123") {
    token
    user { id email role status }
  }
}
```

### Books
```graphql
query {
  books(
    page: 1
    limit: 12
    searchTerm: "GraphQL"
    sortBy: "createdAt"
    order: "DESC"
  ) {
    books {
      id
      title
      price
      finalPrice
      coverImage
      rating
      status
    }
    total
    pages
  }
}

mutation {
  createBook(
    title: "New Book"
    slug: "new-book"
    isbn: "978-1234567890"
    price: 29.99
    discount: 10
    stock: 50
  ) {
    id
    title
    status
  }
}
```

### Status Management
```graphql
mutation {
  toggleBookStatus(id: "book-id") {
    id
    status
  }
}

mutation {
  toggleUserStatus(userId: "user-id") {
    id
    status
  }
}
```

## 🔐 OAuth Configuration

### Google OAuth2
1. Go to Google Cloud Console
2. Create OAuth 2.0 credentials
3. Add callback URL: `http://localhost:4000/auth/google/callback`
4. Add credentials to `.env`

### GitHub OAuth2
1. Go to GitHub Settings > Developer settings > OAuth Apps
2. Create new OAuth App
3. Add callback URL: `http://localhost:4000/auth/github/callback`
4. Add credentials to `.env`

## 📱 Frontend Pages

- **Home Page** (`/`) - Featured books
- **Book Listing** (`/books`) - All books with filters and pagination
- **Book Detail** (`/books/:slug`) - Detailed book information and reviews
- **Search** (`/books/search?q=query`) - Search results
- **Login** (`/auth/login`) - User login
- **Register** (`/auth/register`) - User registration
- **Profile** (`/auth/profile`) - User profile information
- **Forgot Password** (`/auth/forgot-password`) - Password recovery
- **Reset Password** (`/auth/reset-password?token=...`) - Reset password
- **About** (`/about`) - About the store
- **Contact** (`/contact`) - Contact form

## ✅ Validation & Error Handling

- Email validation and uniqueness checks
- Password strength requirements
- ISBN uniqueness validation
- Stock availability checks
- Price and discount validation
- Token expiration handling
- CORS configuration
- GraphQL error responses

## 📦 Dependencies

### Backend
- express, apollo/server, graphql
- sequelize, mysql2
- jsonwebtoken, bcryptjs
- passport, passport-google-oauth20, passport-github2
- dotenv, cors, nodemailer

### Frontend
- spring-boot-starter-web, spring-boot-starter-thymeleaf
- spring-boot-starter-security
- jjwt (JWT)
- httpclient5 (HTTP calls)
- gson (JSON)
- lombok, bootstrap

## 🔄 Development Workflow

1. **Backend Development**
   ```bash
   cd backend
   npm run dev  # Starts with auto-reload
   ```

2. **Frontend Development**
   ```bash
   cd frontend
   mvn spring-boot:run
   ```

3. **Testing GraphQL**
   - Visit `http://localhost:4000/graphql`
   - Use Apollo Studio for queries/mutations

## 📚 Additional Notes

- All passwords are bcrypt-hashed with 10 rounds
- JWT tokens expire in 7 days by default
- Reset tokens expire in 24 hours
- Reviews require approval before being displayed
- Stock decreases on order creation and increases on order cancellation
- CORS is configured to accept requests from frontend

## 🎯 Future Enhancements

- Payment gateway integration
- Email notifications
- Advanced reporting
- Inventory management dashboard
- Wishlist functionality
- Book recommendations
- Reading lists
- Author interviews/blogs
- Mobile app

---

**Happy Coding! 📚✨**
