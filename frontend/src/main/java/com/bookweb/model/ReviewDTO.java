package com.bookweb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private String id;
    private Integer rating;
    private String title;
    private String content;
    private Integer helpfulCount;
    private String status;
    private UserDTO user;
    private BookDTO book;
    private String userId;
    private String userName;
    private String bookId;
    private String bookTitle;
    private String createdAt;
    private String updatedAt;
}
