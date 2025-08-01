package com.example.bankcards.util.masking;

import lombok.experimental.UtilityClass;

/**
 * A utility class for masking sensitive information, such as card numbers.
 * The methods in this class are static and the class cannot be instantiated.
 */
@UtilityClass
public class CardMaskingUtil {
    private static final String MASKED_PREFIX = "**** **** **** ";

    /**
     * Masks a 16-digit card number, showing only the last four digits.
     * The output format is "**** **** **** 1234".
     * <p>
     * This method assumes the input is a non-null, 16-character string.
     * Input validation should be handled by the caller.
     *
     * @param cardNumber The full 16-digit card number to be masked.
     * @return A masked string representation of the card number. Returns null if the input is null.
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return null;
        }
        String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
        return MASKED_PREFIX + lastFourDigits;
    }
}
