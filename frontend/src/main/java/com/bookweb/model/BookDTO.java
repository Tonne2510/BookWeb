package com.bookweb.model;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private String id;
    private String title;
    private String slug;
    private String description;
    private String isbn;
    private Double price;
    private Double discount;
    private Double finalPrice;
    private String coverImage;
    private String publisher;
    private String publicationDate;
    private Integer pages;
    private Integer stock;
    private Double rating;
    private Integer views;
    private String status;
    private String categoryId;
    private String categoryName;
    private String authorId;
    private String authorName;
}
