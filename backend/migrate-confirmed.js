const db = require('./db/connection');

db.authenticate().then(async () => {
  try {
    await db.query("ALTER TABLE `Orders` MODIFY COLUMN `status` ENUM('pending','processing','shipped','delivered','cancelled','confirmed') NOT NULL DEFAULT 'pending'");
    console.log('✅ Status enum updated to include confirmed');
  } catch(e) {
    console.log('Status alter error (may already exist):', e.message);
  }
  try {
    await db.query("ALTER TABLE `Orders` ADD COLUMN `reviewed` TINYINT(1) NOT NULL DEFAULT 0");
    console.log('✅ reviewed column added');
  } catch(e) {
    console.log('reviewed column error (may already exist):', e.message);
  }
  process.exit(0);
}).catch(e => {
  console.error('DB Error:', e.message);
  process.exit(1);
});
