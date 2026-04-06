const { Sequelize } = require('sequelize');
const dotenv = require('dotenv');

dotenv.config();

// Get password - if DB_PASSWORD is explicitly set, use it; otherwise default to '123456'
const dbPassword = process.env.DB_PASSWORD !== undefined && process.env.DB_PASSWORD !== '' 
  ? process.env.DB_PASSWORD 
  : (process.env.DB_PASSWORD === '' ? null : '123456');

const sequelize = new Sequelize(
  process.env.DB_NAME || 'bookweb',
  process.env.DB_USER || 'root',
  dbPassword,
  {
    host: process.env.DB_HOST || 'localhost',
    port: process.env.DB_PORT || 3306,
    dialect: 'mysql',
    logging: process.env.NODE_ENV === 'development' ? console.log : false,
    pool: {
      max: 5,
      min: 0,
      acquire: 30000,
      idle: 10000
    }
  }
);

module.exports = sequelize;
