package org.example.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Transaction {

    @Id @GeneratedValue
    private UUID id;
    private UUID sessionKey;
    private UUID cardIdentifier;
    private long amount;

    public Transaction() {}

    public Transaction(UUID sessionKey, long amount) {
        this.sessionKey = sessionKey;
        this.amount = amount;
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public UUID getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(final UUID sessionKey) {
        this.sessionKey = sessionKey;
    }

    public UUID getCardIdentifier() {
        return cardIdentifier;
    }

    public void setCardIdentifier(final UUID cardIdentifier) {
        this.cardIdentifier = cardIdentifier;
    }

    /**
     * Altho hibernate says you shouldn't use ID for equals - in this case, two transactions ARE equal if their ID's are equal, whilst all other attributes
     * are mutable e.g. a given transaction can use different session keys over time, and the card identifier is only stored once obtained.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        final Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
