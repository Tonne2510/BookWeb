package com.bookweb.util;

import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TokenUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenUtil.class);
    private static final String TOKEN_COOKIE_NAME = "auth_token";
    private static final String ROLE_COOKIE_NAME = "user_role";

    public static String getTokenFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            logger.warn("RequestContextHolder attributes are NULL!");
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        
        // 1. Try Authorization header first
        String header = request.getHeader("Authorization");
        logger.debug("Authorization header: {}", header);
        if (header != null && header.startsWith("Bearer ")) {
            logger.debug("Found token in Authorization header");
            return header.substring(7);
        }

        // 2. Try session attribute
        String sessionToken = request.getSession().getAttribute("token") != null ?
                (String) request.getSession().getAttribute("token") : null;
        if (sessionToken != null) {
            logger.debug("Found token in session");
            return sessionToken;
        }
        
        // 3. Try cookies (most reliable for persistence)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    logger.debug("Found token in cookie");
                    return cookie.getValue();
                }
            }
        }
        
        logger.debug("No token found in header, session, or cookies");
        return null;
    }

    public static String getUserRoleFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            logger.warn("Cannot get user role - RequestContextHolder is NULL!");
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        
        // 1. Try session first
        String sessionRole = request.getSession().getAttribute("userRole") != null ?
                (String) request.getSession().getAttribute("userRole") : null;
        if (sessionRole != null) {
            logger.debug("Found role in session: {}", sessionRole);
            return sessionRole;
        }
        
        // 2. Try cookies
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (ROLE_COOKIE_NAME.equals(cookie.getName())) {
                    logger.debug("Found role in cookie: {}", cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        
        logger.debug("No role found in session or cookies");
        return null;
    }

    public static void setTokenToSession(String token) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            request.getSession().setAttribute("token", token);
            logger.info("Token set in session (tokenLength: {})", token.length());
        } else {
            logger.error("Cannot set token - RequestContextHolder attributes are NULL!");
        }
    }

    public static void setTokenToSession(String token, HttpServletRequest request) {
        if (request != null && token != null) {
            request.getSession().setAttribute("token", token);
            logger.info("Token set in session via request (tokenLength: {})", token.length());
        } else {
            logger.error("Cannot set token - request is null or token is null");
        }
    }

    public static void setTokenToCookie(String token, HttpServletResponse response) {
        if (token != null && response != null) {
            Cookie cookie = new Cookie(TOKEN_COOKIE_NAME, token);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Set to true in production with HTTPS
            cookie.setMaxAge(30 * 60 * 60); // 30 hours
            response.addCookie(cookie);
            logger.info("Token set in cookie (tokenLength: {})", token.length());
        } else {
            logger.error("Cannot set token cookie - response is null or token is null");
        }
    }

    public static void setRoleToCookie(String role, HttpServletResponse response) {
        if (role != null && response != null) {
            Cookie cookie = new Cookie(ROLE_COOKIE_NAME, role);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Set to true in production with HTTPS
            cookie.setMaxAge(30 * 60 * 60); // 30 hours
            response.addCookie(cookie);
            logger.info("Role set in cookie: {}", role);
        } else {
            logger.error("Cannot set role cookie - response is null or role is null");
        }
    }

    public static void clearTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(TOKEN_COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        logger.info("Token cookie cleared");
    }

    public static void clearRoleCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(ROLE_COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        logger.info("Role cookie cleared");
    }
}
