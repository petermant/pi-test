package org.example.client.dtos.transaction;

import org.example.model.Transaction;
import org.example.utils.AmountConverter;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class TransactionRequestDTO {
    private final String transactionType = "Payment";
    private final Map<String, PaymentMethod> paymentMethod;
    private final UUID vendorTxCode;
    private final long amount;
    private final String currency = "GBP";
    private final String description;
    private final String customerFirstName = "Pete";
    private final String customerLastName = "Mantell";
    private final BillingAddress billingAddress = new BillingAddress("1 Main Street", "Some city", "AB1 CD2", "GB");
    private final StrongCustomerAuthentication strongCustomerAuthentication;

    public TransactionRequestDTO(Transaction t) {
        vendorTxCode = t.getId();
        amount = t.getAmount();
        // todo hard coding to 'card' payment method for now - what others are available?
        paymentMethod = Collections.singletonMap("card", new PaymentMethod(t.getSessionKey(), t.getCardIdentifier()));
        description = "Pete's PI integration demo app tx for £" + AmountConverter.convertToPounds(amount);
        strongCustomerAuthentication = new StrongCustomerAuthentication();
    }

    public String getTransactionType() {
        return transactionType;
    }

    public Map<String, PaymentMethod> getPaymentMethod() {
        return paymentMethod;
    }

    public UUID getVendorTxCode() {
        return vendorTxCode;
    }

    public long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public String getCustomerFirstName() {
        return customerFirstName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public BillingAddress getBillingAddress() {
        return billingAddress;
    }

    public StrongCustomerAuthentication getStrongCustomerAuthentication() {
        return strongCustomerAuthentication;
    }
}
