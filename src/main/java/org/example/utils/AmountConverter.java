package org.example.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class AmountConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmountConverter.class);

    public static long parseAmount(final Object amountStr) {
        if (amountStr instanceof String && !((String) amountStr).isEmpty()) {
            // todo this could do more error checking
            long newValue = new BigDecimal((String)amountStr).scaleByPowerOfTen(2).longValueExact();
            LOGGER.debug("Converted string '{}' to long value {}", amountStr, newValue);
            return newValue;
        } else {
            throw new RuntimeException("Invalid format for amount. Amount was " + amountStr);
        }
    }

    public static String convertToPounds(final long amount) {
        return new BigDecimal(amount).scaleByPowerOfTen(-2).toString();
    }
}
