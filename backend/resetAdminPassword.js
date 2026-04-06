const sequelize = require('./db/connection');
const User = require('./models/User');
const bcrypt = require('bcryptjs');

async function resetAdminPassword() {
  try {
    // Authenticate and sync DB
    await sequelize.authenticate();
    console.log('✅ Database connected');

    // Find admin user
    const admin = await User.findOne({ where: { email: 'admin@bookweb.com' } });
    
    if (!admin) {
      console.log('❌ Admin user not found. Creating...');
      
      // DO NOT hash password - User model beforeCreate hook will handle it
      const newAdmin = await User.create({
        email: 'admin@bookweb.com',
        password: '123456',
        firstName: 'Admin',
        lastName: 'BookWeb',
        role: 'admin',
        status: 'active',
        phone: '1900-2255-39'
      });
      
      console.log('✅ Admin user created!');
    } else {
      // Update password - DO NOT hash, User model beforeUpdate hook will handle it
      await admin.update({ password: '123456' });
      console.log('✅ Admin password updated!');
    }

    console.log('\n📧 Login Email: admin@bookweb.com');
    console.log('🔐 Login Password: 123456');

    process.exit(0);
  } catch (error) {
    console.error('❌ Error:', error.message);
    process.exit(1);
  }
}

resetAdminPassword();
