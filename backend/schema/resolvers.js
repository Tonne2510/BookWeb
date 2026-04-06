const { User, Category, Author, Book, Review, Order, OrderItem, Favorite, Voucher, VoucherUsage } = require('../models');
const authService = require('../utils/authService');
const emailService = require('../utils/emailService');
const { generateSlug } = require('../utils/slugService');
const { Op, Sequelize } = require('sequelize');

// In-memory OTP store: { email -> { otp, expires } }
const otpStore = new Map();

const calculateVoucherDiscount = async ({ code, subtotal, userId }) => {
  const safeSubtotal = Number(subtotal || 0);
  if (!code) {
    return { valid: true, message: 'No voucher', voucher: null, discountAmount: 0, finalAmount: safeSubtotal };
  }

  const normalizedCode = code.trim().toUpperCase();
  const voucher = await Voucher.findOne({ where: { code: normalizedCode } });
  if (!voucher) {
    return { valid: false, message: 'Mã voucher không tồn tại', voucher: null, discountAmount: 0, finalAmount: safeSubtotal };
  }

  if (!voucher.isActive) {
    return { valid: false, message: 'Voucher hiện không khả dụng', voucher, discountAmount: 0, finalAmount: safeSubtotal };
  }

  const now = new Date();
  if (voucher.startDate && now < voucher.startDate) {
    return { valid: false, message: 'Voucher chưa đến thời gian áp dụng', voucher, discountAmount: 0, finalAmount: safeSubtotal };
  }
  if (voucher.endDate && now > voucher.endDate) {
    return { valid: false, message: 'Voucher đã hết hạn', voucher, discountAmount: 0, finalAmount: safeSubtotal };
  }

  if (voucher.totalUsageLimit != null && voucher.usedCount >= voucher.totalUsageLimit) {
    return { valid: false, message: 'Voucher đã hết lượt sử dụng', voucher, discountAmount: 0, finalAmount: safeSubtotal };
  }

  if (safeSubtotal < Number(voucher.minOrderValue || 0)) {
    return {
      valid: false,
      message: `Đơn hàng tối thiểu ₫${Math.round(Number(voucher.minOrderValue || 0)).toLocaleString('vi-VN')} để dùng voucher này`,
      voucher,
      discountAmount: 0,
      finalAmount: safeSubtotal
    };
  }

  if (userId) {
    const usageCount = await VoucherUsage.count({ where: { voucherId: voucher.id, userId } });
    if (usageCount >= Number(voucher.perUserLimit || 1)) {
      return { valid: false, message: 'Bạn đã dùng hết lượt voucher này', voucher, discountAmount: 0, finalAmount: safeSubtotal };
    }
  }

  let discountAmount = 0;
  if (voucher.type === 'percent') {
    discountAmount = (safeSubtotal * Number(voucher.value || 0)) / 100;
    if (voucher.maxDiscount != null) {
      discountAmount = Math.min(discountAmount, Number(voucher.maxDiscount));
    }
  } else {
    discountAmount = Number(voucher.value || 0);
  }

  discountAmount = Math.max(0, Math.min(discountAmount, safeSubtotal));
  const finalAmount = Math.max(0, safeSubtotal - discountAmount);

  return {
    valid: true,
    message: 'Áp dụng voucher thành công',
    voucher,
    discountAmount,
    finalAmount
  };
};

const resolvers = {
  Query: {
    // Auth
    me: async (_, __, { user }) => {
      if (!user) throw new Error('Not authenticated');
      return User.findByPk(user.id);
    },

    // Users
    user: async (_, { id }) => {
      return User.findByPk(id, {
        attributes: { exclude: ['password'] }
      });
    },

    users: async (_, { page = 1, limit = 10, status, role, searchTerm }) => {
      const where = {};
      if (status) where.status = status;
      if (role) where.role = role;
      if (searchTerm) {
        where[Op.or] = [
          Sequelize.where(Sequelize.fn('CONCAT', Sequelize.col('firstName'), ' ', Sequelize.col('lastName')), Op.like, `%${searchTerm}%`),
          { email: { [Op.like]: `%${searchTerm}%` } }
        ];
      }

      return User.findAll({
        where,
        limit,
        offset: (page - 1) * limit,
        attributes: { exclude: ['password'] }
      });
    },

    // Books
    book: async (_, { id, slug }) => {
      const where = id ? { id } : { slug };
      const book = await Book.findOne({
        where,
        include: [
          { model: Category },
          { model: Author }
        ]
      });
      
      if (book) {
        await book.increment('views');
      }
      return book;
    },

    books: async (_, { 
      page = 1, 
      limit = 10, 
      status, 
      categoryId, 
      authorId,
      minPrice,
      maxPrice,
      searchTerm,
      sortBy = 'createdAt',
      order = 'DESC'
    }) => {
      const where = {};
      
      if (status) where.status = status;
      if (categoryId) where.categoryId = categoryId;
      if (authorId) where.authorId = authorId;
      
      if (minPrice || maxPrice) {
        where.price = {};
        if (minPrice) where.price[Op.gte] = minPrice;
        if (maxPrice) where.price[Op.lte] = maxPrice;
      }

      if (searchTerm) {
        where[Op.or] = [
          { title: { [Op.like]: `%${searchTerm}%` } },
          { description: { [Op.like]: `%${searchTerm}%` } },
          { isbn: { [Op.like]: `%${searchTerm}%` } }
        ];
      }

      const { count, rows } = await Book.findAndCountAll({
        where,
        include: [
          { model: Category },
          { model: Author }
        ],
        order: [[sortBy, order]],
        limit,
        offset: (page - 1) * limit
      });

      const pages = Math.ceil(count / limit);
      return {
        books: rows,
        total: count,
        page,
        limit,
        pages
      };
    },

    // Categories
    category: async (_, { id, slug }) => {
      const where = id ? { id } : { slug };
      return Category.findOne({
        where,
        include: [{ model: Book }]
      });
    },

    categories: async (_, { page = 1, limit = 10, status }) => {
      const where = status ? { status } : {};
      const { count, rows } = await Category.findAndCountAll({
        where,
        include: [{ model: Book }],
        limit,
        offset: (page - 1) * limit
      });

      const pages = Math.ceil(count / limit);
      return {
        categories: rows,
        total: count,
        page,
        limit,
        pages
      };
    },

    // Authors
    author: async (_, { id, slug }) => {
      const where = id ? { id } : { slug };
      return Author.findOne({
        where,
        include: [{ model: Book }]
      });
    },

    authors: async (_, { page = 1, limit = 10, status }) => {
      const where = status ? { status } : {};
      const { count, rows } = await Author.findAndCountAll({
        where,
        include: [{ model: Book }],
        limit,
        offset: (page - 1) * limit
      });

      const pages = Math.ceil(count / limit);
      return {
        authors: rows,
        total: count,
        page,
        limit,
        pages
      };
    },

    // Reviews
    reviews: async (_, { bookId, page = 1, limit = 10, status }) => {
      const where = {};
      if (bookId) where.bookId = bookId;
      if (status) where.status = status;
      
      const { count, rows } = await Review.findAndCountAll({
        where,
        include: [
          { model: User, attributes: { exclude: ['password'] } },
          { model: Book }
        ],
        order: [['createdAt', 'DESC']],
        limit,
        offset: (page - 1) * limit
      });

      const pages = Math.ceil(count / limit);
      return {
        reviews: rows,
        total: count,
        page,
        limit,
        pages
      };
    },

    userReviews: async (_, { page = 1, limit = 10 }, { user }) => {
      if (!user) throw new Error('Not authenticated');
      
      const { count, rows } = await Review.findAndCountAll({
        where: { userId: user.id },
        include: [
          { model: User, attributes: { exclude: ['password'] } },
          { model: Book }
        ],
        order: [['createdAt', 'DESC']],
        limit,
        offset: (page - 1) * limit
      });

      const pages = Math.ceil(count / limit);
      return {
        reviews: rows,
        total: count,
        page,
        limit,
        pages
      };
    },

    // Favorites
    myFavorites: async (_, { page = 1, limit = 20 }, { user }) => {
      if (!user) throw new Error('Not authenticated');

      const { count, rows } = await Favorite.findAndCountAll({
        where: { userId: user.id },
        include: [
          { model: Book, include: [{ model: Category }, { model: Author }] }
        ],
        order: [['createdAt', 'DESC']],
        limit,
        offset: (page - 1) * limit
      });

      const pages = Math.ceil(count / limit);
      return {
        favorites: rows,
        total: count,
        page,
        limit,
        pages
      };
    },

    isFavorite: async (_, { bookId }, { user }) => {
      if (!user) return false;
      const fav = await Favorite.findOne({ where: { userId: user.id, bookId } });
      return fav !== null;
    },

    // Vouchers
    vouchers: async (_, { page = 1, limit = 20, isActive, searchTerm }, { user }) => {
      if (!user || user.role !== 'admin') throw new Error('Not authorized');
      const where = {};
      if (typeof isActive === 'boolean') where.isActive = isActive;
      if (searchTerm) {
        where[Op.or] = [
          { code: { [Op.like]: `%${searchTerm}%` } },
          { name: { [Op.like]: `%${searchTerm}%` } }
        ];
      }
      return Voucher.findAll({
        where,
        order: [['createdAt', 'DESC']],
        limit,
        offset: (page - 1) * limit
      });
    },

    validateVoucher: async (_, { code, subtotal }, { user }) => {
      if (!user) throw new Error('Not authenticated');
      return calculateVoucherDiscount({ code, subtotal, userId: user.id });
    },

    // Orders
    order: async (_, { id }, { user }) => {
      if (!user) throw new Error('Not authenticated');
      
      const where = { id };
      // Regular users can only see their own orders
      if (user.role !== 'admin') {
        where.userId = user.id;
      }

      return Order.findOne({
        where,
        include: [
          { model: User, attributes: { exclude: ['password'] } },
          { model: OrderItem, include: [{ model: Book }] }
        ]
      });
    },

    orders: async (_, { page = 1, limit = 10, status, userId }, { user }) => {
      if (!user) throw new Error('Not authenticated');

      const where = {};
      // Admin can see all orders; regular users only see their own
      if (user.role !== 'admin') {
        where.userId = user.id;
      } else if (userId) {
        where.userId = userId;
      }
      if (status) where.status = status;

      const { count, rows } = await Order.findAndCountAll({
        where,
        include: [
          { model: User, attributes: { exclude: ['password'] } },
          { model: OrderItem, include: [{ model: Book }] }
        ],
        order: [['createdAt', 'DESC']],
        limit,
        offset: (page - 1) * limit
      });

      const pages = Math.ceil(count / limit);
      return {
        orders: rows,
        total: count,
        page,
        limit,
        pages
      };
    }
  },

  Mutation: {
    // Auth Mutations
    sendVerificationOtp: async (_, { email }) => {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(email)) {
        throw new Error('Địa chỉ email không hợp lệ');
      }

      const existingUser = await User.findOne({ where: { email } });
      if (existingUser) {
        throw new Error('Email đã được đăng ký. Vui lòng đăng nhập hoặc dùng email khác.');
      }

      const otp = Math.floor(100000 + Math.random() * 900000).toString();
      const expires = Date.now() + 10 * 60 * 1000; // 10 minutes
      otpStore.set(email, { otp, expires });

      try {
        await emailService.sendOtpEmail(email, otp);
        return 'Mã xác nhận đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.';
      } catch (error) {
        otpStore.delete(email);
        console.error('OTP email error:', error);
        throw new Error('Không thể gửi email. Vui lòng kiểm tra lại địa chỉ email và thử lại.');
      }
    },

    register: async (_, { email, password, firstName, lastName, otp }) => {
      // Verify OTP
      const stored = otpStore.get(email);
      if (!stored) {
        throw new Error('Vui lòng gửi mã xác nhận trước khi đăng ký.');
      }
      if (Date.now() > stored.expires) {
        otpStore.delete(email);
        throw new Error('Mã xác nhận đã hết hạn. Vui lòng gửi lại mã mới.');
      }
      if (stored.otp !== otp) {
        throw new Error('Mã xác nhận không đúng. Vui lòng kiểm tra lại.');
      }
      otpStore.delete(email);

      // Validate email format
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(email)) {
        throw new Error('Invalid email format');
      }

      // Validate password
      if (!password || password.length < 6) {
        throw new Error('Password must be at least 6 characters long');
      }

      // Check if email already exists
      const existingUser = await User.findOne({ where: { email } });
      if (existingUser) {
        throw new Error('Email already registered. Please login or use another email.');
      }

      // Create new user
      const user = await User.create({
        email,
        password,
        firstName,
        lastName,
        status: 'active'
      });

      // Send welcome email
      try {
        await emailService.sendWelcomeEmail(user.email, user.firstName);
      } catch (error) {
        console.error('Error sending welcome email:', error);
        // Don't fail registration if email fails
      }

      const token = authService.generateToken(user.id, user.role);
      return {
        token,
        user: {
          id: user.id,
          email: user.email,
          firstName: user.firstName,
          lastName: user.lastName,
          role: user.role,
          status: user.status,
          avatar: user.avatar,
          address: user.address,
          phone: user.phone
        }
      };
    },

    login: async (_, { email, password }) => {
      // Validate inputs
      if (!email || !password) {
        throw new Error('Email and password are required');
      }

      const user = await User.findOne({ where: { email } });
      if (!user) {
        throw new Error('Invalid email or password');
      }

      // Check if account is active
      if (user.status === 'inactive') {
        throw new Error('Account is inactive. Please contact support.');
      }

      if (user.status === 'blocked') {
        throw new Error('Account is blocked. Please contact support.');
      }

      // Verify password (for OAuth users without password)
      if (!user.password && !user.googleId && !user.githubId) {
        throw new Error('This account requires OAuth login');
      }

      if (user.password) {
        const isValidPassword = await authService.comparePassword(password, user.password);
        if (!isValidPassword) {
          throw new Error('Invalid email or password');
        }
      }

      // Update last login
      await user.update({ lastLogin: new Date() });

      const token = authService.generateToken(user.id, user.role);

      return {
        token,
        user: {
          id: user.id,
          email: user.email,
          firstName: user.firstName,
          lastName: user.lastName,
          role: user.role,
          status: user.status,
          avatar: user.avatar,
          address: user.address,
          phone: user.phone
        }
      };
    },

    forgotPassword: async (_, { email }) => {
      const user = await User.findOne({ where: { email } });
      if (!user) {
        // Don't reveal if email exists (security best practice)
        return 'If an account exists, a reset email will be sent';
      }

      const resetToken = authService.generateResetToken();
      const resetTokenExpire = new Date(Date.now() + 24 * 60 * 60 * 1000); // 24 hours

      await user.update({
        resetToken,
        resetTokenExpire
      });

      // Build reset URL
      const resetUrl = `${process.env.FRONTEND_URL || 'http://localhost:8080'}/auth/reset-password?token=${resetToken}`;

      try {
        // Send reset email
        await emailService.sendForgotPasswordEmail(
          user.email,
          user.firstName,
          resetToken,
          resetUrl
        );
        return 'Password reset email sent successfully. Please check your email.';
      } catch (error) {
        console.error('Error sending reset email:', error);
        // Clear reset token if email fails
        await user.update({
          resetToken: null,
          resetTokenExpire: null
        });
        throw new Error('Failed to send reset email. Please try again later.');
      }
    },

    resetPassword: async (_, { token, newPassword }) => {
      if (!newPassword || newPassword.length < 6) {
        throw new Error('Password must be at least 6 characters long');
      }

      const user = await User.findOne({
        where: {
          resetToken: token,
          resetTokenExpire: { [Op.gt]: new Date() }
        }
      });

      if (!user) {
        throw new Error('Invalid or expired reset token');
      }

      // Update password and clear reset token
      await user.update({
        password: newPassword,
        resetToken: null,
        resetTokenExpire: null
      });

      try {
        // Send confirmation email
        await emailService.sendResetPasswordConfirmation(user.email, user.firstName);
      } catch (error) {
        console.error('Error sending confirmation email:', error);
        // Don't fail the reset if confirmation email fails
      }

      return 'Password reset successfully. You can now login with your new password.';
    },

    updateProfile: async (_, { firstName, lastName, phone, address }, { user }) => {
      if (!user) throw new Error('Not authenticated');

      const updates = {};
      if (firstName) updates.firstName = firstName;
      if (lastName) updates.lastName = lastName;
      if (phone) updates.phone = phone;
      if (address) updates.address = address;

      const foundUser = await User.findByPk(user.id);
      if (!foundUser) throw new Error('User not found');
      await foundUser.update(updates);
      return foundUser;
    },

    // User Management (Admin)
    toggleUserStatus: async (_, { userId }, { user }) => {
      if (!user || user.role !== 'admin') throw new Error('Not authorized');

      const targetUser = await User.findByPk(userId);
      if (!targetUser) throw new Error('User not found');

      const newStatus = targetUser.status === 'active' ? 'inactive' : 'active';
      await targetUser.update({ status: newStatus });
      return targetUser;
    },

    changeUserRole: async (_, { userId, role }, { user }) => {
      if (!user || user.role !== 'admin') throw new Error('Not authorized');

      const targetUser = await User.findByPk(userId);
      if (!targetUser) throw new Error('User not found');

      await targetUser.update({ role });
      return targetUser;
    },

    deleteUser: async (_, { userId }, { user }) => {
      if (!user || user.role !== 'admin') throw new Error('Not authorized');

      await User.destroy({ where: { id: userId } });
      return 'User deleted successfully';
    },

    // Book Mutations
    createBook: async (_, args, { user }) => {
      if (!user || !['admin', 'moderator'].includes(user.role)) {
        throw new Error('Not authorized');
      }

      // Generate slug if not provided
      if (!args.slug) {
        args.slug = generateSlug(args.title);
        
        // Check if slug already exists and make it unique
        const existingBook = await Book.findOne({ where: { slug: args.slug } });
        if (existingBook) {
          args.slug = `${args.slug}-${Date.now()}`;
        }
      }

      // Ensure stock is provided
      if (!args.stock) {
        args.stock = 0;
      }

      return Book.create(args);
    },

    updateBook: async (_, { id, ...updates }, { user }) => {
      if (!user || !['admin', 'moderator'].includes(user.role)) {
        throw new Error('Not authorized');
      }

      const book = await Book.findByPk(id);
      if (!book) throw new Error('Book not found');

      await book.update(updates);
      return book;
    },

    toggleBookStatus: async (_, { id }, { user }) => {
      if (!user || !['admin', 'moderator'].includes(user.role)) {
        throw new Error('Not authorized');
      }

      const book = await Book.findByPk(id);
      if (!book) throw new Error('Book not found');

      const statuses = ['active', 'inactive', 'archived'];
      const currentIndex = statuses.indexOf(book.status);
      const newStatus = statuses[(currentIndex + 1) % statuses.length];

      await book.update({ status: newStatus });
      return book;
    },

    deleteBook: async (_, { id }, { user }) => {
      if (!user || !['admin', 'moderator'].includes(user.role)) {
        throw new Error('Not authorized');
      }

      await Book.destroy({ where: { id } });
      return 'Book deleted successfully';
    },

    // Category Mutations
    createCategory: async (_, args, { user }) => {
      if (!user || !['admin', 'moderator'].includes(user.role)) {
        throw new Error('Not authorized');
      }

      // Generate slug if not provided
      if (!args.slug) {
        args.slug = generateSlug(args.name);
        
        // Check if slug already exists and make it unique
        const existingCategory = await Category.findOne({ where: { slug: args.slug } });
        if (existingCategory) {
          args.slug = `${args.slug}-${Date.now()}`;
        }
      }

      return Category.create(args);
    },

    updateCategory: async (_, { id, ...updates }, { user }) => {
      if (!user || !['admin', 'moderator'].includes(user.role)) {
        throw new Error('Not authorized');
      }

      const category = await Category.findByPk(id);
      if (!category) throw new Error('Category not found');

      await category.update(updates);
      return category;
    },

    toggleCategoryStatus: async (_, { id }, { user }) => {
      if (!user || !['admin', 'moderator'].includes(user.role)) {
        throw new Error('Not authorized');
      }

      const category = await Category.findByPk(id);
      if (!category) throw new Error('Category not found');

      const newStatus = category.status === 'active' ? 'inactive' : 'active';
      await category.update({ status: newStatus });
      return category;
    },

    deleteCategory: async (_, { id }, { user }) => {
      if (!user || !['admin', 'moderator'].includes(user.role)) {
        throw new Error('Not authorized');
      }

      await Category.destroy({ where: { id } });
      return 'Category deleted successfully';
    },

    // Author Mutations
    createAuthor: async (_, args, { user }) => {
      if (!user || !['admin', 'moderator'].includes(user.role)) {
        throw new Error('Not authorized');
      }

      // Generate slug if not provided
      if (!args.slug) {
        args.slug = generateSlug(args.name);
        
        // Check if slug already exists and make it unique
        const existingAuthor = await Author.findOne({ where: { slug: args.slug } });
        if (existingAuthor) {
          args.slug = `${args.slug}-${Date.now()}`;
        }
      }

      return Author.create(args);
    },

    updateAuthor: async (_, { id, ...updates }, { user }) => {
      if (!user || !['admin', 'moderator'].includes(user.role)) {
        throw new Error('Not authorized');
      }

      const author = await Author.findByPk(id);
      if (!author) throw new Error('Author not found');

      await author.update(updates);
      return author;
    },

    toggleAuthorStatus: async (_, { id }, { user }) => {
      if (!user || !['admin', 'moderator'].includes(user.role)) {
        throw new Error('Not authorized');
      }

      const author = await Author.findByPk(id);
      if (!author) throw new Error('Author not found');

      const newStatus = author.status === 'active' ? 'inactive' : 'active';
      await author.update({ status: newStatus });
      return author;
    },

    deleteAuthor: async (_, { id }, { user }) => {
      if (!user || !['admin', 'moderator'].includes(user.role)) {
        throw new Error('Not authorized');
      }

      await Author.destroy({ where: { id } });
      return 'Author deleted successfully';
    },

    // Review Mutations
    createReview: async (_, { bookId, rating, title, content, imageUrl, orderId }, { user }) => {
      if (!user) throw new Error('Not authenticated');

      const review = await Review.create({
        bookId,
        userId: user.id,
        rating,
        title,
        content,
        imageUrl,
        status: 'approved'
      });

      // Recalculate average rating from approved reviews
      const approvedReviews = await Review.findAll({ where: { bookId, status: 'approved' } });
      if (approvedReviews.length > 0) {
        const avg = approvedReviews.reduce((sum, r) => sum + r.rating, 0) / approvedReviews.length;
        await Book.update({ rating: Math.round(avg * 10) / 10 }, { where: { id: bookId } });
      }

      // If orderId provided, auto-confirm the order and mark as reviewed
      if (orderId) {
        const order = await Order.findByPk(orderId);
        if (order && order.userId === user.id && ['delivered', 'confirmed'].includes(order.status)) {
          await order.update({ status: 'confirmed', reviewed: true });
        }
      }

      return Review.findByPk(review.id, {
        include: [
          { model: User, attributes: { exclude: ['password'] } },
          { model: Book }
        ]
      });
    },

    updateReview: async (_, { id, rating, title, content }, { user }) => {
      if (!user) throw new Error('Not authenticated');

      const review = await Review.findByPk(id);
      if (!review) throw new Error('Review not found');
      if (review.userId !== user.id && user.role !== 'admin') {
        throw new Error('Not authorized');
      }

      const updates = {};
      if (rating) updates.rating = rating;
      if (title) updates.title = title;
      if (content) updates.content = content;

      await review.update(updates);
      return Review.findByPk(id, {
        include: [
          { model: User, attributes: { exclude: ['password'] } },
          { model: Book }
        ]
      });
    },

    deleteReview: async (_, { id }, { user }) => {
      if (!user) throw new Error('Not authenticated');

      const review = await Review.findByPk(id);
      if (!review) throw new Error('Review not found');
      if (review.userId !== user.id && user.role !== 'admin') {
        throw new Error('Not authorized');
      }

      await Review.destroy({ where: { id } });
      return 'Review deleted successfully';
    },

    approveReview: async (_, { id }, { user }) => {
      if (!user || user.role !== 'admin') throw new Error('Not authorized');

      const review = await Review.findByPk(id);
      if (!review) throw new Error('Review not found');

      await review.update({ status: 'approved' });

      // Recalculate average rating from approved reviews
      const bookId = review.bookId;
      const approvedReviews = await Review.findAll({ where: { bookId, status: 'approved' } });
      if (approvedReviews.length > 0) {
        const avg = approvedReviews.reduce((sum, r) => sum + r.rating, 0) / approvedReviews.length;
        await Book.update({ rating: Math.round(avg * 10) / 10 }, { where: { id: bookId } });
      }

      return Review.findByPk(id, {
        include: [
          { model: User, attributes: { exclude: ['password'] } },
          { model: Book }
        ]
      });
    },

    rejectReview: async (_, { id }, { user }) => {
      if (!user || user.role !== 'admin') throw new Error('Not authorized');

      const review = await Review.findByPk(id);
      if (!review) throw new Error('Review not found');

      await review.update({ status: 'rejected' });

      // Recalculate average rating from remaining approved reviews
      const bookId = review.bookId;
      const approvedReviews = await Review.findAll({ where: { bookId, status: 'approved' } });
      const avg = approvedReviews.length > 0
        ? approvedReviews.reduce((sum, r) => sum + r.rating, 0) / approvedReviews.length
        : 0;
      await Book.update({ rating: Math.round(avg * 10) / 10 }, { where: { id: bookId } });

      return Review.findByPk(id, {
        include: [
          { model: User, attributes: { exclude: ['password'] } },
          { model: Book }
        ]
      });
    },

    // Order Mutations
    createOrder: async (_, { items, shippingAddress, paymentMethod, customerName, customerEmail, customerPhone, voucherCode }, { user }) => {
      if (!user) throw new Error('Not authenticated');

      let subtotal = 0;
      let totalDiscount = 0;

      // Validate and calculate totals
      const orderItems = [];
      for (const item of items) {
        const book = await Book.findByPk(item.bookId);
        if (!book) throw new Error(`Book ${item.bookId} not found`);
        if (book.stock < item.quantity) throw new Error(`Insufficient stock for ${book.title}`);

        const price = Number(book.price) * item.quantity;
        const discount = (Number(book.discount || 0) / 100) * price;
        subtotal += price;
        totalDiscount += discount;

        orderItems.push({
          bookId: item.bookId,
          quantity: item.quantity,
          price: book.price,
          discount: (book.discount || 0) / 100
        });

        // Decrease stock
        await book.decrement('stock', { by: item.quantity });
      }

      const amountAfterBookDiscount = Math.max(0, subtotal - totalDiscount);
      const voucherResult = await calculateVoucherDiscount({
        code: voucherCode,
        subtotal: amountAfterBookDiscount,
        userId: user.id
      });
      if (!voucherResult.valid) {
        throw new Error(voucherResult.message);
      }

      const voucherDiscount = Number(voucherResult.discountAmount || 0);
      const finalTotal = Number(voucherResult.finalAmount || amountAfterBookDiscount);

      const orderNumber = `ORD-${Date.now()}`;
      const order = await Order.create({
        userId: user.id,
        orderNumber,
        totalPrice: finalTotal,
        voucherCode: voucherResult.voucher ? voucherResult.voucher.code : null,
        voucherDiscount,
        totalDiscount,
        shippingAddress,
        paymentMethod,
        customerName,
        customerEmail,
        customerPhone
      });

      if (voucherResult.voucher) {
        await VoucherUsage.create({
          voucherId: voucherResult.voucher.id,
          userId: user.id,
          orderId: order.id,
          discountAmount: voucherDiscount
        });
        await voucherResult.voucher.increment('usedCount');
      }

      for (const item of orderItems) {
        await OrderItem.create({
          orderId: order.id,
          ...item
        });
      }

      return Order.findByPk(order.id, {
        include: [
          { model: User, attributes: { exclude: ['password'] } },
          { model: OrderItem, include: [{ model: Book }] }
        ]
      });
    },

    updateOrderStatus: async (_, { id, status }, { user }) => {
      if (!user || user.role !== 'admin') throw new Error('Not authorized');
      if (status === 'confirmed') throw new Error('Admin cannot set status to confirmed');

      const order = await Order.findByPk(id);
      if (!order) throw new Error('Order not found');

      await order.update({ status });
      return Order.findByPk(id, {
        include: [
          { model: User, attributes: { exclude: ['password'] } },
          { model: OrderItem, include: [{ model: Book }] }
        ]
      });
    },

    confirmOrder: async (_, { id }, { user }) => {
      if (!user) throw new Error('Not authenticated');

      const order = await Order.findByPk(id);
      if (!order) throw new Error('Order not found');
      if (order.userId !== user.id) throw new Error('Not authorized');
      if (order.status !== 'delivered') throw new Error('Only delivered orders can be confirmed');

      await order.update({ status: 'confirmed' });
      return Order.findByPk(id, {
        include: [
          { model: User, attributes: { exclude: ['password'] } },
          { model: OrderItem, include: [{ model: Book }] }
        ]
      });
    },

    createVoucher: async (_, args, { user }) => {
      if (!user || user.role !== 'admin') throw new Error('Not authorized');
      const code = args.code.trim().toUpperCase();
      const exists = await Voucher.findOne({ where: { code } });
      if (exists) throw new Error('Voucher code already exists');
      const payload = {
        ...args,
        code,
        minOrderValue: args.minOrderValue || 0,
        perUserLimit: args.perUserLimit || 1,
        isActive: typeof args.isActive === 'boolean' ? args.isActive : true
      };
      return Voucher.create(payload);
    },

    updateVoucher: async (_, { id, ...updates }, { user }) => {
      if (!user || user.role !== 'admin') throw new Error('Not authorized');
      const voucher = await Voucher.findByPk(id);
      if (!voucher) throw new Error('Voucher not found');
      await voucher.update(updates);
      return voucher;
    },

    toggleVoucherStatus: async (_, { id }, { user }) => {
      if (!user || user.role !== 'admin') throw new Error('Not authorized');
      const voucher = await Voucher.findByPk(id);
      if (!voucher) throw new Error('Voucher not found');
      await voucher.update({ isActive: !voucher.isActive });
      return voucher;
    },

    deleteVoucher: async (_, { id }, { user }) => {
      if (!user || user.role !== 'admin') throw new Error('Not authorized');
      await VoucherUsage.destroy({ where: { voucherId: id } });
      await Voucher.destroy({ where: { id } });
      return 'Voucher deleted successfully';
    },

    cancelOrder: async (_, { id }, { user }) => {

      const order = await Order.findByPk(id);
      if (!order) throw new Error('Order not found');
      if (order.userId !== user.id && user.role !== 'admin') {
        throw new Error('Not authorized');
      }

      if (!['pending', 'processing'].includes(order.status)) {
        throw new Error('Cannot cancel this order');
      }

      // Restore stock
      const items = await OrderItem.findAll({ where: { orderId: id } });
      for (const item of items) {
        await Book.increment('stock', {
          by: item.quantity,
          where: { id: item.bookId }
        });
      }

      await order.update({ status: 'cancelled' });
      return Order.findByPk(id, {
        include: [
          { model: User, attributes: { exclude: ['password'] } },
          { model: OrderItem, include: [{ model: Book }] }
        ]
      });
    },

    // Favorite Mutations
    addToFavorites: async (_, { bookId }, { user }) => {
      if (!user) throw new Error('Not authenticated');

      const book = await Book.findByPk(bookId);
      if (!book) throw new Error('Book not found');

      const [fav] = await Favorite.findOrCreate({
        where: { userId: user.id, bookId },
        defaults: { userId: user.id, bookId }
      });

      return Favorite.findByPk(fav.id, {
        include: [
          { model: User, attributes: { exclude: ['password'] } },
          { model: Book, include: [{ model: Category }, { model: Author }] }
        ]
      });
    },

    removeFromFavorites: async (_, { bookId }, { user }) => {
      if (!user) throw new Error('Not authenticated');

      const deleted = await Favorite.destroy({
        where: { userId: user.id, bookId }
      });

      if (deleted === 0) throw new Error('Favorite not found');
      return 'Đã xóa khỏi danh sách yêu thích';
    }
  },

  // Field Resolvers
  User: {
    fullName: (parent) => `${parent.firstName} ${parent.lastName}`,
    createdAt: (parent) => parent.createdAt instanceof Date ? parent.createdAt.toISOString() : (parent.createdAt ? String(parent.createdAt) : null),
    updatedAt: (parent) => parent.updatedAt instanceof Date ? parent.updatedAt.toISOString() : (parent.updatedAt ? String(parent.updatedAt) : null)
  },

  Category: {
    books: (parent) => parent.Books || []
  },

  Author: {
    books: (parent) => parent.Books || []
  },

  Book: {
    finalPrice: (parent) => {
      const discount = parent.discount || 0;
      return parent.price * (1 - discount / 100);
    },
    category: (parent) => parent.Category || null,
    author: (parent) => parent.Author || null,
    reviews: (parent) => parent.Reviews || [],
    orderItems: (parent) => parent.OrderItems || []
  },

  Review: {
    user: (parent) => parent.User || null,
    book: (parent) => parent.Book || null,
    createdAt: (parent) => parent.createdAt instanceof Date ? parent.createdAt.toISOString() : (parent.createdAt ? String(parent.createdAt) : null),
    updatedAt: (parent) => parent.updatedAt instanceof Date ? parent.updatedAt.toISOString() : (parent.updatedAt ? String(parent.updatedAt) : null)
  },

  Order: {
    user: (parent) => parent.User || null,
    items: (parent) => parent.OrderItems || [],
    createdAt: (parent) => parent.createdAt instanceof Date ? parent.createdAt.toISOString() : (parent.createdAt ? String(parent.createdAt) : null),
    updatedAt: (parent) => parent.updatedAt instanceof Date ? parent.updatedAt.toISOString() : (parent.updatedAt ? String(parent.updatedAt) : null)
  },

  OrderItem: {
    book: (parent) => parent.Book || null,
    order: (parent) => parent.Order || null
  },

  Favorite: {
    user: (parent) => parent.User || null,
    book: (parent) => parent.Book || null,
    createdAt: (parent) => parent.createdAt instanceof Date ? parent.createdAt.toISOString() : (parent.createdAt ? String(parent.createdAt) : null)
  }
};

module.exports = resolvers;
