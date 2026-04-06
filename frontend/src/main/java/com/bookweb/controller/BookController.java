package com.bookweb.controller;

import com.bookweb.model.BookDTO;
import com.bookweb.model.ReviewDTO;
import com.bookweb.model.CategoryDTO;
import com.bookweb.model.AuthorDTO;
import com.bookweb.service.BookService;
import com.bookweb.service.ReviewService;
import com.bookweb.service.CategoryService;
import com.bookweb.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AuthorService authorService;

    @GetMapping
    public String listBooks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String order,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String authorId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            Model model) {
        try {
            List<BookDTO> books = bookService.getAllBooks(page, limit, search, sortBy, order, "active", categoryId, authorId, minPrice, maxPrice);
            model.addAttribute("books", books);
            model.addAttribute("page", page);
            model.addAttribute("limit", limit);
            model.addAttribute("searchTerm", search);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("order", order);
            model.addAttribute("selectedCategoryId", categoryId);
            model.addAttribute("selectedAuthorId", authorId);
            model.addAttribute("minPrice", minPrice);
            model.addAttribute("maxPrice", maxPrice);

            try {
                List<CategoryDTO> categories = categoryService.getAllCategories(1, 200, "active");
                model.addAttribute("categories", categories);
            } catch (Exception e) {
                model.addAttribute("categories", java.util.Collections.emptyList());
            }
            try {
                List<AuthorDTO> authors = authorService.getAllAuthors(1, 200, "active");
                model.addAttribute("authors", authors);
            } catch (Exception e) {
                model.addAttribute("authors", java.util.Collections.emptyList());
            }

            return "books/list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/{slug}")
    public String viewBook(@PathVariable String slug, Model model) {
        try {
            BookDTO book = bookService.getBookBySlug(slug);
            model.addAttribute("book", book);

            // Load reviews for this book
            try {
                List<ReviewDTO> reviews = reviewService.getBookReviews(book.getId(), 1, 20);
                model.addAttribute("reviews", reviews);
                int reviewCount = reviewService.getBookReviewCount(book.getId());
                model.addAttribute("reviewCount", reviewCount);
            } catch (Exception e) {
                model.addAttribute("reviews", Collections.emptyList());
                model.addAttribute("reviewCount", 0);
            }

            return "books/detail";
        } catch (Exception e) {
            model.addAttribute("error", "Book not found");
            return "error";
        }
    }

    @PostMapping("/{id}/review")
    public String submitReview(
            @PathVariable String id,
            @RequestParam int rating,
            @RequestParam(required = false) String title,
            @RequestParam String content,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            String token = (String) session.getAttribute("token");
            if (token == null) {
                return "redirect:/auth/login";
            }
            reviewService.createReview(id, rating, title, content, token);
            redirectAttributes.addFlashAttribute("message", "Đánh giá đã được gửi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi gửi đánh giá: " + e.getMessage());
        }
        // Redirect back to the book detail - need to get slug
        try {
            BookDTO book = bookService.getBookById(id);
            return "redirect:/books/" + book.getSlug();
        } catch (Exception e) {
            return "redirect:/books";
        }
    }

    @GetMapping("/search")
    public String searchBooks(@RequestParam String q, Model model) {
        try {
            List<BookDTO> results = bookService.searchBooks(q);
            model.addAttribute("books", results);
            model.addAttribute("searchTerm", q);
            return "books/search-results";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}
