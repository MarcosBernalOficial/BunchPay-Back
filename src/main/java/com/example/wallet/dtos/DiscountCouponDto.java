package com.example.wallet.dtos;

import com.example.wallet.model.implementations.DiscountCoupon;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DiscountCouponDto {
    private String company;
    private int discountPercent;
    private String code;
    private LocalDate expirationDate;

    public static DiscountCouponDto fromEntity(DiscountCoupon coupon) {
        DiscountCouponDto dto = new DiscountCouponDto();
        dto.setCompany(coupon.getCompany());
        dto.setDiscountPercent(coupon.getDiscountPercent());
        dto.setCode(coupon.getCode());
        dto.setExpirationDate(coupon.getExpirationDate());
        return dto;
    }
}