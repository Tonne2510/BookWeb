package com.bookweb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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

    public String getStartDateDisplay() {
        return toDisplayDate(startDate);
    }

    public String getEndDateDisplay() {
        return toDisplayDate(endDate);
    }

    private String toDisplayDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return "-";
        }

        String value = raw.trim();

        // Handle unix timestamp in seconds/milliseconds
        if (value.matches("^\\d{10,13}$")) {
            try {
                long epoch = Long.parseLong(value);
                if (value.length() == 10) {
                    epoch *= 1000;
                }
                LocalDate date = Instant.ofEpochMilli(epoch)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception ignored) {
            }
        }

        // Handle ISO date-time
        if (value.length() >= 10) {
            return value.substring(0, 10);
        }

        return value;
    }
}
