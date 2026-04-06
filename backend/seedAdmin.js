const sequelize = require('./db/connection');
const User = require('./models/User');
const bcrypt = require('bcryptjs');

async function seedAdmin() {
  try {
    // Authenticate and sync DB
    await sequelize.authenticate();
    console.log('✅ Database connected');

    // Check if admin already exists
    const existingAdmin = await User.findOne({ where: { email: 'admin@bookweb.com' } });
    
    if (existingAdmin) {
      console.log('⚠️  Admin user already exists');
      process.exit(0);
    }

    // Create admin user
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash('Admin@123456', salt);

    const admin = await User.create({
      email: 'admin@bookweb.com',
      password: hashedPassword,
      firstName: 'Admin',
      lastName: 'BookWeb',
      role: 'admin',
      status: 'active',
      phone: '1900-2255-39',
      avatar: null,
      address: 'Hà Nội, Việt Nam'
    });

    console.log('✅ Admin user created successfully!');
    console.log('📧 Email: admin@bookweb.com');
    console.log('🔐 Password: Admin@123456');
    
    // Create test user
    const testUserPassword = await bcrypt.hash('User@123456', salt);
    const testUser = await User.create({
      email: 'user@bookweb.com',
      password: testUserPassword,
      firstName: 'Test',
      lastName: 'User',
      role: 'user',
      status: 'active'
    });

    console.log('\n✅ Test user created successfully!');
    console.log('📧 Email: user@bookweb.com');
    console.log('🔐 Password: User@123456');

    process.exit(0);
  } catch (error) {
    console.error('❌ Error:', error.message);
    process.exit(1);
  }
}

seedAdmin();
