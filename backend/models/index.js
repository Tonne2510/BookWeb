const User = require('../models/User');
const Category = require('../models/Category');
const Author = require('../models/Author');
const Book = require('../models/Book');
const Review = require('../models/Review');
const Order = require('../models/Order');
const OrderItem = require('../models/OrderItem');

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

module.exports = {
  User,
  Category,
  Author,
  Book,
  Review,
  Order,
  OrderItem
};
