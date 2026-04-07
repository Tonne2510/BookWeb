package com.bookweb.controller;

import com.bookweb.service.CartService;
import com.bookweb.service.AuthService;
import com.bookweb.service.OrderService;
import com.bookweb.service.VoucherService;
import com.bookweb.model.VoucherDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.env.Environment;
import java.util.*;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/cart")
public class CartController {

    private static final String APPLIED_VOUCHER_CODE_SESSION_KEY = "appliedVoucherCode";

    @Autowired
    private CartService cartService;

    @Autowired
    private AuthService authService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment environment;

    /**
     * View shopping cart
     */
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Map<String, Object> cart = cartService.getCart(session);
        double subtotal = extractCartSubtotal(cart);

        String token = com.bookweb.util.TokenUtil.getTokenFromRequest();
        List<VoucherDTO> giftVouchers = new ArrayList<>();
        List<VoucherDTO> publicCodeVouchers = new ArrayList<>();
        if (token != null) {
            try {
                giftVouchers = voucherService.getMyVouchers(subtotal, token);
                publicCodeVouchers = voucherService.getPublicCodeVouchers(subtotal, token);
            } catch (Exception ignored) {
            }
        }

        Map<String, Object> appliedVoucherValidation = loadAppliedVoucherValidation(session, token, subtotal);
        double cartFinalTotal = subtotal;
        if (appliedVoucherValidation != null) {
            cartFinalTotal = ((Number) appliedVoucherValidation.get("finalAmount")).doubleValue();
        }

        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cart.get("items"));
        model.addAttribute("cartTotal", cart.get("total"));
        model.addAttribute("cartCount", cart.get("count"));
        model.addAttribute("giftVouchers", giftVouchers);
        model.addAttribute("publicCodeVouchers", publicCodeVouchers);
        model.addAttribute("appliedVoucherCode", session.getAttribute(APPLIED_VOUCHER_CODE_SESSION_KEY));
        model.addAttribute("appliedVoucherValidation", appliedVoucherValidation);
        model.addAttribute("cartFinalTotal", cartFinalTotal);
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
        double subtotal = extractCartSubtotal(cart);
        Map<String, Object> appliedVoucherValidation = loadAppliedVoucherValidation(session, token, subtotal);

        // Auto-fill user info from profile
        try {
            var user = authService.getMe(token);
            model.addAttribute("user", user);
        } catch (Exception e) {
            // Ignore - user can still fill manually
        }

        model.addAttribute("cart", cart);
        model.addAttribute("cartTotal", cart.get("total"));
        model.addAttribute("appliedVoucherCode", session.getAttribute(APPLIED_VOUCHER_CODE_SESSION_KEY));
        model.addAttribute("appliedVoucherValidation", appliedVoucherValidation);
        model.addAttribute("cartFinalTotal", appliedVoucherValidation != null
            ? ((Number) appliedVoucherValidation.get("finalAmount")).doubleValue()
            : subtotal);
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

    @PostMapping("/apply-voucher")
    @ResponseBody
    public ResponseEntity<?> applyVoucher(@RequestParam String code, HttpSession session) {
        try {
            String token = com.bookweb.util.TokenUtil.getTokenFromRequest();
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "valid", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            Map<String, Object> cart = cartService.getCart(session);
            double subtotal = extractCartSubtotal(cart);
            Map<String, Object> result = voucherService.validateVoucher(code, subtotal, token);
            boolean valid = Boolean.TRUE.equals(result.get("valid"));
            if (!valid) {
                clearAppliedVoucherSession(session);
                return ResponseEntity.badRequest().body(result);
            }

            String normalizedCode = code.trim().toUpperCase();
            session.setAttribute(APPLIED_VOUCHER_CODE_SESSION_KEY, normalizedCode);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            clearAppliedVoucherSession(session);
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/remove-voucher")
    @ResponseBody
    public ResponseEntity<?> removeVoucher(HttpSession session) {
        clearAppliedVoucherSession(session);
        return ResponseEntity.ok(Map.of("success", true));
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
            String normalizedPaymentMethod = "momo".equals(paymentMethod) ? "bank_transfer" : paymentMethod;
            orderData.put("paymentMethod", normalizedPaymentMethod);
            orderData.put("items", items);

            double subtotal = ((Number) cart.get("total")).doubleValue();
            double voucherDiscount = 0;
            double finalTotal = subtotal;
            Map<String, Object> voucherValidation = loadAppliedVoucherValidation(session, token, subtotal);
            if (voucherValidation != null) {
                voucherDiscount = ((Number) voucherValidation.get("discountAmount")).doubleValue();
                finalTotal = ((Number) voucherValidation.get("finalAmount")).doubleValue();
                String appliedCode = (String) session.getAttribute(APPLIED_VOUCHER_CODE_SESSION_KEY);
                if (appliedCode != null && !appliedCode.isBlank()) {
                    orderData.put("voucherCode", appliedCode);
                }
            }
            orderData.put("total", finalTotal);

            if ("momo".equals(paymentMethod)) {
                // MoMo: create pending order first, real order is created after callback success.
                String tempOrderId = java.util.UUID.randomUUID().toString();
                Map<String, Object> pendingOrder = new HashMap<>(orderData);
                pendingOrder.put("orderId", tempOrderId);
                pendingOrder.put("token", token);
                session.setAttribute("pendingMomoOrder", pendingOrder);

                Map<String, Object> lastOrder = new HashMap<>();
                lastOrder.put("orderId", tempOrderId);
                lastOrder.put("fullName", fullName);
                lastOrder.put("email", email);
                lastOrder.put("phone", phone);
                lastOrder.put("shippingAddress", fullShippingAddress);
                lastOrder.put("paymentMethod", paymentMethod);
                lastOrder.put("total", finalTotal);
                lastOrder.put("voucherDiscount", voucherDiscount);
                lastOrder.put("voucherCode", session.getAttribute(APPLIED_VOUCHER_CODE_SESSION_KEY));
                session.setAttribute("lastOrder", lastOrder);

                return "redirect:/cart/momo-payment";
            }

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
                lastOrder.put("voucherCode", session.getAttribute(APPLIED_VOUCHER_CODE_SESSION_KEY));
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
            lastOrder.put("voucherCode", session.getAttribute(APPLIED_VOUCHER_CODE_SESSION_KEY));
            session.setAttribute("lastOrder", lastOrder);

            clearAppliedVoucherSession(session);
            return "redirect:/cart/success";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi xử lý đơn hàng: " + e.getMessage());
            return "redirect:/cart/checkout";
        }
    }

    /**
     * Intermediate page to start MoMo payment from pending session order.
     */
    @GetMapping("/momo-payment")
    public String momoPaymentPage(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        Map<String, Object> pending = (Map<String, Object>) session.getAttribute("pendingMomoOrder");
        if (pending == null) {
            return "redirect:/cart/checkout";
        }

        model.addAttribute("orderId", pending.get("orderId"));
        model.addAttribute("amount", pending.get("total"));
        return "cart/momo-payment-standalone";
    }

    /**
     * MoMo callback endpoint. resultCode=0 means payment success.
     */
    @GetMapping("/momo-return")
    public String momoReturn(
            @RequestParam(required = false) String resultCode,
            @RequestParam(required = false) String message,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            if (!"0".equals(resultCode)) {
                String userMessage = (message != null && !message.isBlank())
                        ? message
                        : "Thanh toán MoMo chưa thành công.";
                redirectAttributes.addFlashAttribute("error", userMessage);
                return "redirect:/cart/checkout";
            }

            String realOrderId = finalizePendingOrder(session, "pendingMomoOrder");
            return "redirect:/cart/success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xác nhận thanh toán MoMo: " + e.getMessage());
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
            String realOrderId = finalizePendingOrder(session, "pendingVietqrOrder");

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
        clearAppliedVoucherSession(session);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private double extractCartSubtotal(Map<String, Object> cart) {
        if (cart == null || cart.get("total") == null) {
            return 0;
        }
        return ((Number) cart.get("total")).doubleValue();
    }

    private void clearAppliedVoucherSession(HttpSession session) {
        session.removeAttribute(APPLIED_VOUCHER_CODE_SESSION_KEY);
    }

    private Map<String, Object> loadAppliedVoucherValidation(HttpSession session, String token, double subtotal) {
        Object appliedCodeObj = session.getAttribute(APPLIED_VOUCHER_CODE_SESSION_KEY);
        if (!(appliedCodeObj instanceof String appliedCode) || appliedCode.isBlank() || token == null) {
            return null;
        }

        try {
            Map<String, Object> validation = voucherService.validateVoucher(appliedCode, subtotal, token);
            boolean valid = Boolean.TRUE.equals(validation.get("valid"));
            if (!valid) {
                clearAppliedVoucherSession(session);
                return null;
            }
            return validation;
        } catch (Exception ex) {
            clearAppliedVoucherSession(session);
            return null;
        }
    }

    private String finalizePendingOrder(HttpSession session, String pendingSessionKey) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> pending = (Map<String, Object>) session.getAttribute(pendingSessionKey);
        if (pending == null) {
            throw new Exception("No pending order");
        }

        String token = (String) pending.get("token");
        Map<String, Object> orderData = new HashMap<>(pending);
        orderData.remove("orderId");
        orderData.remove("token");

        String realOrderId = orderService.createOrder(orderData, token);

        @SuppressWarnings("unchecked")
        Map<String, Object> lastOrder = (Map<String, Object>) session.getAttribute("lastOrder");
        if (lastOrder != null) {
            lastOrder.put("orderId", realOrderId);
            session.setAttribute("lastOrder", lastOrder);
        }

        session.removeAttribute(pendingSessionKey);
        cartService.clearCart(session);
        clearAppliedVoucherSession(session);
        return realOrderId;
    }

    /**
     * Proxy MoMo payment creation request to backend Node.js
     */
    @PostMapping("/api/momo/create-payment")
    @ResponseBody
    public ResponseEntity<?> createMoMoPaymentProxy(@RequestBody Map<String, Object> request) {
        try {
            String backendUrl = environment.getProperty("backend.base-url");
            if (backendUrl == null) {
                backendUrl = "http://localhost:4000";
            }
            
            String momoUrl = backendUrl + "/api/momo/create-payment";
            Map<String, Object> response = restTemplate.postForObject(
                    momoUrl,
                    request,
                    Map.class
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi tạo link MoMo: " + e.getMessage()
            ));
        }
    }
}
