package com.bookweb.controller;

import com.bookweb.model.FavoriteDTO;
import com.bookweb.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping
    public String favoritesPage(HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            return "redirect:/auth/login";
        }
        try {
            List<FavoriteDTO> favorites = favoriteService.getMyFavorites(token);
            model.addAttribute("favorites", favorites);
        } catch (Exception e) {
            model.addAttribute("favorites", java.util.Collections.emptyList());
            model.addAttribute("error", "Không thể tải danh sách yêu thích: " + e.getMessage());
        }
        return "auth/favorites";
    }

    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @RequestParam String bookId,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        String token = (String) session.getAttribute("token");
        if (token == null) {
            result.put("success", false);
            result.put("message", "Bạn cần đăng nhập để sử dụng tính năng này");
            return ResponseEntity.status(401).body(result);
        }
        try {
            boolean wasFavorite = favoriteService.isFavorite(bookId, token);
            if (wasFavorite) {
                favoriteService.removeFromFavorites(bookId, token);
                result.put("isFavorite", false);
                result.put("message", "Đã xóa khỏi danh sách yêu thích");
            } else {
                favoriteService.addToFavorites(bookId, token);
                result.put("isFavorite", true);
                result.put("message", "Đã thêm vào danh sách yêu thích");
            }
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addFavorite(
            @RequestParam String bookId,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        String token = (String) session.getAttribute("token");
        if (token == null) {
            result.put("success", false);
            result.put("message", "Bạn cần đăng nhập");
            return ResponseEntity.status(401).body(result);
        }
        try {
            favoriteService.addToFavorites(bookId, token);
            result.put("success", true);
            result.put("isFavorite", true);
            result.put("message", "Đã thêm vào danh sách yêu thích");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFavorite(
            @RequestParam String bookId,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        String token = (String) session.getAttribute("token");
        if (token == null) {
            result.put("success", false);
            result.put("message", "Bạn cần đăng nhập");
            return ResponseEntity.status(401).body(result);
        }
        try {
            favoriteService.removeFromFavorites(bookId, token);
            result.put("success", true);
            result.put("isFavorite", false);
            result.put("message", "Đã xóa khỏi danh sách yêu thích");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
