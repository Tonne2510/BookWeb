package com.bookweb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private String id;
    private Integer quantity;
    private Double price;
    private Double discount;
    private BookDTO book;
    private String bookId;
    private String bookTitle;
}
