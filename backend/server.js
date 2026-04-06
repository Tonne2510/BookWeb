const express = require("express");
const { ApolloServer } = require("@apollo/server");
const { expressMiddleware } = require("@apollo/server/express4");
const cors = require("cors");
const dotenv = require("dotenv");
const db = require("./db/connection");
const typeDefs = require("./schema/typeDefs");
const resolvers = require("./schema/resolvers");
const authMiddleware = require("./middleware/auth");
const passport = require("passport");
const mbService = require("./utils/mbService");
require("./config/passport");

dotenv.config();

const app = express();
app.set("trust proxy", 1);

const getAllowedOrigins = () => {
  const configured = (process.env.FRONTEND_URL || "")
    .split(",")
    .map((origin) => origin.trim())
    .filter(Boolean);

  const defaults = [
    "http://localhost:8080",
    "http://127.0.0.1:8080",
    "https://localhost:8080",
  ];

  return Array.from(new Set([...defaults, ...configured]));
};

const allowedOrigins = getAllowedOrigins();

const ensureReviewImageColumn = async () => {
  try {
    await db.query(
      "ALTER TABLE `Reviews` ADD COLUMN `imageUrl` VARCHAR(500) NULL",
    );
    console.log("✅ Added Reviews.imageUrl column");
  } catch (error) {
    // Ignore duplicate column errors for repeated startups
    if (
      error &&
      (error.original?.code === "ER_DUP_FIELDNAME" ||
        error.parent?.code === "ER_DUP_FIELDNAME")
    ) {
      return;
    }
    throw error;
  }
};

const ensureOrderVoucherColumns = async () => {
  try {
    await db.query(
      "ALTER TABLE `Orders` ADD COLUMN `voucherCode` VARCHAR(255) NULL",
    );
    console.log("✅ Added Orders.voucherCode column");
  } catch (error) {
    if (
      !(
        error &&
        (error.original?.code === "ER_DUP_FIELDNAME" ||
          error.parent?.code === "ER_DUP_FIELDNAME")
      )
    ) {
      throw error;
    }
  }

  try {
    await db.query(
      "ALTER TABLE `Orders` ADD COLUMN `voucherDiscount` DECIMAL(10,2) NOT NULL DEFAULT 0",
    );
    console.log("✅ Added Orders.voucherDiscount column");
  } catch (error) {
    if (
      !(
        error &&
        (error.original?.code === "ER_DUP_FIELDNAME" ||
          error.parent?.code === "ER_DUP_FIELDNAME")
      )
    ) {
      throw error;
    }
  }

  try {
    await db.query(
      "ALTER TABLE `Orders` ADD COLUMN `reviewed` TINYINT(1) NOT NULL DEFAULT 0",
    );
    console.log("✅ Added Orders.reviewed column");
  } catch (error) {
    if (
      !(
        error &&
        (error.original?.code === "ER_DUP_FIELDNAME" ||
          error.parent?.code === "ER_DUP_FIELDNAME")
      )
    ) {
      throw error;
    }
  }
};

const ensureVoucherGiftColumns = async () => {
  const alterStatements = [
    'ALTER TABLE `Vouchers` ADD COLUMN `userId` CHAR(36) NULL',
    'ALTER TABLE `Vouchers` ADD COLUMN `distributionType` ENUM(\'code\',\'gift\') NOT NULL DEFAULT \'code\'',
    'ALTER TABLE `Vouchers` ADD COLUMN `giftSource` VARCHAR(255) NULL',
    'ALTER TABLE `Vouchers` ADD COLUMN `giftConditionType` ENUM(\'amount\',\'review\') NULL',
    'ALTER TABLE `Vouchers` ADD COLUMN `minGiftAmount` DECIMAL(10,2) NULL',
    'ALTER TABLE `Vouchers` ADD COLUMN `maxGiftAmount` DECIMAL(10,2) NULL',
    'ALTER TABLE `Vouchers` ADD COLUMN `minGiftReviewCount` INT NULL',
    'ALTER TABLE `Vouchers` ADD COLUMN `maxGiftReviewCount` INT NULL',
    'ALTER TABLE `Vouchers` ADD COLUMN `giftedCount` INT NOT NULL DEFAULT 0',
    'ALTER TABLE `Vouchers` ADD COLUMN `giftedBySystem` TINYINT(1) NOT NULL DEFAULT 0',
    'ALTER TABLE `Vouchers` ADD COLUMN `sourceTemplateId` CHAR(36) NULL'
  ];

  for (const sql of alterStatements) {
    try {
      await db.query(sql);
    } catch (error) {
      if (!(error && (error.original?.code === 'ER_DUP_FIELDNAME' || error.parent?.code === 'ER_DUP_FIELDNAME'))) {
        throw error;
      }
    }
  }

  // Drop old columns if they exist
  const dropStatements = [
    'ALTER TABLE `Vouchers` DROP COLUMN IF EXISTS `rewardTier`',
    'ALTER TABLE `Vouchers` DROP COLUMN IF EXISTS `triggerOrderAmount`',
    'ALTER TABLE `Vouchers` DROP COLUMN IF EXISTS `triggerReviewCount`'
  ];

  for (const sql of dropStatements) {
    try {
      await db.query(sql);
    } catch (error) {
      // Ignore errors for columns that don't exist
    }
  }
};

const ensureOrderStatusConfirmed = async () => {
  try {
    await db.query(
      "ALTER TABLE `Orders` MODIFY COLUMN `status` ENUM('pending','processing','shipped','delivered','cancelled','confirmed') NOT NULL DEFAULT 'pending'",
    );
    console.log("✅ Orders.status ENUM updated with confirmed");
  } catch (error) {
    // Ignore if already contains enum value (some DB versions throw ER_DUP_KEYNAME or similar)
    if (
      error &&
      (error.original?.code === "ER_DUP_FIELDNAME" ||
        error.parent?.code === "ER_DUP_FIELDNAME")
    ) {
      return;
    }
    // If enum already has 'confirmed', MySQL may not error — safe to ignore
    // Only rethrow if it's actually a different kind of error
    const msg = (error.original?.message || error.message || "").toLowerCase();
    if (msg.includes("confirmed")) return; // already has the value
    throw error;
  }
};

const ensureVoucherRecipientsTable = async () => {
  await db.query(`
    CREATE TABLE IF NOT EXISTS \`VoucherRecipients\` (
      \`id\` CHAR(36) NOT NULL,
      \`source\` VARCHAR(255) NULL,
      \`createdAt\` DATETIME NOT NULL,
      \`updatedAt\` DATETIME NOT NULL,
      \`voucherId\` CHAR(36) NOT NULL,
      \`userId\` CHAR(36) NOT NULL,
      PRIMARY KEY (\`id\`),
      UNIQUE KEY \`voucher_recipients_unique\` (\`voucherId\`, \`userId\`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  `);
};

// Middleware
app.use(
  cors({
    origin: (origin, callback) => {
      // Allow non-browser requests (e.g., server-to-server, curl)
      if (!origin) {
        return callback(null, true);
      }

      if (allowedOrigins.includes(origin)) {
        return callback(null, true);
      }

      return callback(new Error(`CORS blocked origin: ${origin}`));
    },
    credentials: true,
  }),
);

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Session middleware for OAuth
app.use(
  require("express-session")({
    secret: process.env.JWT_SECRET || "your_jwt_secret_key_here",
    resave: false,
    saveUninitialized: true,
    cookie: {
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
    },
  }),
);

app.use(passport.initialize());
app.use(passport.session());

// OAuth Routes
app.get(
  "/auth/google",
  passport.authenticate("google", { scope: ["profile", "email"] }),
);

app.get(
  "/auth/google/callback",
  passport.authenticate("google", {
    failureRedirect: `${process.env.FRONTEND_URL}/auth/login?error=google_failed`,
  }),
  (req, res) => {
    res.redirect(
      `${process.env.FRONTEND_URL}/auth/auth-callback?token=${req.user.token}&provider=google`,
    );
  },
);

app.get(
  "/auth/github",
  passport.authenticate("github", { scope: ["user:email"] }),
);

app.get(
  "/auth/github/callback",
  passport.authenticate("github", {
    failureRedirect: `${process.env.FRONTEND_URL}/auth/login?error=github_failed`,
  }),
  (req, res) => {
    res.redirect(
      `${process.env.FRONTEND_URL}/auth/auth-callback?token=${req.user.token}&provider=github`,
    );
  },
);

// -------- API THANH TOÁN MOMO ĐƯỢC GẮN VÀO ĐÂY --------
app.use("/api/momo", require("./routes/momo"));
// --------------------------------------------------------

// Apollo Server Setup
const startServer = async () => {
  const server = new ApolloServer({
    typeDefs,
    resolvers,
    context: async ({ req }) => {
      const token = req.headers.authorization?.split("Bearer ")[1];
      const user = authMiddleware.verifyToken(token);
      return { user, db };
    },
  });

  await server.start();

  app.use(
    "/graphql",
    expressMiddleware(server, {
      context: async ({ req }) => {
        const token = req.headers.authorization?.split("Bearer ")[1];
        const user = authMiddleware.verifyToken(token);
        return { user, db };
      },
    }),
  );

  // Health check
  app.get("/health", (req, res) => {
    res.json({ status: "OK", timestamp: new Date() });
  });

  // ---- MB Bank Payment SSE ----
  // Frontend calls this to start watching a payment
  app.post("/payment/watch", (req, res) => {
    const { orderId, amount } = req.body;
    if (!orderId || !amount)
      return res.status(400).json({ error: "Missing orderId or amount" });
    mbService.watchPayment(orderId, Number(amount));
    res.json({ ok: true });
  });

  // SSE stream: frontend connects here after placing order
  app.get("/payment/sse/:orderId", (req, res) => {
    const { orderId } = req.params;
    res.setHeader("Content-Type", "text/event-stream");
    res.setHeader("Cache-Control", "no-cache");
    res.setHeader("Connection", "keep-alive");
    res.setHeader(
      "Access-Control-Allow-Origin",
      process.env.FRONTEND_URL || "http://localhost:8080",
    );
    res.flushHeaders();
    // Send heartbeat every 20s to keep connection alive
    const heartbeat = setInterval(() => {
      try {
        res.write(": heartbeat\n\n");
      } catch (_) {}
    }, 20000);
    mbService.addSSEClient(orderId, res);
    req.on("close", () => {
      clearInterval(heartbeat);
      mbService.removeSSEClient(orderId, res);
    });
  });

  const PORT = process.env.SERVER_PORT || 4000;
  app.listen(PORT, () => {
    console.log(`🚀 Server running at http://localhost:${PORT}/graphql`);
  });
};

// Database Connection
db.authenticate()
  .then(() => {
    console.log("✅ Database connected successfully");
    return db.sync();
  })
  .then(() => {
    return ensureReviewImageColumn();
  })
  .then(() => {
    return ensureOrderVoucherColumns();
  })
  .then(() => {
    return ensureVoucherGiftColumns();
  })
  .then(() => {
    return ensureVoucherRecipientsTable();
  })
  .then(() => {
    return ensureOrderStatusConfirmed();
  })
  .then(() => {
    startServer();
  })
  .catch((err) => {
    console.error("❌ Database connection failed:", err);
    process.exit(1);
  });

module.exports = app;
