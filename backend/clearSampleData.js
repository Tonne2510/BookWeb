const db = require('./db/connection');
const { Book, Category, Author, Review, OrderItem, Order } = require('./models');

const clearSampleData = async () => {
  try {
    console.log('🗑️  Starting to clear sample data...');
    
    await db.authenticate();
    console.log('✅ Database connected');

    // Delete in order of foreign key dependencies
    // 1. Delete OrderItems first (depends on Book and Order)
    const deletedOrderItems = await OrderItem.destroy({ where: {} });
    console.log(`✅ Deleted ${deletedOrderItems} order items`);

    // 2. Delete Reviews (depends on Book and User)
    const deletedReviews = await Review.destroy({ where: {} });
    console.log(`✅ Deleted ${deletedReviews} reviews`);

    // 3. Delete Orders (depends on User)
    const deletedOrders = await Order.destroy({ where: {} });
    console.log(`✅ Deleted ${deletedOrders} orders`);

    // 4. Delete Books (depends on Category and Author)
    const deletedBooks = await Book.destroy({ where: {} });
    console.log(`✅ Deleted ${deletedBooks} books`);

    // 5. Delete Authors
    const deletedAuthors = await Author.destroy({ where: {} });
    console.log(`✅ Deleted ${deletedAuthors} authors`);

    // 6. Delete Categories
    const deletedCategories = await Category.destroy({ where: {} });
    console.log(`✅ Deleted ${deletedCategories} categories`);

    console.log('\n🎉 All sample data cleared successfully!');
    process.exit(0);
  } catch (err) {
    console.error('❌ Error clearing data:', err.message);
    process.exit(1);
  }
};

clearSampleData();
