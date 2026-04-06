const passport = require('passport');
const GoogleStrategy = require('passport-google-oauth20').Strategy;
const GitHubStrategy = require('passport-github2').Strategy;
const { User } = require('../models');
const authService = require('../utils/authService');

// Google OAuth - Only initialize if credentials are provided
if (process.env.GOOGLE_CLIENT_ID && process.env.GOOGLE_CLIENT_SECRET) {
  const googleCallbackUrl = `${process.env.FRONTEND_URL || 'http://localhost:8080'}/auth/google/callback`;
  passport.use(new GoogleStrategy({
    clientID: process.env.GOOGLE_CLIENT_ID,
    clientSecret: process.env.GOOGLE_CLIENT_SECRET,
    callbackURL: googleCallbackUrl
  }, async (accessToken, refreshToken, profile, done) => {
    try {
      let user = await User.findOne({ where: { googleId: profile.id } });

      if (!user) {
        user = await User.create({
          email: profile.emails[0].value,
          firstName: profile.name.givenName || '',
          lastName: profile.name.familyName || '',
          googleId: profile.id,
          avatar: profile.photos[0]?.value,
          status: 'active'
        });
      }

      const token = authService.generateToken(user.id);
      user.token = token;
      return done(null, user);
    } catch (err) {
      return done(err);
    }
  }));
}

// GitHub OAuth - Only initialize if credentials are provided
if (process.env.GITHUB_CLIENT_ID && process.env.GITHUB_CLIENT_SECRET) {
  const githubCallbackUrl = `${process.env.FRONTEND_URL || 'http://localhost:8080'}/auth/github/callback`;
  passport.use(new GitHubStrategy({
    clientID: process.env.GITHUB_CLIENT_ID,
    clientSecret: process.env.GITHUB_CLIENT_SECRET,
    callbackURL: githubCallbackUrl,
    scope: ['user:email']
  }, async (accessToken, refreshToken, profile, done) => {
    try {
      let user = await User.findOne({ where: { githubId: profile.id } });

      if (!user) {
        // GitHub may not expose email if it's private — use fallback
        const email = (profile.emails && profile.emails.length > 0)
          ? profile.emails[0].value
          : `github_${profile.id}@github.com`;

        const displayName = profile.displayName || profile.username || 'GitHub User';
        const nameParts = displayName.split(' ');
        const firstName = nameParts[0] || 'GitHub';
        const lastName = nameParts.slice(1).join(' ') || 'User';

        user = await User.create({
          email,
          firstName,
          lastName,
          githubId: profile.id,
          avatar: profile.photos && profile.photos[0] ? profile.photos[0].value : null,
          status: 'active'
        });
      }

      const token = authService.generateToken(user.id);
      user.token = token;
      return done(null, user);
    } catch (err) {
      return done(err);
    }
  }));
}

passport.serializeUser((user, done) => {
  done(null, user.id);
});

passport.deserializeUser(async (id, done) => {
  try {
    const user = await User.findByPk(id, {
      attributes: { exclude: ['password'] }
    });
    done(null, user);
  } catch (err) {
    done(err);
  }
});

module.exports = passport;
