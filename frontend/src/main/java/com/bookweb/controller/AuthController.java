package com.bookweb.controller;

import com.bookweb.model.UserDTO;
import com.bookweb.model.OrderDTO;
import com.bookweb.service.AuthService;
import com.bookweb.service.CartService;
import com.bookweb.service.OrderService;
import com.bookweb.service.VoucherService;
import com.bookweb.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Controller
@RequestMapping("/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private VoucherService voucherService;

    @Value("${backend.base-url:http://localhost:4000}")
    private String backendBaseUrl;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @GetMapping("/google")
    public String googleLogin() {
        return "redirect:" + backendBaseUrl + "/auth/google";
    }

    @GetMapping("/github")
    public String githubLogin() {
        return "redirect:" + backendBaseUrl + "/auth/github";
    }

    @PostMapping("/send-otp")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestParam String email) {
        Map<String, Object> result = new HashMap<>();
        try {
            String message = authService.sendVerificationOtp(email);
            result.put("success", true);
            result.put("message", message);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/register")
    public String register(@RequestParam String email, @RequestParam String password, 
                          @RequestParam String firstName, @RequestParam String lastName,
                          @RequestParam String confirmPassword,
                          @RequestParam String otp,
                          HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu không trùng khớp");
                return "redirect:/auth/register";
            }

            if (password.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự");
                return "redirect:/auth/register";
            }

            var authResponse = authService.register(email, password, firstName, lastName, otp);
            TokenUtil.setTokenToSession(authResponse.getToken());
            
            String userRole = authResponse.getUser().getRole();
            session.setAttribute("userRole", userRole);
            
            redirectAttributes.addFlashAttribute("message", "Đăng ký thành công! Chào mừng bạn!");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                       HttpSession session, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        try {
            logger.info("Login attempt for email: {}", email);
            var authResponse = authService.login(email, password);
            String token = authResponse.getToken();
            String userRole = authResponse.getUser().getRole();
            
            logger.info("Login successful. Setting token and role to session and cookies");
            
            // Set token to session
            if (request != null) {
                TokenUtil.setTokenToSession(token, request);
            } else {
                TokenUtil.setTokenToSession(token);
            }
            
            // Set token to cookie for persistence
            TokenUtil.setTokenToCookie(token, response);
            TokenUtil.setRoleToCookie(userRole, response);
            
            // Store user role in session
            session.setAttribute("userRole", userRole);
            
            // Load persisted cart for this user into session
            try {
                cartService.loadCartForUser(authResponse.getUser().getId(), session);
            } catch (Exception ignored) {}

            logger.info("User role: {}", userRole);
            logger.info("Session token: {}", session.getAttribute("token") != null ? "FOUND" : "NOT FOUND");
            
            // Role-based redirect
            if ("admin".equalsIgnoreCase(userRole)) {
                logger.info("Redirecting to admin dashboard");
                return "redirect:/admin/dashboard";
            }
            return "redirect:/";
        } catch (Exception e) {
            logger.error("Login failed: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/auth-callback")
    public String authCallback(@RequestParam String token,
                               @RequestParam(required = false) String provider,
                               HttpSession session,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               RedirectAttributes redirectAttributes) {
        try {
            TokenUtil.setTokenToSession(token, request);
            TokenUtil.setTokenToCookie(token, response);

            UserDTO user = authService.getMe(token);
            String userRole = user.getRole();
            TokenUtil.setRoleToCookie(userRole, response);
            session.setAttribute("userRole", userRole);

            try {
                cartService.loadCartForUser(user.getId(), session);
            } catch (Exception ignored) {}

            if ("admin".equalsIgnoreCase(userRole)) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đăng nhập thất bại. Vui lòng thử lại.");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/complete-profile")
    public String completeProfilePage(HttpSession session, Model model) {
        String token = TokenUtil.getTokenFromRequest();
        if (token == null) return "redirect:/auth/login";
        try {
            UserDTO user = authService.getMe(token);
            model.addAttribute("user", user);
            return "auth/complete-profile";
        } catch (Exception e) {
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/complete-profile")
    public String completeProfile(@RequestParam String firstName,
                                  @RequestParam String lastName,
                                  @RequestParam(required = false) String phone,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        try {
            String token = TokenUtil.getTokenFromRequest();
            if (token == null) return "redirect:/auth/login";
            authService.updateProfile(firstName, lastName, phone, null, token);
            session.removeAttribute("oauthUser");
            redirectAttributes.addFlashAttribute("message", "Chào mừng bạn đến với BookWeb!");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/auth/complete-profile";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            authService.forgotPassword(email);
            redirectAttributes.addFlashAttribute("message", "Reset link sent to your email");
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token, @RequestParam String password,
                               RedirectAttributes redirectAttributes) {
        try {
            authService.resetPassword(token, password);
            redirectAttributes.addFlashAttribute("message", "Password reset successfully");
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/reset-password?token=" + token;
        }
    }

    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpSession session, HttpServletResponse response) {
        // Save cart before invalidating session
        try {
            String token = TokenUtil.getTokenFromRequest();
            if (token != null) {
                var user = authService.getMe(token);
                if (user != null) {
                    cartService.saveCartForUser(user.getId(), session);
                }
            }
        } catch (Exception ignored) {}
        // Invalidate entire session (clears token, userRole, and shopping cart)
        session.invalidate();
        // Clear cookies
        jakarta.servlet.http.Cookie tokenCookie = new jakarta.servlet.http.Cookie("auth_token", null);
        tokenCookie.setMaxAge(0);
        tokenCookie.setPath("/");
        response.addCookie(tokenCookie);
        jakarta.servlet.http.Cookie roleCookie = new jakarta.servlet.http.Cookie("user_role", null);
        roleCookie.setMaxAge(0);
        roleCookie.setPath("/");
        response.addCookie(roleCookie);
        return "redirect:/";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        try {
            String token = TokenUtil.getTokenFromRequest();
            if (token == null) {
                return "redirect:/auth/login";
            }

            UserDTO user = authService.getMe(token);
            model.addAttribute("user", user);
            return "auth/profile";
        } catch (Exception e) {
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String firstName, @RequestParam String lastName,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String address,
                                RedirectAttributes redirectAttributes) {
        try {
            String token = TokenUtil.getTokenFromRequest();
            if (token == null) {
                return "redirect:/auth/login";
            }

            authService.updateProfile(firstName, lastName, phone, address, token);
            redirectAttributes.addFlashAttribute("message", "Cập nhật thông tin thành công!");
            return "redirect:/auth/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/auth/profile";
        }
    }

    @GetMapping("/orders")
    public String myOrders(Model model) {
        String token = TokenUtil.getTokenFromRequest();
        if (token == null) {
            return "redirect:/auth/login";
        }

        try {
            java.util.List<OrderDTO> orders = orderService.getMyOrders(token);
            model.addAttribute("orders", orders);
            return "auth/orders";
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (message.contains("not authenticated") || message.contains("unauthorized") || message.contains("jwt")) {
                return "redirect:/auth/login";
            }
            model.addAttribute("orders", java.util.Collections.emptyList());
            model.addAttribute("error", "Không thể tải đơn hàng lúc này. Vui lòng thử lại sau.");
            logger.error("Failed to load user orders", e);
            return "auth/orders";
        }
    }

    @GetMapping("/my-vouchers")
    public String myVouchers(Model model) {
        try {
            String token = TokenUtil.getTokenFromRequest();
            if (token == null) {
                return "redirect:/auth/login";
            }

            UserDTO user = authService.getMe(token);
            List<com.bookweb.model.VoucherDTO> giftedVouchers = voucherService.getMyVouchers(null, token);
            List<com.bookweb.model.VoucherDTO> codeVouchers = voucherService.getPublicCodeVouchers(null, token);
            model.addAttribute("user", user);
            model.addAttribute("giftedVouchers", giftedVouchers);
            model.addAttribute("codeVouchers", codeVouchers);
            return "auth/my-vouchers";
        } catch (Exception e) {
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            String token = TokenUtil.getTokenFromRequest();
            if (token == null) return "redirect:/auth/login";
            orderService.cancelOrder(id, token);
            redirectAttributes.addFlashAttribute("message", "Đã hủy đơn hàng thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hủy đơn: " + e.getMessage());
        }
        return "redirect:/auth/orders";
    }

    @PostMapping("/orders/{id}/confirm")
    public String confirmOrder(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            String token = TokenUtil.getTokenFromRequest();
            if (token == null) return "redirect:/auth/login";
            orderService.confirmOrder(id, token);
            redirectAttributes.addFlashAttribute("message", "Đã xác nhận nhận hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi xác nhận: " + e.getMessage());
        }
        return "redirect:/auth/orders";
    }

    @PostMapping("/orders/{id}/review")
    public String submitOrderReview(
            @PathVariable String id,
            @RequestParam String bookId,
            @RequestParam int rating,
            @RequestParam(required = false) String title,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile reviewImage,
            RedirectAttributes redirectAttributes) {
        try {
            String token = TokenUtil.getTokenFromRequest();
            if (token == null) return "redirect:/auth/login";
            String reviewImageUrl = null;
            if (reviewImage != null && !reviewImage.isEmpty()) {
                try {
                    String uploadDir = "uploads/reviews/";
                    Files.createDirectories(Paths.get(uploadDir));
                    String safeName = reviewImage.getOriginalFilename() == null
                            ? "review.jpg"
                            : reviewImage.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
                    String fileName = System.currentTimeMillis() + "_" + safeName;
                    reviewImage.transferTo(Paths.get(uploadDir, fileName));
                    reviewImageUrl = "/" + uploadDir + fileName;
                } catch (Exception e) {
                    logger.error("Review image upload error: {}", e.getMessage());
                }
            }
            orderService.submitOrderReview(id, bookId, rating, title, content, reviewImageUrl, token);
            redirectAttributes.addFlashAttribute("message", "Cảm ơn bạn đã đánh giá sản phẩm!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi gửi đánh giá: " + e.getMessage());
        }
        return "redirect:/auth/orders";
    }
}
