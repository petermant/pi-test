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
    private String transactionType;
    private UUID sessionKey;
    private UUID cardIdentifier;
    private long amount;
    private UUID opayoTransactionId;
    private boolean reusable = false;
    private boolean deferred = false;
    private UUID referenceTransactionId;

    public Transaction() {}

    public Transaction(String transactionType, UUID sessionKey, long amount, boolean reusable) {
        this.transactionType = transactionType;
        this.sessionKey = sessionKey;
        this.amount = amount;
        this.reusable = reusable;
    }

    public Transaction(String transactionType, UUID sessionKey, long amount, Transaction repeatTransaction) {
        this.transactionType = transactionType;
        this.sessionKey = sessionKey;
        this.amount = amount;
        this.referenceTransactionId = repeatTransaction.getOpayoTransactionId();
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
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

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getAmount() {
        return amount;
    }

    public UUID getOpayoTransactionId() {
        return opayoTransactionId;
    }

    public void setOpayoTransactionId(final UUID opayoTransactionId) {
        this.opayoTransactionId = opayoTransactionId;
    }

    public boolean isReusable() {
        return reusable;
    }

    public void setReusable(final boolean reusable) {
        this.reusable = reusable;
    }

    public UUID getReferenceTransactionId() {
        return referenceTransactionId;
    }

    public void setReferenceTransactionId(UUID referenceTransactionId) {
        this.referenceTransactionId = referenceTransactionId;
    }

    public void setDeferred(boolean deferred) {
        this.deferred = deferred;
    }

    public boolean isDeferred() {
        return deferred;
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

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", transactionType=" + transactionType +
                ", sessionKey=" + sessionKey +
                ", cardIdentifier=" + cardIdentifier +
                ", amount=" + amount +
                ", opayoTransactionId=" + opayoTransactionId +
                ", reusable=" + reusable +
                ", deferred=" + deferred +
                (referenceTransactionId == null ? "" : ", referenceTransactionId=" + referenceTransactionId) +
                '}';
    }
}
