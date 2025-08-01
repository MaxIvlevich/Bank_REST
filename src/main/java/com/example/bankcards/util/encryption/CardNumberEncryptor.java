package com.example.bankcards.util.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Converter
@Slf4j
public class CardNumberEncryptor implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH_BYTE = 16;
    private final SecretKeySpec secretKey;

    public CardNumberEncryptor(@Value("${app.encryption.key}") String key) {
        this.secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

            byte[] encryptedValue = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            byte[] finalCipherBytes = new byte[iv.length + encryptedValue.length];
            System.arraycopy(iv, 0, finalCipherBytes, 0, iv.length);
            System.arraycopy(encryptedValue, 0, finalCipherBytes, iv.length, encryptedValue.length);

            return Base64.getEncoder().encodeToString(finalCipherBytes);

        } catch (Exception e) {
            log.error("Error during encryption: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to encrypt data", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            byte[] fullCipherBytes = Base64.getDecoder().decode(dbData);

            byte[] iv = new byte[IV_LENGTH_BYTE];
            System.arraycopy(fullCipherBytes, 0, iv, 0, iv.length);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            int encryptedValueLength = fullCipherBytes.length - IV_LENGTH_BYTE;
            byte[] encryptedValue = new byte[encryptedValueLength];
            System.arraycopy(fullCipherBytes, IV_LENGTH_BYTE, encryptedValue, 0, encryptedValueLength);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedValue);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Error during decryption: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to decrypt data", e);
        }

    }
}
