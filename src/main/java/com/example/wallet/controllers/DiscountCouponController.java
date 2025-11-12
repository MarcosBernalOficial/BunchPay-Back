package com.example.wallet.controllers;

import com.example.wallet.dtos.DiscountCouponDto;
import com.example.wallet.model.implementations.AccountClient;
import com.example.wallet.model.implementations.DiscountCoupon;
import com.example.wallet.repository.AccountClientRepository;
import com.example.wallet.services.DiscountCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/discount-coupons")
@RequiredArgsConstructor
@Tag(name = "Coupon", description = "Operaciones relacionadas a los cupones de descuento")
public class DiscountCouponController {

    private final DiscountCouponService discountCouponService;
    private final AccountClientRepository accountClientRepository;

    @Operation(summary = "Recibir cupon", description = "Recibe todos los cupones de descuento")
    @GetMapping("/my-active")
    public ResponseEntity<List<DiscountCouponDto>> getMyActiveCoupons(Authentication auth) {
        String email = auth.getName();
        AccountClient accountClient = accountClientRepository.findByClientEmail(email)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        List<DiscountCoupon> coupons = discountCouponService.getActiveCoupons(accountClient.getId());
        // Mapear a DTO (opcional, pero recomendado)
        List<DiscountCouponDto> dtos = coupons.stream().map(DiscountCouponDto::fromEntity).toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Forzar generación de cupones", description = "Genera cupones si la cuenta no tiene activos")
    @PostMapping("/generate-if-empty")
    public ResponseEntity<List<DiscountCouponDto>> generateIfEmpty(Authentication auth) {
        String email = auth.getName();
        AccountClient accountClient = accountClientRepository.findByClientEmail(email)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        List<DiscountCoupon> coupons = discountCouponService.getActiveCoupons(accountClient.getId());
        List<DiscountCouponDto> dtos = coupons.stream().map(DiscountCouponDto::fromEntity).toList();
        return ResponseEntity.ok(dtos);
    }
}
