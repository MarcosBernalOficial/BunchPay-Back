package com.example.wallet.services;

import com.example.wallet.controllers.exceptions.InsufficientBalanceException;
import com.example.wallet.dtos.*;
import com.example.wallet.model.enums.CryptoTransactionType;
import com.example.wallet.model.implementations.AccountClient;
import com.example.wallet.model.implementations.CryptoHolding;
import com.example.wallet.model.implementations.CryptoTransaction;
import com.example.wallet.repository.AccountClientRepository;
import com.example.wallet.repository.CryptoHoldingRepository;
import com.example.wallet.repository.CryptoTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CryptoService {

    private final CryptoHoldingRepository cryptoHoldingRepository;
    private final CryptoTransactionRepository cryptoTransactionRepository;
    private final AccountClientRepository accountClientRepository;
    private final BinanceService binanceService;

    @Transactional
    public CryptoTransactionDto buyCrypto(AccountClient account, BuyCryptoRequestDto request) {
        // Construir símbolo completo (BTC -> BTCARS)
        String fullSymbol = request.getSymbol() + "ARS";

        // Obtener precio actual de Binance
        CryptoPriceDto priceDto = binanceService.getCryptoPrice(fullSymbol);
        Double currentPrice = Double.parseDouble(priceDto.getPrice());

        // Verificar saldo suficiente
        if (account.getBalance() < request.getAmountArs()) {
            throw new InsufficientBalanceException("Saldo insuficiente para comprar crypto");
        }

        // Calcular cantidad de crypto a comprar
        Double cryptoAmount = request.getAmountArs() / currentPrice;

        // Descontar del saldo
        account.setBalance(account.getBalance() - request.getAmountArs().floatValue());
        accountClientRepository.save(account);

        // Buscar o crear holding (guardar con símbolo completo BTCARS)
        CryptoHolding holding = cryptoHoldingRepository
                .findByAccountClientIdAndSymbol(account.getId(), fullSymbol)
                .orElse(new CryptoHolding());

        if (holding.getId() == null) {
            // Nuevo holding
            holding.setAccountClient(account);
            holding.setSymbol(fullSymbol);
            holding.setAmount(cryptoAmount);
            holding.setAveragePurchasePrice(currentPrice);
        } else {
            // Actualizar holding existente con precio promedio ponderado
            Double totalCrypto = holding.getAmount() + cryptoAmount;
            Double totalInvested = (holding.getAmount() * holding.getAveragePurchasePrice()) +
                    (cryptoAmount * currentPrice);
            holding.setAveragePurchasePrice(totalInvested / totalCrypto);
            holding.setAmount(totalCrypto);
        }
        holding.setLastUpdated(LocalDateTime.now());
        cryptoHoldingRepository.save(holding);

        // Registrar transacción (con símbolo completo)
        CryptoTransaction transaction = new CryptoTransaction();
        transaction.setAccountClient(account);
        transaction.setSymbol(fullSymbol);
        transaction.setAmount(cryptoAmount);
        transaction.setPriceArs(currentPrice);
        transaction.setTotalArs(request.getAmountArs());
        transaction.setType(CryptoTransactionType.BUY);
        transaction.setDate(LocalDateTime.now());
        cryptoTransactionRepository.save(transaction);

        return mapTransactionToDto(transaction);
    }

    @Transactional
    public CryptoTransactionDto sellCrypto(AccountClient account, SellCryptoRequestDto request) {
        // Construir símbolo completo (BTC -> BTCARS)
        String fullSymbol = request.getSymbol() + "ARS";

        // Buscar holding (con símbolo completo)
        CryptoHolding holding = cryptoHoldingRepository
                .findByAccountClientIdAndSymbol(account.getId(), fullSymbol)
                .orElseThrow(() -> new RuntimeException("No posees esta criptomoneda"));

        // Verificar cantidad suficiente
        if (holding.getAmount() < request.getAmount()) {
            throw new RuntimeException("No tienes suficiente cantidad de " + fullSymbol);
        }

        // Obtener precio actual de Binance
        CryptoPriceDto priceDto = binanceService.getCryptoPrice(fullSymbol);
        Double currentPrice = Double.parseDouble(priceDto.getPrice());

        // Calcular monto en ARS
        Double totalArs = request.getAmount() * currentPrice;

        // Acreditar en saldo
        account.setBalance(account.getBalance() + totalArs.floatValue());
        accountClientRepository.save(account);

        // Actualizar holding
        holding.setAmount(holding.getAmount() - request.getAmount());
        holding.setLastUpdated(LocalDateTime.now());

        if (holding.getAmount() <= 0.00000001) {
            // Si la cantidad es prácticamente 0, eliminar el holding
            cryptoHoldingRepository.delete(holding);
        } else {
            cryptoHoldingRepository.save(holding);
        }

        // Registrar transacción (con símbolo completo)
        CryptoTransaction transaction = new CryptoTransaction();
        transaction.setAccountClient(account);
        transaction.setSymbol(fullSymbol);
        transaction.setAmount(request.getAmount());
        transaction.setPriceArs(currentPrice);
        transaction.setTotalArs(totalArs);
        transaction.setType(CryptoTransactionType.SELL);
        transaction.setDate(LocalDateTime.now());
        cryptoTransactionRepository.save(transaction);

        return mapTransactionToDto(transaction);
    }

    public List<CryptoHoldingDto> getMyHoldings(Long accountClientId) {
        List<CryptoHolding> holdings = cryptoHoldingRepository.findByAccountClientId(accountClientId);

        return holdings.stream().map(holding -> {
            // Obtener precio actual (el holding ya tiene el símbolo completo BTCARS)
            CryptoPriceDto priceDto = binanceService.getCryptoPrice(holding.getSymbol());
            Double currentPrice = Double.parseDouble(priceDto.getPrice());

            CryptoHoldingDto dto = new CryptoHoldingDto();
            dto.setId(holding.getId());
            dto.setSymbol(holding.getSymbol());
            dto.setAmount(holding.getAmount());
            dto.setAveragePurchasePrice(holding.getAveragePurchasePrice());
            dto.setCurrentPrice(currentPrice);
            dto.setTotalValueArs(holding.getAmount() * currentPrice);

            Double profitLoss = (currentPrice - holding.getAveragePurchasePrice()) * holding.getAmount();
            dto.setProfitLossArs(profitLoss);
            dto.setProfitLossPercentage(
                    ((currentPrice - holding.getAveragePurchasePrice()) / holding.getAveragePurchasePrice()) * 100);

            return dto;
        }).collect(Collectors.toList());
    }

    public List<CryptoTransactionDto> getMyTransactions(Long accountClientId) {
        List<CryptoTransaction> transactions = cryptoTransactionRepository
                .findByAccountClientIdOrderByDateDesc(accountClientId);

        return transactions.stream()
                .map(this::mapTransactionToDto)
                .collect(Collectors.toList());
    }

    private CryptoTransactionDto mapTransactionToDto(CryptoTransaction transaction) {
        CryptoTransactionDto dto = new CryptoTransactionDto();
        dto.setId(transaction.getId());
        dto.setSymbol(transaction.getSymbol());
        dto.setAmount(transaction.getAmount());
        dto.setPriceArs(transaction.getPriceArs());
        dto.setTotalArs(transaction.getTotalArs());
        dto.setType(transaction.getType());
        dto.setDate(transaction.getDate());
        return dto;
    }
}
