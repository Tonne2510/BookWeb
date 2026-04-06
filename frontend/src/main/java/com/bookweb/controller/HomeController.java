package com.bookweb.controller;

import com.bookweb.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public String index(Model model) {
        try {
            model.addAttribute("featuredBooks", bookService.getAllBooks(1, 8, "", "createdAt", "DESC", "active"));
        } catch (Exception e) {
            model.addAttribute("featuredBooks", java.util.Collections.emptyList());
        }
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}
