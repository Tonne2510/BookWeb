const User = require('../models/User');
const Category = require('../models/Category');
const Author = require('../models/Author');
const Book = require('../models/Book');
const Review = require('../models/Review');
const Order = require('../models/Order');
const OrderItem = require('../models/OrderItem');
const Favorite = require('../models/Favorite');
const Voucher = require('../models/Voucher');
const VoucherUsage = require('../models/VoucherUsage');
const VoucherRecipient = require('../models/VoucherRecipient');

// User associations
User.hasMany(Review, { foreignKey: 'userId', onDelete: 'CASCADE' });
User.hasMany(Order, { foreignKey: 'userId', onDelete: 'CASCADE' });

Review.belongsTo(User, { foreignKey: 'userId' });
Order.belongsTo(User, { foreignKey: 'userId' });

// Category associations
Category.hasMany(Book, { foreignKey: 'categoryId', onDelete: 'SET NULL' });
Book.belongsTo(Category, { foreignKey: 'categoryId' });

// Author associations
Author.hasMany(Book, { foreignKey: 'authorId', onDelete: 'SET NULL' });
Book.belongsTo(Author, { foreignKey: 'authorId' });

// Book associations
Book.hasMany(Review, { foreignKey: 'bookId', onDelete: 'CASCADE' });
Review.belongsTo(Book, { foreignKey: 'bookId' });

// Order associations
Order.hasMany(OrderItem, { foreignKey: 'orderId', onDelete: 'CASCADE' });
OrderItem.belongsTo(Order, { foreignKey: 'orderId' });
OrderItem.belongsTo(Book, { foreignKey: 'bookId' });
Book.hasMany(OrderItem, { foreignKey: 'bookId', onDelete: 'RESTRICT' });
Order.hasMany(Review, { foreignKey: 'orderId', onDelete: 'SET NULL' });
Review.belongsTo(Order, { foreignKey: 'orderId' });

// Favorite associations
User.hasMany(Favorite, { foreignKey: 'userId', onDelete: 'CASCADE' });
Favorite.belongsTo(User, { foreignKey: 'userId' });
Book.hasMany(Favorite, { foreignKey: 'bookId', onDelete: 'CASCADE' });
Favorite.belongsTo(Book, { foreignKey: 'bookId' });

// Voucher associations
Voucher.hasMany(VoucherUsage, { foreignKey: 'voucherId', onDelete: 'CASCADE' });
VoucherUsage.belongsTo(Voucher, { foreignKey: 'voucherId' });
User.hasMany(VoucherUsage, { foreignKey: 'userId', onDelete: 'CASCADE' });
VoucherUsage.belongsTo(User, { foreignKey: 'userId' });
Order.hasOne(VoucherUsage, { foreignKey: 'orderId', onDelete: 'SET NULL' });
VoucherUsage.belongsTo(Order, { foreignKey: 'orderId' });
User.hasMany(Voucher, { foreignKey: 'userId', onDelete: 'SET NULL' });
Voucher.belongsTo(User, { foreignKey: 'userId' });
Voucher.hasMany(VoucherRecipient, { foreignKey: 'voucherId', onDelete: 'CASCADE' });
VoucherRecipient.belongsTo(Voucher, { foreignKey: 'voucherId' });
User.hasMany(VoucherRecipient, { foreignKey: 'userId', onDelete: 'CASCADE' });
VoucherRecipient.belongsTo(User, { foreignKey: 'userId' });

module.exports = {
  User,
  Category,
  Author,
  Book,
  Review,
  Order,
  OrderItem,
  Favorite,
  Voucher,
  VoucherUsage,
  VoucherRecipient
};
