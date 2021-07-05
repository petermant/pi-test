package org.example.client.dtos.transaction;

import java.util.Objects;
import java.util.UUID;

public class PaymentMethod {
    private final UUID merchantSessionKey;
    private final UUID cardIdentifier;

    public PaymentMethod(final UUID sessionKey, final UUID cardIdentifier) {
        this.merchantSessionKey = sessionKey;
        this.cardIdentifier = cardIdentifier;
    }

    public UUID getMerchantSessionKey() {
        return merchantSessionKey;
    }

    public UUID getCardIdentifier() {
        return cardIdentifier;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentMethod)) return false;
        final PaymentMethod that = (PaymentMethod) o;
        return Objects.equals(merchantSessionKey, that.merchantSessionKey) && Objects.equals(cardIdentifier, that.cardIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(merchantSessionKey, cardIdentifier);
    }
}
