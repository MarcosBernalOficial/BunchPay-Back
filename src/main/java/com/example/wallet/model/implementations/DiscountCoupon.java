package com.example.wallet.model.implementations;

import com.example.wallet.model.implementations.AccountClient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "discount_coupons")
@Data
public class DiscountCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private AccountClient accountClient;

    private String company;

    private Integer discountPercent;

    private String code;

    private LocalDate expirationDate;
}

