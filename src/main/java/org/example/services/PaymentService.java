package org.example.services;

import org.example.client.TransactionClient;
import org.example.client.dtos.transaction.TransactionResponseDTO;
import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired private TransactionRepository transactionRepo;
    @Autowired private TransactionClient transactionClient;

    public TransactionResponseDTO complete(final Transaction t, final UUID cardIdentifier) {
        logger.debug("Completing purchase for transaction {} with card identifier {}", t.getId(), cardIdentifier);

        t.setCardIdentifier(cardIdentifier);

        // todo think about exception handling here ... want to store the card identifier as above, even if the payment API call fails

        TransactionResponseDTO responseDTO = transactionClient.requestTransaction(t);

        // todo also handle other outcomes here, e.g. not authorised, rejected etc - see table in step 2 here - https://developer-eu.elavon.com/docs/opayo/submit-payments-your-server

        logger.debug("Updating transaction {} with Opayo Tx Id {}", t.getId(), responseDTO.getTransactionId());
        logger.info("Transaction {} had status {}: {}", t.getId(), responseDTO.getStatusCode(), responseDTO.getStatus());
        t.setOpayoTransactionId(responseDTO.getTransactionId());

        transactionRepo.save(t);

        return responseDTO;
    }
}
