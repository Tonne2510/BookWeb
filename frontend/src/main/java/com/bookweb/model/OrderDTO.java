package com.bookweb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private String id;
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Double totalPrice;
    private Double totalDiscount;
    private String shippingAddress;
    private Double shippingCost;
    private String paymentMethod;
    private String status;
    private String notes;
    private UserDTO user;
    private List<OrderItemDTO> items;
    private String createdAt;
    private String updatedAt;
}
