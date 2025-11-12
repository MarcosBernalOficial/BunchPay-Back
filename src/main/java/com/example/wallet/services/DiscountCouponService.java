package com.example.wallet.services;

import com.example.wallet.model.implementations.AccountClient;
import com.example.wallet.model.implementations.DiscountCoupon;
import com.example.wallet.repository.AccountClientRepository;
import com.example.wallet.repository.DiscountCouponRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiscountCouponService {

    private final DiscountCouponRepository discountCouponRepository;
    private final AccountClientRepository accountClientRepository;

    private static final List<String> COMPANIES = List.of(
            "McDonalds", "BurgerKing", "Mostaza", "Starbucks", "Kevingston", "Luccianos",
            "Dexter", "Adidas", "Nike", "Under Armour", "Crocs", "Camaron Brujo", "Stanley");

    private final Random random = new Random();

    @Transactional
    public List<DiscountCoupon> getActiveCoupons(Long accountClientId) {
        List<DiscountCoupon> active = discountCouponRepository
                .findByAccountClientIdAndExpirationDateAfter(accountClientId, LocalDate.now());
        if (active.isEmpty()) {
            // Generar inmediatamente si la cuenta aún no tiene beneficios activos
            accountClientRepository.findById(accountClientId).ifPresent(this::generateCouponsForAccount);
            active = discountCouponRepository.findByAccountClientIdAndExpirationDateAfter(accountClientId,
                    LocalDate.now());
        }
        return active;
    }

    @Transactional
    @Scheduled(cron = "2 50 0 * * *") // 00:50 AM -> generar nuevos si faltan
    public void generateCouponsForAllUsers() {
        accountClientRepository.findAll().forEach(this::generateCouponsForAccount);
    }

    @Transactional
    private void generateCouponsForAccount(AccountClient account) {
        List<DiscountCoupon> activeCoupons = discountCouponRepository
                .findByAccountClientIdAndExpirationDateAfter(account.getId(), LocalDate.now());
        // Garantizar siempre al menos 3 cupones
        while (activeCoupons.size() < 3) {
            String company = getRandomCompanyExcluding(activeCoupons);
            int discountPercent = randomBetween(5, 45);
            String code = generateUniqueCode();
            LocalDate expirationDate = LocalDate.now().plusDays(randomBetween(2, 7));

            DiscountCoupon coupon = new DiscountCoupon();
            coupon.setAccountClient(account);
            coupon.setCompany(company);
            coupon.setDiscountPercent(discountPercent);
            coupon.setCode(code);
            coupon.setExpirationDate(expirationDate);

            discountCouponRepository.save(coupon);
            activeCoupons.add(coupon);
        }
    }

    @Transactional
    @Scheduled(cron = "5 50 0 * * *") // 00:55 AM -> refrescar porcentaje diario
    public void refreshDailyDiscountPercents() {
        List<DiscountCoupon> allActiveToday = discountCouponRepository.findAll().stream()
                .filter(c -> !c.getExpirationDate().isBefore(LocalDate.now()))
                .toList();
        for (DiscountCoupon coupon : allActiveToday) {
            coupon.setDiscountPercent(randomBetween(5, 45));
            discountCouponRepository.save(coupon);
        }
    }

    @Transactional
    @Scheduled(cron = "0 2 0 * * *") // 5:30 AM
    public void cleanExpiredCoupons() {
        List<DiscountCoupon> allCoupons = discountCouponRepository.findAll();

        for (DiscountCoupon coupon : allCoupons) {
            if (coupon.getExpirationDate().isBefore(LocalDate.now())) {
                discountCouponRepository.delete(coupon);
            }
        }
    }

    @Transactional
    private String getRandomCompanyExcluding(List<DiscountCoupon> activeCoupons) {
        List<String> availableCompanies = COMPANIES.stream()
                .filter(company -> activeCoupons.stream().noneMatch(c -> c.getCompany().equals(company)))
                .toList();

        if (availableCompanies.isEmpty()) {
            return COMPANIES.get(random.nextInt(COMPANIES.size()));
        } else {
            return availableCompanies.get(random.nextInt(availableCompanies.size()));
        }
    }

    private int randomBetween(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    private String generateUniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
