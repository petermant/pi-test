package org.example.client.dtos.transaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionResponseDTO {
    private UUID transactionId;
    private String transactionType;
    private String status;
    private String statusCode;
    private String statusDetail;
    private String retrievalReference;

    // todo add other attributes when they are useful - from Step 2 here: https://developer-eu.elavon.com/docs/opayo/submit-payments-your-server

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(final UUID transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(final String transactionType) {
        this.transactionType = transactionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(final String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusDetail() {
        return statusDetail;
    }

    public void setStatusDetail(final String statusDetail) {
        this.statusDetail = statusDetail;
    }

    public String getRetrievalReference() {
        return retrievalReference;
    }

    public void setRetrievalReference(final String retrievalReference) {
        this.retrievalReference = retrievalReference;
    }
}
