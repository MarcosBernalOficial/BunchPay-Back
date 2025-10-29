package com.example.wallet.config;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtil {

    private static final String SECRET_KEY = "miClaveSecreta12";

    public static String encrypt(String input){
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(input.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e){
            throw new RuntimeException("Error al encriptar: " + e.getMessage());
        }
    }

    public static String decrypt(String encrypted){
        try{
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            return new String(cipher.doFinal(decoded));
        } catch (Exception e){
            throw new RuntimeException("Error al desencriptar: " + e.getMessage());
        }
    }
}
