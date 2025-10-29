package com.example.wallet.controllers;

import com.example.wallet.dtos.CryptoPriceDto;
import com.example.wallet.services.BinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crypto")
@Tag(name = "Cripto", description = "Se conecta con la api de Binance")
public class CryptoController {
    private final BinanceService binanceService;

    @Operation(summary = "Obtener cotizaciones", description = "Devuelve la cotizacion de las princpales monedas en pesos")
    @GetMapping("/prices")
    public List<CryptoPriceDto> getPrices() {
        return binanceService.getAllPrices()
                .orElseThrow(() -> new RuntimeException("No se pudieron obtener los precios"));
    }
}
