const { DataTypes } = require('sequelize');
const sequelize = require('../db/connection');

const VoucherRecipient = sequelize.define('VoucherRecipient', {
  id: {
    type: DataTypes.UUID,
    defaultValue: DataTypes.UUIDV4,
    primaryKey: true
  },
  source: {
    type: DataTypes.STRING,
    allowNull: true
  }
});

module.exports = VoucherRecipient;
