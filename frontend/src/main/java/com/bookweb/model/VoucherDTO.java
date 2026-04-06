package com.bookweb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDTO {
    private String id;
    private String code;
    private String userId;
    private String name;
    private String description;
    private String type;
    private String distributionType;
    private Double value;
    private Double minOrderValue;
    private Double maxDiscount;
    private Integer totalUsageLimit;
    private Integer usedCount;
    private Integer giftedCount;
    private Integer perUserLimit;
    private String startDate;
    private String endDate;
    private Boolean isActive;
    private String giftSource;
    private String giftConditionType;
    private Double minGiftAmount;
    private Double maxGiftAmount;
    private Integer minGiftReviewCount;
    private Integer maxGiftReviewCount;
    private Boolean giftedBySystem;
    private String sourceTemplateId;
    private String createdAt;
    private String updatedAt;
}
