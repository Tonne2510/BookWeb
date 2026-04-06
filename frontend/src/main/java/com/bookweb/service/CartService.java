package com.bookweb.service;

import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CartService {

    private static final String CART_SESSION_KEY = "shopping_cart";

    // Persistent cart storage: userId -> cart map
    private static final Map<String, Map<String, Object>> userCarts = new ConcurrentHashMap<>();

    /**
     * Get shopping cart from session
     */
    public Map<String, Object> getCart(HttpSession session) {
        Map<String, Object> cart = (Map<String, Object>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new HashMap<>();
            cart.put("items", new ArrayList<>());
            cart.put("total", 0.0);
            cart.put("count", 0);
        }
        return cart;
    }

    /**
     * Add item to cart
     */
    public void addToCart(HttpSession session, String bookId, int quantity, double price, String title, String coverImage) {
        Map<String, Object> cart = getCart(session);
        List<Map<String, Object>> items = (List<Map<String, Object>>) cart.get("items");

        // Check if item already exists
        boolean found = false;
        for (Map<String, Object> item : items) {
            if (item.get("bookId").equals(bookId)) {
                int currentQty = (int) item.get("quantity");
                item.put("quantity", currentQty + quantity);
                found = true;
                break;
            }
        }

        if (!found) {
            Map<String, Object> newItem = new HashMap<>();
            newItem.put("bookId", bookId);
            newItem.put("quantity", quantity);
            newItem.put("price", price);
            if (title != null) newItem.put("title", title);
            if (coverImage != null) newItem.put("coverImage", coverImage);
            items.add(newItem);
        }

        updateCartTotals(cart);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    /**
     * Remove item from cart
     */
    public void removeFromCart(HttpSession session, String bookId) {
        Map<String, Object> cart = getCart(session);
        List<Map<String, Object>> items = (List<Map<String, Object>>) cart.get("items");
        items.removeIf(item -> item.get("bookId").equals(bookId));
        updateCartTotals(cart);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    /**
     * Update item quantity
     */
    public void updateQuantity(HttpSession session, String bookId, int quantity) {
        if (quantity <= 0) {
            removeFromCart(session, bookId);
            return;
        }

        Map<String, Object> cart = getCart(session);
        List<Map<String, Object>> items = (List<Map<String, Object>>) cart.get("items");
        for (Map<String, Object> item : items) {
            if (item.get("bookId").equals(bookId)) {
                item.put("quantity", quantity);
                break;
            }
        }
        updateCartTotals(cart);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    /**
     * Clear entire cart
     */
    public void clearCart(HttpSession session) {
        Map<String, Object> cart = new HashMap<>();
        cart.put("items", new ArrayList<>());
        cart.put("total", 0.0);
        cart.put("count", 0);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    /**
     * Update cart totals (price and count)
     */
    private void updateCartTotals(Map<String, Object> cart) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) cart.get("items");
        double total = 0;
        int count = 0;

        for (Map<String, Object> item : items) {
            Integer qty = (Integer) item.get("quantity");
            if (qty != null) count += qty;
            
            if (item.containsKey("price")) {
                Double price = ((Number) item.get("price")).doubleValue();
                if (qty != null && qty > 0) {
                    total += price * qty;
                }
            }
        }

        cart.put("total", total);
        cart.put("count", count);
    }

    /**
     * Get cart with details
     */
    public Map<String, Object> getCartWithDetails(HttpSession session, String token) {
        return getCart(session);
    }

    /**
     * Save current session cart for a user (called on logout)
     */
    public void saveCartForUser(String userId, HttpSession session) {
        if (userId == null || userId.isEmpty()) return;
        Map<String, Object> cart = getCart(session);
        // Deep copy to avoid session reference issues
        Map<String, Object> saved = new HashMap<>(cart);
        List<Map<String, Object>> originalItems = (List<Map<String, Object>>) cart.get("items");
        List<Map<String, Object>> copiedItems = new ArrayList<>();
        for (Map<String, Object> item : originalItems) {
            copiedItems.add(new HashMap<>(item));
        }
        saved.put("items", copiedItems);
        userCarts.put(userId, saved);
    }

    /**
     * Load saved cart for a user into session (called on login)
     */
    public void loadCartForUser(String userId, HttpSession session) {
        if (userId == null || userId.isEmpty()) return;
        Map<String, Object> saved = userCarts.get(userId);
        if (saved == null) return;
        // Merge saved cart with current session cart
        Map<String, Object> sessionCart = getCart(session);
        List<Map<String, Object>> sessionItems = (List<Map<String, Object>>) sessionCart.get("items");
        List<Map<String, Object>> savedItems = (List<Map<String, Object>>) saved.get("items");
        for (Map<String, Object> savedItem : savedItems) {
            String savedBookId = (String) savedItem.get("bookId");
            boolean found = false;
            for (Map<String, Object> sessionItem : sessionItems) {
                if (sessionItem.get("bookId").equals(savedBookId)) {
                    // Keep session quantity (user may have added more before login)
                    found = true;
                    break;
                }
            }
            if (!found) {
                sessionItems.add(new HashMap<>(savedItem));
            }
        }
        updateCartTotals(sessionCart);
        session.setAttribute(CART_SESSION_KEY, sessionCart);
    }

    /**
     * Add to cart and persist for user if logged in
     */
    public void addToCart(HttpSession session, String bookId, int quantity, double price, String title, String coverImage, String userId) {
        addToCart(session, bookId, quantity, price, title, coverImage);
        if (userId != null && !userId.isEmpty()) {
            saveCartForUser(userId, session);
        }
    }
}

