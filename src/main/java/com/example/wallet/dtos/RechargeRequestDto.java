package com.example.wallet.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RechargeRequestDto {
    private String type;
    @NotBlank
    private String destination;
    private float amount;
}
