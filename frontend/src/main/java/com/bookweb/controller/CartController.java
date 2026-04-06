package com.bookweb.controller;

import com.bookweb.service.CartService;
import com.bookweb.service.AuthService;
import com.bookweb.service.OrderService;
import com.bookweb.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import java.util.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private AuthService authService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private VoucherService voucherService;

    /**
     * View shopping cart
     */
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Map<String, Object> cart = cartService.getCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cart.get("items"));
        model.addAttribute("cartTotal", cart.get("total"));
        model.addAttribute("cartCount", cart.get("count"));
        return "cart/view";
    }

    /**
     * Add item to cart
     */
    @PostMapping("/add/{bookId}/{quantity}/{price}")
    public ResponseEntity<?> addToCart(
            @PathVariable String bookId,
            @PathVariable int quantity,
            @PathVariable double price,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String coverImage,
            HttpSession session) {
        try {
            // Check authentication
            String token = com.bookweb.util.TokenUtil.getTokenFromRequest();
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "requiresLogin", true,
                        "message", "Vui lòng đăng nhập để thêm vào giỏ hàng"
                ));
            }
            // Get userId to persist cart
            String userId = null;
            try {
                var user = authService.getMe(token);
                if (user != null) userId = user.getId();
            } catch (Exception ignored) {}

            cartService.addToCart(session, bookId, quantity, price, title, coverImage, userId);
            Map<String, Object> cart = cartService.getCart(session);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Added to cart successfully",
                    "cartCount", cart.get("count")
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Remove item from cart
     */
    @PostMapping("/remove/{bookId}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable String bookId,
            HttpSession session) {
        try {
            cartService.removeFromCart(session, bookId);
            Map<String, Object> cart = cartService.getCart(session);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Removed from cart",
                    "cartCount", cart.get("count")
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false));
        }
    }

    /**
     * Update quantity
     */
    @PostMapping("/update/{bookId}/{quantity}")
    public ResponseEntity<?> updateQuantity(
            @PathVariable String bookId,
            @PathVariable int quantity,
            HttpSession session) {
        try {
            cartService.updateQuantity(session, bookId, quantity);
            Map<String, Object> cart = cartService.getCart(session);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "cartCount", cart.get("count"),
                    "cartTotal", cart.get("total")
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false));
        }
    }

    /**
     * Get cart count (for AJAX)
     */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<?> getCartCount(HttpSession session) {
        Map<String, Object> cart = cartService.getCart(session);
        return ResponseEntity.ok(Map.of("count", cart.get("count")));
    }

    /**
     * Checkout page
     */
    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        String token = com.bookweb.util.TokenUtil.getTokenFromRequest();
        if (token == null) {
            return "redirect:/auth/login";
        }

        Map<String, Object> cart = cartService.getCart(session);
        List<?> items = (List<?>) cart.get("items");
        if (items == null || items.isEmpty()) {
            return "redirect:/cart";
        }

        // Auto-fill user info from profile
        try {
            var user = authService.getMe(token);
            model.addAttribute("user", user);
        } catch (Exception e) {
            // Ignore - user can still fill manually
        }

        model.addAttribute("cart", cart);
        model.addAttribute("cartTotal", cart.get("total"));
        model.addAttribute("appliedVoucherCode", "");
        return "cart/checkout-standalone";
    }

    @PostMapping("/validate-voucher")
    @ResponseBody
    public ResponseEntity<?> validateVoucher(
            @RequestParam String code,
            @RequestParam Double subtotal) {
        try {
            String token = com.bookweb.util.TokenUtil.getTokenFromRequest();
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "valid", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }
            Map<String, Object> result = voucherService.validateVoucher(code, subtotal, token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Process checkout
     */
    @PostMapping("/checkout")
    public String processCheckout(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String city,
            @RequestParam String district,
            @RequestParam String shippingAddress,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String voucherCode,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            String token = com.bookweb.util.TokenUtil.getTokenFromRequest();
            if (token == null) {
                return "redirect:/auth/login";
            }

            Map<String, Object> cart = cartService.getCart(session);
            List<?> items = (List<?>) cart.get("items");
            if (items.isEmpty()) {
                return "redirect:/cart";
            }

            String fullShippingAddress = district + ", " + city + ", " + shippingAddress;
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("fullName", fullName);
            orderData.put("email", email);
            orderData.put("phone", phone);
            orderData.put("shippingAddress", fullShippingAddress);
            orderData.put("paymentMethod", paymentMethod);
            orderData.put("items", items);

            double subtotal = ((Number) cart.get("total")).doubleValue();
            double voucherDiscount = 0;
            double finalTotal = subtotal;
            if (voucherCode != null && !voucherCode.isBlank()) {
                Map<String, Object> voucherValidation = voucherService.validateVoucher(voucherCode, subtotal, token);
                boolean valid = (Boolean) voucherValidation.get("valid");
                if (!valid) {
                    redirectAttributes.addFlashAttribute("error", voucherValidation.get("message"));
                    return "redirect:/cart/checkout";
                }
                voucherDiscount = ((Number) voucherValidation.get("discountAmount")).doubleValue();
                finalTotal = ((Number) voucherValidation.get("finalAmount")).doubleValue();
                orderData.put("voucherCode", voucherCode.trim().toUpperCase());
            }
            orderData.put("total", finalTotal);

            if ("vietqr".equals(paymentMethod)) {
                // VietQR: DON'T create order yet — store data in session, create after payment confirmed
                String tempOrderId = java.util.UUID.randomUUID().toString();
                Map<String, Object> pendingOrder = new HashMap<>(orderData);
                pendingOrder.put("orderId", tempOrderId);
                pendingOrder.put("token", token);
                session.setAttribute("pendingVietqrOrder", pendingOrder);

                Map<String, Object> lastOrder = new HashMap<>();
                lastOrder.put("orderId", tempOrderId);
                lastOrder.put("fullName", fullName);
                lastOrder.put("email", email);
                lastOrder.put("phone", phone);
                lastOrder.put("shippingAddress", fullShippingAddress);
                lastOrder.put("paymentMethod", paymentMethod);
                lastOrder.put("total", finalTotal);
                lastOrder.put("voucherDiscount", voucherDiscount);
                lastOrder.put("voucherCode", voucherCode != null ? voucherCode.trim().toUpperCase() : null);
                session.setAttribute("lastOrder", lastOrder);
                return "redirect:/cart/payment";
            }

            // Non-VietQR: create order immediately
            String orderId = orderService.createOrder(orderData, token);
            cartService.clearCart(session);

            Map<String, Object> lastOrder = new HashMap<>();
            lastOrder.put("orderId", orderId);
            lastOrder.put("fullName", fullName);
            lastOrder.put("email", email);
            lastOrder.put("phone", phone);
            lastOrder.put("shippingAddress", fullShippingAddress);
            lastOrder.put("paymentMethod", paymentMethod);
            lastOrder.put("total", finalTotal);
            lastOrder.put("voucherDiscount", voucherDiscount);
            lastOrder.put("voucherCode", voucherCode != null ? voucherCode.trim().toUpperCase() : null);
            session.setAttribute("lastOrder", lastOrder);
            return "redirect:/cart/success";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi xử lý đơn hàng: " + e.getMessage());
            return "redirect:/cart/checkout";
        }
    }

    /**
     * VietQR payment waiting page
     */
    @GetMapping("/payment")
    public String paymentPage(HttpSession session, Model model) {
        Map<?, ?> order = (Map<?, ?>) session.getAttribute("lastOrder");
        if (order == null) return "redirect:/cart";
        order.forEach((k, v) -> model.addAttribute(k.toString(), v));
        return "cart/payment-standalone";
    }

    /**
     * Confirm VietQR payment — creates order in DB after payment detected
     */
    @PostMapping("/confirm-payment")
    @ResponseBody
    public ResponseEntity<?> confirmPayment(HttpSession session) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> pending = (Map<String, Object>) session.getAttribute("pendingVietqrOrder");
            if (pending == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "No pending order"));
            }

            String token = (String) pending.get("token");
            Map<String, Object> orderData = new HashMap<>(pending);
            orderData.remove("orderId");
            orderData.remove("token");

            String realOrderId = orderService.createOrder(orderData, token);

            // Update lastOrder with real orderId
            @SuppressWarnings("unchecked")
            Map<String, Object> lastOrder = (Map<String, Object>) session.getAttribute("lastOrder");
            if (lastOrder != null) {
                lastOrder.put("orderId", realOrderId);
                session.setAttribute("lastOrder", lastOrder);
            }

            // Clear pending order and cart
            session.removeAttribute("pendingVietqrOrder");
            cartService.clearCart(session);

            return ResponseEntity.ok(Map.of("success", true, "orderId", realOrderId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Order success page
     */
    @GetMapping("/success")
    public String successPage(HttpSession session, Model model) {
        Map<?, ?> order = (Map<?, ?>) session.getAttribute("lastOrder");
        if (order == null) return "redirect:/";
        order.forEach((k, v) -> model.addAttribute(k.toString(), v));
        session.removeAttribute("lastOrder");
        return "cart/success-standalone";
    }

    /**
     * Clear cart
     */
    @PostMapping("/clear")
    public ResponseEntity<?> clearCart(HttpSession session) {
        cartService.clearCart(session);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
