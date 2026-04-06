const { DataTypes } = require('sequelize');
const sequelize = require('../db/connection');

const VoucherUsage = sequelize.define('VoucherUsage', {
  id: {
    type: DataTypes.UUID,
    defaultValue: DataTypes.UUIDV4,
    primaryKey: true
  },
  discountAmount: {
    type: DataTypes.DECIMAL(10, 2),
    allowNull: false,
    defaultValue: 0
  }
});

module.exports = VoucherUsage;
