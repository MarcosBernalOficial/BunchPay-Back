package com.example.wallet.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data

public class TransferRequestDto {

    //Manual validation in service
    private String receiverCVU;
    private String receiverAlias;
    private String description;

    @NotNull(message = "Ingrese un monto a transferir.")
    @Positive(message = "El monto no puede ser negativo o cero.")
    private float amount;
}
