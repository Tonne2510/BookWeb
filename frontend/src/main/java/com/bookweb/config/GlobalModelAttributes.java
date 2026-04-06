package com.bookweb.config;

import com.bookweb.model.CategoryDTO;
import com.bookweb.service.CartService;
import com.bookweb.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpSession session) {
        // Add active categories for navigation menu
        try {
            List<CategoryDTO> menuCategories = categoryService.getAllCategories(1, 50, "active");
            model.addAttribute("menuCategories", menuCategories);
        } catch (Exception e) {
            model.addAttribute("menuCategories", Collections.emptyList());
        }

        // Add cart count for header badge
        try {
            Map<String, Object> cart = cartService.getCart(session);
            model.addAttribute("cartCount", cart.get("count"));
        } catch (Exception e) {
            model.addAttribute("cartCount", 0);
        }
    }
}
