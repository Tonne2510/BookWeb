const { gql } = require('graphql-tag');

const typeDefs = gql`
  enum UserRole {
    user
    admin
    moderator
  }

  enum UserStatus {
    active
    inactive
    blocked
  }

  enum BookStatus {
    active
    inactive
    archived
  }

  enum ReviewStatus {
    approved
    pending
    rejected
  }

  enum OrderStatus {
    pending
    processing
    shipped
    delivered
    cancelled
    confirmed
  }

  enum PaymentMethod {
    credit_card
    bank_transfer
    cash_on_delivery
    vietqr
  }

  enum VoucherType {
    percent
    fixed
  }

  type User {
    id: ID!
    email: String!
    firstName: String!
    lastName: String!
    fullName: String!
    phone: String
    avatar: String
    address: String
    role: UserRole!
    status: UserStatus!
    lastLogin: String
    createdAt: String!
    updatedAt: String!
    reviews: [Review!]
    orders: [Order!]
  }

  type Category {
    id: ID!
    name: String!
    slug: String!
    description: String
    image: String
    status: BookStatus!
    books: [Book!]
    createdAt: String!
    updatedAt: String!
  }

  type Author {
    id: ID!
    name: String!
    slug: String!
    bio: String
    imageUrl: String
    dateOfBirth: String
    nationality: String
    status: BookStatus!
    books: [Book!]
    createdAt: String!
    updatedAt: String!
  }

  type Book {
    id: ID!
    title: String!
    slug: String!
    description: String
    isbn: String
    price: Float!
    discount: Float
    finalPrice: Float!
    coverImage: String
    publisher: String
    publicationDate: String
    pages: Int
    stock: Int
    rating: Float
    views: Int
    status: BookStatus!
    category: Category
    author: Author
    reviews: [Review!]
    orderItems: [OrderItem!]
    createdAt: String!
    updatedAt: String!
  }

  type Review {
    id: ID!
    rating: Int!
    title: String
    content: String!
    imageUrl: String
    helpfulCount: Int
    status: ReviewStatus!
    user: User!
    book: Book!
    createdAt: String!
    updatedAt: String!
  }

  type Order {
    id: ID!
    orderNumber: String!
    customerName: String
    customerEmail: String
    customerPhone: String
    totalPrice: Float!
    voucherCode: String
    voucherDiscount: Float
    totalDiscount: Float
    shippingAddress: String!
    shippingCost: Float
    paymentMethod: PaymentMethod!
    status: OrderStatus!
    reviewed: Boolean!
    notes: String
    user: User!
    items: [OrderItem!]
    createdAt: String!
    updatedAt: String!
  }

  type OrderItem {
    id: ID!
    quantity: Int!
    price: Float!
    discount: Float
    book: Book!
    order: Order!
  }

  type Favorite {
    id: ID!
    user: User!
    book: Book!
    createdAt: String!
  }

  type Voucher {
    id: ID!
    code: String!
    name: String!
    description: String
    type: VoucherType!
    value: Float!
    minOrderValue: Float
    maxDiscount: Float
    totalUsageLimit: Int
    usedCount: Int
    perUserLimit: Int
    startDate: String!
    endDate: String!
    isActive: Boolean!
    createdAt: String!
    updatedAt: String!
  }

  type VoucherValidation {
    valid: Boolean!
    message: String!
    voucher: Voucher
    discountAmount: Float!
    finalAmount: Float!
  }

  type PaginatedFavorites {
    favorites: [Favorite!]!
    total: Int!
    page: Int!
    limit: Int!
    pages: Int!
  }

  type AuthPayload {
    token: String!
    user: User!
  }

  type PaginatedBooks {
    books: [Book!]!
    total: Int!
    page: Int!
    limit: Int!
    pages: Int!
  }

  type PaginatedReviews {
    reviews: [Review!]!
    total: Int!
    page: Int!
    limit: Int!
    pages: Int!
  }

  type PaginatedCategories {
    categories: [Category!]!
    total: Int!
    page: Int!
    limit: Int!
    pages: Int!
  }

  type PaginatedAuthors {
    authors: [Author!]!
    total: Int!
    page: Int!
    limit: Int!
    pages: Int!
  }

  type PaginatedOrders {
    orders: [Order!]!
    total: Int!
    page: Int!
    limit: Int!
    pages: Int!
  }

  type Query {
    # Auth
    me: User!
    
    # Users
    user(id: ID!): User
    users(
      page: Int
      limit: Int
      status: UserStatus
      role: UserRole
      searchTerm: String
    ): [User!]!

    # Books
    book(id: ID, slug: String): Book
    books(
      page: Int
      limit: Int
      status: BookStatus
      categoryId: ID
      authorId: ID
      minPrice: Float
      maxPrice: Float
      searchTerm: String
      sortBy: String
      order: String
    ): PaginatedBooks!

    # Categories
    category(id: ID, slug: String): Category
    categories(page: Int, limit: Int, status: BookStatus): PaginatedCategories!

    # Authors
    author(id: ID, slug: String): Author
    authors(page: Int, limit: Int, status: BookStatus): PaginatedAuthors!

    # Reviews
    reviews(bookId: ID, page: Int, limit: Int, status: ReviewStatus): PaginatedReviews!
    userReviews(page: Int, limit: Int): PaginatedReviews!

    # Favorites
    myFavorites(page: Int, limit: Int): PaginatedFavorites!
    isFavorite(bookId: ID!): Boolean!

    # Vouchers
    vouchers(page: Int, limit: Int, isActive: Boolean, searchTerm: String): [Voucher!]!
    validateVoucher(code: String!, subtotal: Float!): VoucherValidation!

    # Orders
    order(id: ID!): Order
    orders(
      page: Int
      limit: Int
      status: OrderStatus
      userId: ID
    ): PaginatedOrders!
  }

  type Mutation {
    # Auth
    register(email: String!, password: String!, firstName: String!, lastName: String!, otp: String!): AuthPayload!
    login(email: String!, password: String!): AuthPayload!
    sendVerificationOtp(email: String!): String!
    forgotPassword(email: String!): String!
    resetPassword(token: String!, newPassword: String!): String!
    updateProfile(firstName: String, lastName: String, phone: String, address: String): User!

    # Users (Admin)
    toggleUserStatus(userId: ID!): User!
    changeUserRole(userId: ID!, role: UserRole!): User!
    deleteUser(userId: ID!): String!

    # Books
    createBook(
      title: String!
      slug: String
      description: String
      isbn: String
      price: Float!
      discount: Float
      coverImage: String
      publisher: String
      publicationDate: String
      pages: Int
      stock: Int
      categoryId: ID
      authorId: ID
    ): Book!

    updateBook(
      id: ID!
      title: String
      slug: String
      description: String
      isbn: String
      price: Float
      discount: Float
      coverImage: String
      publisher: String
      publicationDate: String
      pages: Int
      stock: Int
      categoryId: ID
      authorId: ID
      status: BookStatus
    ): Book!

    toggleBookStatus(id: ID!): Book!
    deleteBook(id: ID!): String!

    # Categories
    createCategory(
      name: String!
      slug: String
      description: String
      image: String
    ): Category!

    updateCategory(
      id: ID!
      name: String
      slug: String
      description: String
      image: String
      status: BookStatus
    ): Category!

    toggleCategoryStatus(id: ID!): Category!
    deleteCategory(id: ID!): String!

    # Authors
    createAuthor(
      name: String!
      slug: String
      bio: String
      imageUrl: String
      dateOfBirth: String
      nationality: String
    ): Author!

    updateAuthor(
      id: ID!
      name: String
      slug: String
      bio: String
      imageUrl: String
      dateOfBirth: String
      nationality: String
      status: BookStatus
    ): Author!

    toggleAuthorStatus(id: ID!): Author!
    deleteAuthor(id: ID!): String!

    # Reviews
    createReview(bookId: ID!, rating: Int!, title: String, content: String!, imageUrl: String, orderId: ID): Review!
    updateReview(id: ID!, rating: Int, title: String, content: String): Review!
    deleteReview(id: ID!): String!
    approveReview(id: ID!): Review!
    rejectReview(id: ID!): Review!

    # Favorites
    addToFavorites(bookId: ID!): Favorite!
    removeFromFavorites(bookId: ID!): String!

    # Orders
    createOrder(items: [OrderItemInput!]!, shippingAddress: String!, paymentMethod: PaymentMethod!, customerName: String, customerEmail: String, customerPhone: String, voucherCode: String): Order!
    updateOrderStatus(id: ID!, status: OrderStatus!): Order!
    cancelOrder(id: ID!): Order!
    confirmOrder(id: ID!): Order!

    # Vouchers (Admin)
    createVoucher(
      code: String!
      name: String!
      description: String
      type: VoucherType!
      value: Float!
      minOrderValue: Float
      maxDiscount: Float
      totalUsageLimit: Int
      perUserLimit: Int
      startDate: String!
      endDate: String!
      isActive: Boolean
    ): Voucher!
    updateVoucher(
      id: ID!
      name: String
      description: String
      type: VoucherType
      value: Float
      minOrderValue: Float
      maxDiscount: Float
      totalUsageLimit: Int
      perUserLimit: Int
      startDate: String
      endDate: String
      isActive: Boolean
    ): Voucher!
    toggleVoucherStatus(id: ID!): Voucher!
    deleteVoucher(id: ID!): String!
  }

  input OrderItemInput {
    bookId: ID!
    quantity: Int!
  }
`;

module.exports = typeDefs;
