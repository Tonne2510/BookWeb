const { DataTypes } = require('sequelize');
const sequelize = require('../db/connection');

const Voucher = sequelize.define('Voucher', {
  id: {
    type: DataTypes.UUID,
    defaultValue: DataTypes.UUIDV4,
    primaryKey: true
  },
  code: {
    type: DataTypes.STRING,
    allowNull: false,
    unique: true
  },
  name: {
    type: DataTypes.STRING,
    allowNull: false
  },
  description: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  type: {
    type: DataTypes.ENUM('percent', 'fixed'),
    allowNull: false,
    defaultValue: 'percent'
  },
  value: {
    type: DataTypes.DECIMAL(10, 2),
    allowNull: false
  },
  minOrderValue: {
    type: DataTypes.DECIMAL(10, 2),
    allowNull: false,
    defaultValue: 0
  },
  maxDiscount: {
    type: DataTypes.DECIMAL(10, 2),
    allowNull: true
  },
  totalUsageLimit: {
    type: DataTypes.INTEGER,
    allowNull: true
  },
  usedCount: {
    type: DataTypes.INTEGER,
    allowNull: false,
    defaultValue: 0
  },
  perUserLimit: {
    type: DataTypes.INTEGER,
    allowNull: false,
    defaultValue: 1
  },
  startDate: {
    type: DataTypes.DATE,
    allowNull: false
  },
  endDate: {
    type: DataTypes.DATE,
    allowNull: false
  },
  isActive: {
    type: DataTypes.BOOLEAN,
    allowNull: false,
    defaultValue: true
  }
});

module.exports = Voucher;
