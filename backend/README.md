# BookWeb Backend

GraphQL API for book selling application built with Node.js, Express, and Apollo Server.

## Setup

1. Install dependencies:
```bash
npm install
```

2. Configure environment variables:
```bash
cp .env.example .env
```

3. Update `.env` with your database and OAuth credentials

4. Start the server:
```bash
npm run dev
```

The GraphQL endpoint will be available at `http://localhost:4000/graphql`

## API Features

- User authentication (JWT, Google, GitHub)
- Book management (CRUD)
- Categories and Authors
- Reviews and Ratings
- Orders and Order Items
- Search and Filter
- User role management (admin, moderator, user)
- Status toggles for books, users, and products

## Database Tables

1. Users
2. Categories
3. Authors
4. Books
5. Reviews
6. Orders
7. OrderItems
