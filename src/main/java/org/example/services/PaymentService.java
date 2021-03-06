package org.example.services;

import org.example.client.TransactionClient;
import org.example.client.dtos.transaction.CredentialType;
import org.example.client.dtos.transaction.TransactionResponseDTO;
import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;

@Service
public class PaymentService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired private TransactionRepository transactionRepo;
    @Autowired private TransactionClient transactionClient;

    public TransactionResponseDTO complete(final String transactionType, final Transaction t, final UUID cardIdentifier, CredentialType credentialType, final Boolean save, final Boolean reusable) {
        logger.debug("Completing purchase for transaction {} with card identifier {}", t.getId(), cardIdentifier);

        t.setCardIdentifier(cardIdentifier);

        // todo think about exception handling here ... want to store the card identifier as above, even if the payment API call fails

        TransactionResponseDTO responseDTO = transactionClient.requestTransaction(transactionType, t, credentialType, save, reusable);

        // todo also handle other outcomes here, e.g. not authorised, rejected etc - see table in step 2 here - https://developer-eu.elavon.com/docs/opayo/submit-payments-your-server

        logger.debug("Updating transaction {} with Opayo Tx Id {}", t.getId(), responseDTO.getTransactionId());
        logger.info("Transaction {} had status {}: {}", t.getId(), responseDTO.getStatusCode(), responseDTO.getStatus());
        t.setOpayoTransactionId(responseDTO.getTransactionId());

        transactionRepo.save(t);

        return responseDTO;
    }

    public HashMap<String, Object> release(Transaction tx) {
        logger.debug("Releasing transaction with ID {}", tx.getId());

        return transactionClient.releaseTransaction(tx);
    }

    public HashMap<String, Object> refund(Transaction tx) {
        logger.debug("Requesting refund for transaction {} for refund amount {}", tx.getId(), tx.getAmount());

        HashMap<String, Object> response = transactionClient.refund(tx);

        logger.debug("Successfully refunded {} from transaction {}", tx.getAmount(), tx.getId());

        return response;
    }
}
