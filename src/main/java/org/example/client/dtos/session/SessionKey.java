package org.example.client.dtos.session;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

public class SessionKey {
    private UUID merchantSessionKey;
    private Timestamp expiry;

    public UUID getMerchantSessionKey() {
        return merchantSessionKey;
    }

    public void setMerchantSessionKey(final UUID merchantSessionKey) {
        this.merchantSessionKey = merchantSessionKey;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(final Timestamp expiry) {
        this.expiry = expiry;
    }

    @Override
    public String toString() {
        return "SessionKey{" +
                "merchantSessionKey=" + merchantSessionKey +
                ", expiry=" + expiry +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionKey)) return false;
        final SessionKey that = (SessionKey) o;
        return Objects.equals(merchantSessionKey, that.merchantSessionKey) && Objects.equals(expiry, that.expiry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(merchantSessionKey, expiry);
    }
}
