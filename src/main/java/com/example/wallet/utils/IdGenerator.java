package com.example.wallet.utils;
import java.security.SecureRandom;
import java.util.UUID;

import static org.hibernate.annotations.UuidGenerator.Style.RANDOM;

public class IdGenerator {
    /*
     * Genera un ID alfanumérico usando UUID, con una longitud máxima definida.
     * Si la longitud es mayor a 32, se concatenan múltiples UUIDs sin guiones.
     *
     * @param length la longitud deseada del ID
     * @return un ID alfanumérico aleatorio
     */
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

