package com.example.wallet.services;

import com.example.wallet.model.implementations.Profit;
import com.example.wallet.model.implementations.AccountClient;
import com.example.wallet.repository.AccountClientRepository;
import com.example.wallet.repository.ProfitRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfitService {

    private static final double ANNUAL_RATE = 0.39;

    private final ProfitRepository profitRepository;
    private final AccountClientRepository accountClientRepository;

    public Double getTotalProfits(Long accountClientId) {
        Double total = profitRepository.sumTotalProfits(accountClientId);
        return total != null ? total : 0.0;
    }

    public Double getTodayProfit(Long accountClientId) {
        LocalDate today = LocalDate.now();
        Double todayProfit = profitRepository.sumTodayProfit(accountClientId, today);
        return todayProfit != null ? todayProfit : 0.0;
    }

    public Profit generateProfit(AccountClient accountClient, Double amount) {
        Profit profit = new Profit();
        profit.setAccountClient(accountClient);
        profit.setAmount(amount);
        profit.setDate(LocalDate.now());
        return profitRepository.save(profit);
    }

    @Scheduled(cron = "0 * * * * *") // Cada minuto para testing
    @Transactional
    public void generateDailyProfits() {
        List<AccountClient> accounts = accountClientRepository.findAll();

        for (AccountClient account : accounts) {
            Float balance = account.getBalance();

            float currentBalance = (balance != null) ? balance : 0.0f;

            float dailyProfit = currentBalance * ((float) ANNUAL_RATE / 365);

            if (dailyProfit > 0) {
                generateProfit(account, (double) dailyProfit);

                account.setBalance(currentBalance + dailyProfit);
                accountClientRepository.save(account);
            }
        }
    }

}
