package com.example.wallet.services;

import com.example.wallet.dtos.CryptoPriceDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BinanceService {
    private final RestTemplate restTemplate;
    private Collectors Collector;

    public BinanceService() {
        this.restTemplate = new RestTemplate();
    }

    public CryptoPriceDto getCryptoPrice(String symbol) {
        String url = "https://api3.binance.com/api/v3/ticker/price?symbol=" + symbol;
        return restTemplate.getForObject(url, CryptoPriceDto.class);
    }

    public Optional<List<CryptoPriceDto>> getAllPrices() {
        List<String> symbols = List.of("BTCARS", "ETHARS", "USDTARS");
        return Optional.of(symbols.stream()
                .map(this::getCryptoPrice)
                .collect(Collector.toList()));
    }
}
