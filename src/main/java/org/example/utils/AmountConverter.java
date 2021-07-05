package org.example.utils;

import java.math.BigDecimal;

public class AmountConverter {

    public static long parseAmount(final Object amountStr) {
        if (amountStr instanceof String) {
            // todo this could do more error checking
            return new BigDecimal((String)amountStr).scaleByPowerOfTen(2).longValueExact();
        } else {
            throw new RuntimeException("Invalid format for amount. Type was " + amountStr.getClass());
        }
    }

    public static String convertToPounds(final long amount) {
        return new BigDecimal(amount).scaleByPowerOfTen(-2).toString();
    }
}
