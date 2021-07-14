package org.example.client.dtos.transaction;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentMethod {
    private final UUID merchantSessionKey;
    private final UUID cardIdentifier;
    private final Boolean save;
    private final Boolean reusable;

    public PaymentMethod(final UUID sessionKey, final UUID cardIdentifier, Boolean save, Boolean reusable) {
        this.merchantSessionKey = sessionKey;
        this.cardIdentifier = cardIdentifier;
        this.save = save;
        this.reusable = reusable;
    }

    public UUID getMerchantSessionKey() {
        return merchantSessionKey;
    }

    public UUID getCardIdentifier() {
        return cardIdentifier;
    }

    public Boolean isSave() {
        return save;
    }

    public Boolean isReusable() {
        return reusable;
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
