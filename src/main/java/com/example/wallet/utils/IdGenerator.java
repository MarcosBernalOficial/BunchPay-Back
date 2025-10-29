package com.example.wallet.utils;

import java.security.SecureRandom;
import java.util.UUID;

public class IdGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate() {
        return generate(5);
    }

    public static String generate(int length) {
        StringBuilder id = new StringBuilder();

        while (id.length() < length) {
            String uuid = UUID.randomUUID().toString().replaceAll("-", ""); // 32 caracteres
            id.append(uuid);
        }

        return id.substring(0, length);
    }

    public static String generateNumericId(int length) {
        StringBuilder id = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int digit = RANDOM.nextInt(10);
            id.append(digit);
        }
        return id.toString();
    }
}
