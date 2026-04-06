const express = require('express');
const { ApolloServer } = require('@apollo/server');
const { expressMiddleware } = require('@apollo/server/express4');
const cors = require('cors');
const dotenv = require('dotenv');
const db = require('./db/connection');
const typeDefs = require('./schema/typeDefs');
const resolvers = require('./schema/resolvers');
const authMiddleware = require('./middleware/auth');
const passport = require('passport');
const mbService = require('./utils/mbService');
require('./config/passport');

dotenv.config();

const app = express();
app.set('trust proxy', 1);

const ensureReviewImageColumn = async () => {
  try {
    await db.query('ALTER TABLE `Reviews` ADD COLUMN `imageUrl` VARCHAR(500) NULL');
    console.log('✅ Added Reviews.imageUrl column');
  } catch (error) {
    // Ignore duplicate column errors for repeated startups
    if (error && (error.original?.code === 'ER_DUP_FIELDNAME' || error.parent?.code === 'ER_DUP_FIELDNAME')) {
      return;
    }
    throw error;
  }
};

const ensureOrderVoucherColumns = async () => {
  try {
    await db.query('ALTER TABLE `Orders` ADD COLUMN `voucherCode` VARCHAR(255) NULL');
    console.log('✅ Added Orders.voucherCode column');
  } catch (error) {
    if (!(error && (error.original?.code === 'ER_DUP_FIELDNAME' || error.parent?.code === 'ER_DUP_FIELDNAME'))) {
      throw error;
    }
  }

  try {
    await db.query('ALTER TABLE `Orders` ADD COLUMN `voucherDiscount` DECIMAL(10,2) NOT NULL DEFAULT 0');
    console.log('✅ Added Orders.voucherDiscount column');
  } catch (error) {
    if (!(error && (error.original?.code === 'ER_DUP_FIELDNAME' || error.parent?.code === 'ER_DUP_FIELDNAME'))) {
      throw error;
    }
  }
};

// Middleware
app.use(cors({
  origin: process.env.FRONTEND_URL || 'http://localhost:8080',
  credentials: true
}));

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Session middleware for OAuth
app.use(require('express-session')({
  secret: process.env.JWT_SECRET || 'your_jwt_secret_key_here',
  resave: false,
  saveUninitialized: true,
  cookie: {
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax'
  }
}));

app.use(passport.initialize());
app.use(passport.session());

// OAuth Routes
app.get('/auth/google',
  passport.authenticate('google', { scope: ['profile', 'email'] })
);

app.get('/auth/google/callback',
  passport.authenticate('google', { failureRedirect: `${process.env.FRONTEND_URL}/auth/login?error=google_failed` }),
  (req, res) => {
    res.redirect(`${process.env.FRONTEND_URL}/auth/auth-callback?token=${req.user.token}&provider=google`);
  }
);

app.get('/auth/github',
  passport.authenticate('github', { scope: ['user:email'] })
);

app.get('/auth/github/callback',
  passport.authenticate('github', { failureRedirect: `${process.env.FRONTEND_URL}/auth/login?error=github_failed` }),
  (req, res) => {
    res.redirect(`${process.env.FRONTEND_URL}/auth/auth-callback?token=${req.user.token}&provider=github`);
  }
);

// Apollo Server Setup
const startServer = async () => {
  const server = new ApolloServer({
    typeDefs,
    resolvers,
    context: async ({ req }) => {
      const token = req.headers.authorization?.split('Bearer ')[1];
      const user = authMiddleware.verifyToken(token);
      return { user, db };
    }
  });

  await server.start();

  app.use('/graphql', expressMiddleware(server, {
    context: async ({ req }) => {
      const token = req.headers.authorization?.split('Bearer ')[1];
      const user = authMiddleware.verifyToken(token);
      return { user, db };
    }
  }));

  // Health check
  app.get('/health', (req, res) => {
    res.json({ status: 'OK', timestamp: new Date() });
  });

  // ---- MB Bank Payment SSE ----
  // Frontend calls this to start watching a payment
  app.post('/payment/watch', (req, res) => {
    const { orderId, amount } = req.body;
    if (!orderId || !amount) return res.status(400).json({ error: 'Missing orderId or amount' });
    mbService.watchPayment(orderId, Number(amount));
    res.json({ ok: true });
  });

  // SSE stream: frontend connects here after placing order
  app.get('/payment/sse/:orderId', (req, res) => {
    const { orderId } = req.params;
    res.setHeader('Content-Type', 'text/event-stream');
    res.setHeader('Cache-Control', 'no-cache');
    res.setHeader('Connection', 'keep-alive');
    res.setHeader('Access-Control-Allow-Origin', process.env.FRONTEND_URL || 'http://localhost:8080');
    res.flushHeaders();
    // Send heartbeat every 20s to keep connection alive
    const heartbeat = setInterval(() => {
      try { res.write(': heartbeat\n\n'); } catch (_) {}
    }, 20000);
    mbService.addSSEClient(orderId, res);
    req.on('close', () => {
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
    console.log('✅ Database connected successfully');
    return ensureReviewImageColumn();
  })
  .then(() => {
    return ensureOrderVoucherColumns();
  })
  .then(() => {
    return db.sync();
  })
  .then(() => {
    startServer();
  })
  .catch(err => {
    console.error('❌ Database connection failed:', err);
    process.exit(1);
  });

module.exports = app;
