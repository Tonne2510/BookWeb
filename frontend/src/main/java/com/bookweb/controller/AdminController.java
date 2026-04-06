package com.bookweb.controller;

import com.bookweb.service.*;
import com.bookweb.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private BookService bookService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private VoucherService voucherService;

    // Check if user is admin
    private void checkAdminAccess() throws Exception {
        logger.info("Checking admin access");
        String token = TokenUtil.getTokenFromRequest();
        logger.info("Token from request: {}", token != null ? "FOUND (length: " + token.length() + ")" : "NOT FOUND");
        if (token == null || token.isEmpty()) {
            logger.error("Unauthorized: Admin access required - token is null/empty");
            throw new Exception("Unauthorized: Admin access required");
        }
    }

    private String getTokenFromSession() {
        return TokenUtil.getTokenFromRequest();
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();

            // Content stats
            var books = bookService.getAllBooks(1, 200, "", "title", "asc");
            var categories = categoryService.getAllCategories(1, 100);
            var authors = authorService.getAllAuthors(1, 100);

            // All orders for revenue/status stats
            var allOrders = orderService.getAllOrders(1, 500, null, token);

            // Users & reviews
            var users = userService.getAllUsers(1, 500, token);
            var reviews = reviewService.getAllReviews(1, 500, null);

            // Order status breakdown
            long pendingCount   = allOrders.stream().filter(o -> "pending".equals(o.getStatus())).count();
            long processingCount= allOrders.stream().filter(o -> "processing".equals(o.getStatus())).count();
            long shippedCount   = allOrders.stream().filter(o -> "shipped".equals(o.getStatus())).count();
            long deliveredCount = allOrders.stream().filter(o -> "delivered".equals(o.getStatus())).count();
            long confirmedCount = allOrders.stream().filter(o -> "confirmed".equals(o.getStatus())).count();
            long cancelledCount = allOrders.stream().filter(o -> "cancelled".equals(o.getStatus())).count();

            // Total revenue: sum of all non-cancelled orders
            double totalRevenue = allOrders.stream()
                .filter(o -> !"cancelled".equals(o.getStatus()))
                .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0)
                .sum();

            // Recent 10 orders sorted by createdAt desc
            var recentOrders = allOrders.stream()
                .sorted((a, b) -> {
                    String ca = a.getCreatedAt() != null ? a.getCreatedAt() : "";
                    String cb = b.getCreatedAt() != null ? b.getCreatedAt() : "";
                    return cb.compareTo(ca);
                })
                .limit(10)
                .collect(java.util.stream.Collectors.toList());

            model.addAttribute("totalBooks", books.size());
            model.addAttribute("totalCategories", categories.size());
            model.addAttribute("totalAuthors", authors.size());
            model.addAttribute("totalOrders", allOrders.size());
            model.addAttribute("totalUsers", users.size());
            model.addAttribute("totalReviews", reviews.size());
            model.addAttribute("totalRevenue", (long) totalRevenue);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("processingCount", processingCount);
            model.addAttribute("shippedCount", shippedCount);
            model.addAttribute("deliveredCount", deliveredCount);
            model.addAttribute("confirmedCount", confirmedCount);
            model.addAttribute("cancelledCount", cancelledCount);
            model.addAttribute("recentOrders", recentOrders);

            return "admin/dashboard-standalone";
        } catch (Exception e) {
            logger.error("Dashboard error: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Unauthorized access");
            return "redirect:/auth/login";
        }
    }

    // ===== BOOK MANAGEMENT =====
    @GetMapping("/books")
    public String manageBooks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            var books = bookService.getAllBooks(page, limit, "", "createdAt", "DESC");
            model.addAttribute("books", books);
            model.addAttribute("page", page);
            model.addAttribute("limit", limit);
            return "admin/books-standalone";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized access");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/books/create")
    public String createBookPage(Model model, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            var categories = categoryService.getAllCategories(1, 100, "active");
            var authors = authorService.getAllAuthors(1, 100, "active");
            model.addAttribute("categories", categories);
            model.addAttribute("authors", authors);
            return "admin/book-create-standalone";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized access");
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/books/create")
    public String createBook(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam Double price,
            @RequestParam(required = false) Double discount,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String authorId,
            @RequestParam(required = false) MultipartFile coverImage,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) String publicationDate,
            @RequestParam(required = false) Integer pages,
            @RequestParam(required = false) Integer stock,
            RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            
            // Handle file upload
            String coverImageFileName = null;
            if (coverImage != null && !coverImage.isEmpty()) {
                try {
                    String uploadDir = "uploads/books/";
                    Files.createDirectories(Paths.get(uploadDir));
                    String fileName = System.currentTimeMillis() + "_" + coverImage.getOriginalFilename();
                    coverImage.transferTo(Paths.get(uploadDir, fileName));
                    coverImageFileName = "/" + uploadDir + fileName;
                } catch (Exception e) {
                    logger.error("File upload error: {}", e.getMessage());
                }
            }
            
            bookService.createBook(title, description, price, discount, isbn, categoryId, authorId,
                    coverImageFileName, publisher, publicationDate, pages, stock, token);
            redirectAttributes.addFlashAttribute("message", "Book created successfully");
            return "redirect:/admin/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/books/create";
        }
    }

    @GetMapping("/books/edit/{id}")
    public String editBook(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            var book = bookService.getBookById(id);
            var categories = categoryService.getAllCategories(1, 100, "active");
            var authors = authorService.getAllAuthors(1, 100, "active");
            model.addAttribute("book", book);
            model.addAttribute("categories", categories);
            model.addAttribute("authors", authors);
            return "admin/book-edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/books";
        }
    }

    @PostMapping("/books/update/{id}")
    public String updateBook(@PathVariable String id,
                            @RequestParam String title,
                            @RequestParam String description,
                            @RequestParam Double price,
                            @RequestParam(required = false) Double discount,
                            @RequestParam(required = false) String isbn,
                            @RequestParam(required = false) String categoryId,
                            @RequestParam(required = false) String authorId,
                            @RequestParam(required = false) MultipartFile coverImage,
                            @RequestParam(required = false) String publisher,
                            @RequestParam(required = false) String publicationDate,
                            @RequestParam(required = false) Integer pages,
                            @RequestParam(required = false) Double stock,
                            @RequestParam(required = false) String status,
                            RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            // Convert Double stock to Integer if provided
            Integer stockInt = stock != null ? stock.intValue() : null;
            
            // Handle file upload
            String coverImageFileName = null;
            if (coverImage != null && !coverImage.isEmpty()) {
                try {
                    String uploadDir = "uploads/books/";
                    Files.createDirectories(Paths.get(uploadDir));
                    String fileName = System.currentTimeMillis() + "_" + coverImage.getOriginalFilename();
                    coverImage.transferTo(Paths.get(uploadDir, fileName));
                    coverImageFileName = "/" + uploadDir + fileName;
                } catch (Exception e) {
                    logger.error("File upload error: {}", e.getMessage());
                }
            }
            
            bookService.updateBook(id, title, description, price, discount, isbn, categoryId,
                    authorId, coverImageFileName, publisher, publicationDate, pages, stockInt, status, token);
            redirectAttributes.addFlashAttribute("message", "Book updated successfully");
            return "redirect:/admin/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/books/edit/" + id;
        }
    }

    @GetMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            bookService.deleteBook(id, token);
            redirectAttributes.addFlashAttribute("message", "Book deleted successfully");
            return "redirect:/admin/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/books";
        }
    }

    @GetMapping("/books/toggle-status/{id}")
    public String toggleBookStatus(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            bookService.toggleBookStatus(id, token);
            redirectAttributes.addFlashAttribute("message", "Book status toggled successfully");
            return "redirect:/admin/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/books";
        }
    }

    // ===== CATEGORY MANAGEMENT =====
    @GetMapping("/categories")
    public String manageCategories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            var categories = categoryService.getAllCategories(page, limit);
            model.addAttribute("categories", categories);
            model.addAttribute("page", page);
            model.addAttribute("limit", limit);
            return "admin/categories-standalone";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized access");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/categories/create")
    public String createCategoryPage(Model model, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            return "admin/category-create-standalone";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized access");
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/categories/create")
    public String createCategory(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String icon,
            RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            categoryService.createCategory(name, description, icon, token);
            redirectAttributes.addFlashAttribute("message", "Category created successfully");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategoryPage(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            var category = categoryService.getCategoryById(id);
            model.addAttribute("category", category);
            return "admin/category-edit-standalone";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    @PostMapping("/categories/update/{id}")
    public String updateCategory(
            @PathVariable String id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String icon,
            @RequestParam(required = false) String status,
            RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            String categoryStatus = (status != null && status.equals("active")) ? "active" : "inactive";
            categoryService.updateCategory(id, name, description, icon, categoryStatus, token);
            redirectAttributes.addFlashAttribute("message", "Category updated successfully");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            categoryService.deleteCategory(id, token);
            redirectAttributes.addFlashAttribute("message", "Category deleted successfully");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    @GetMapping("/categories/toggle-status/{id}")
    public String toggleCategoryStatus(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            categoryService.toggleCategoryStatus(id, token);
            redirectAttributes.addFlashAttribute("message", "Category status toggled successfully");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    // ===== AUTHOR MANAGEMENT =====
    @GetMapping("/authors")
    public String manageAuthors(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            var authors = authorService.getAllAuthors(page, limit);
            model.addAttribute("authors", authors);
            model.addAttribute("page", page);
            model.addAttribute("limit", limit);
            return "admin/authors-standalone";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized access");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/authors/create")
    public String createAuthorPage(Model model, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            return "admin/author-create-standalone";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized access");
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/authors/create")
    public String createAuthor(
            @RequestParam String name,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String nationality,
            @RequestParam(required = false) String avatar,
            RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            authorService.createAuthor(name, bio, nationality, avatar, token);
            redirectAttributes.addFlashAttribute("message", "Author created successfully");
            return "redirect:/admin/authors";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/authors";
        }
    }

    @GetMapping("/authors/edit/{id}")
    public String editAuthorPage(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            var author = authorService.getAuthorById(id);
            model.addAttribute("author", author);
            return "admin/author-edit-standalone";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/authors";
        }
    }

    @PostMapping("/authors/update/{id}")
    public String updateAuthor(
            @PathVariable String id,
            @RequestParam String name,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String nationality,
            @RequestParam(required = false) String avatar,
            @RequestParam(required = false) String status,
            RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            String authorStatus = (status != null && status.equals("active")) ? "active" : "inactive";
            authorService.updateAuthor(id, name, bio, nationality, avatar, authorStatus, token);
            redirectAttributes.addFlashAttribute("message", "Author updated successfully");
            return "redirect:/admin/authors";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/authors";
        }
    }

    @GetMapping("/authors/delete/{id}")
    public String deleteAuthor(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            authorService.deleteAuthor(id, token);
            redirectAttributes.addFlashAttribute("message", "Author deleted successfully");
            return "redirect:/admin/authors";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/authors";
        }
    }

    @GetMapping("/authors/toggle-status/{id}")
    public String toggleAuthorStatus(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            authorService.toggleAuthorStatus(id, token);
            redirectAttributes.addFlashAttribute("message", "Author status toggled successfully");
            return "redirect:/admin/authors";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/authors";
        }
    }

    // ===== ORDER MANAGEMENT =====
    @GetMapping("/orders")
    public String manageOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String status,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            var orders = orderService.getAllOrders(page, limit, status, token);
            model.addAttribute("orders", orders);
            model.addAttribute("page", page);
            model.addAttribute("limit", limit);
            model.addAttribute("currentStatus", status);
            return "admin/orders-standalone";
        } catch (Exception e) {
            logger.error("Manage orders error: {}", e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("Unauthorized")) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access");
                return "redirect:/auth/login";
            }
            redirectAttributes.addFlashAttribute("error", "Lỗi tải đơn hàng: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @GetMapping("/orders/detail/{id}")
    public String orderDetail(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            var order = orderService.getOrderById(id, token);
            model.addAttribute("order", order);
            return "admin/order-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/orders";
        }
    }

    @PostMapping("/orders/update-status/{id}")
    public String updateOrderStatus(
            @PathVariable String id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            orderService.updateOrderStatus(id, status, token);
            redirectAttributes.addFlashAttribute("message", "Order status updated successfully");
            return "redirect:/admin/orders/detail/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/orders/detail/" + id;
        }
    }

    // ===== REVIEW MANAGEMENT =====
    @GetMapping("/reviews")
    public String manageReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String status,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            var reviews = reviewService.getAllReviews(page, limit, status);
            model.addAttribute("reviews", reviews);
            model.addAttribute("page", page);
            model.addAttribute("limit", limit);
            model.addAttribute("currentStatus", status);
            return "admin/reviews-standalone";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized access");
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/reviews/delete/{id}")
    public String deleteReview(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            reviewService.deleteReview(id, token);
            redirectAttributes.addFlashAttribute("message", "Đã xóa nhận xét thành công");
            return "redirect:/admin/reviews";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/reviews";
        }
    }

    @GetMapping("/reviews/delete/{id}")
    public String deleteReviewGet(@PathVariable String id, RedirectAttributes redirectAttributes) {
        return deleteReview(id, redirectAttributes);
    }

    @PostMapping("/reviews/approve/{id}")
    public String approveReview(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            reviewService.approveReview(id, token);
            redirectAttributes.addFlashAttribute("message", "Đã duyệt nhận xét thành công");
            return "redirect:/admin/reviews";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/reviews";
        }
    }

    @PostMapping("/reviews/reject/{id}")
    public String rejectReview(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            reviewService.rejectReview(id, token);
            redirectAttributes.addFlashAttribute("message", "Đã ẩn nhận xét thành công");
            return "redirect:/admin/reviews";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/reviews";
        }
    }

    // ===== VOUCHER MANAGEMENT =====
    @GetMapping("/vouchers")
    public String manageVouchers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            var vouchers = voucherService.getAllVouchers(page, limit, token);
            model.addAttribute("vouchers", vouchers);
            return "admin/vouchers-standalone";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/vouchers/create")
    public String createVoucher(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String type,
            @RequestParam(required = false, defaultValue = "code") String distributionType,
            @RequestParam Double value,
            @RequestParam(required = false, defaultValue = "0") Double minOrderValue,
            @RequestParam(required = false) Double maxDiscount,
            @RequestParam(required = false) Integer totalUsageLimit,
            @RequestParam(required = false, defaultValue = "1") Integer perUserLimit,
            @RequestParam(required = false, defaultValue = "amount") String giftConditionType,
            @RequestParam(required = false) Double minGiftAmount,
            @RequestParam(required = false) Double maxGiftAmount,
            @RequestParam(required = false) Integer minGiftReviewCount,
            @RequestParam(required = false) Integer maxGiftReviewCount,
            @RequestParam String startDate,
            @RequestParam String endDate,
            RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();

            Map<String, Object> payload = new HashMap<>();
            payload.put("code", code);
            payload.put("name", name);
            payload.put("description", description);
            payload.put("type", type);
            payload.put("distributionType", distributionType);
            payload.put("value", value);
            payload.put("minOrderValue", minOrderValue);
            payload.put("maxDiscount", maxDiscount);
            payload.put("totalUsageLimit", totalUsageLimit);
            payload.put("perUserLimit", perUserLimit);
            payload.put("giftConditionType", giftConditionType);
            if (minGiftAmount != null) {
                payload.put("minGiftAmount", minGiftAmount);
            }
            if (maxGiftAmount != null) {
                payload.put("maxGiftAmount", maxGiftAmount);
            }
            if (minGiftReviewCount != null) {
                payload.put("minGiftReviewCount", minGiftReviewCount);
            }
            if (maxGiftReviewCount != null) {
                payload.put("maxGiftReviewCount", maxGiftReviewCount);
            }
            payload.put("startDate", startDate);
            payload.put("endDate", endDate);

            voucherService.createVoucher(payload, token);
            redirectAttributes.addFlashAttribute("message", "Tạo voucher thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/vouchers/toggle/{id}")
    public String toggleVoucher(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            voucherService.toggleVoucherStatus(id, token);
            redirectAttributes.addFlashAttribute("message", "Đã cập nhật trạng thái voucher");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/vouchers/delete/{id}")
    public String deleteVoucher(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            voucherService.deleteVoucher(id, token);
            redirectAttributes.addFlashAttribute("message", "Đã xóa voucher");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    // ===== USER MANAGEMENT =====
    @GetMapping("/users")
    public String manageUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            var users = userService.getAllUsers(page, limit, token);
            model.addAttribute("users", users);
            model.addAttribute("page", page);
            model.addAttribute("limit", limit);
            return "admin/users-standalone";
        } catch (Exception e) {
            logger.error("Failed to load users: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Unauthorized access");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/users/detail/{id}")
    public String userDetail(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            var user = userService.getUserById(id, token);
            model.addAttribute("user", user);
            return "admin/user-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            userService.toggleUserStatus(id, token);
            redirectAttributes.addFlashAttribute("message", "User status toggled successfully");
            return "redirect:/admin/users/detail/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users/detail/" + id;
        }
    }

    @PostMapping("/users/change-role/{id}")
    public String changeUserRole(
            @PathVariable String id,
            @RequestParam String role,
            RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            userService.changeUserRole(id, role, token);
            redirectAttributes.addFlashAttribute("message", "User role changed successfully");
            return "redirect:/admin/users/detail/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users/detail/" + id;
        }
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            checkAdminAccess();
            String token = getTokenFromSession();
            userService.deleteUser(id, token);
            redirectAttributes.addFlashAttribute("message", "User deleted successfully");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }
}

