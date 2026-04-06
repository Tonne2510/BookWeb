const { DataTypes } = require('sequelize');
const sequelize = require('../db/connection');

const Favorite = sequelize.define('Favorite', {
  id: {
    type: DataTypes.UUID,
    defaultValue: DataTypes.UUIDV4,
    primaryKey: true
  },
  userId: {
    type: DataTypes.UUID,
    allowNull: false
  },
  bookId: {
    type: DataTypes.UUID,
    allowNull: false
  }
}, {
  indexes: [
    {
      unique: true,
      fields: ['userId', 'bookId']
    }
  ]
});

module.exports = Favorite;
