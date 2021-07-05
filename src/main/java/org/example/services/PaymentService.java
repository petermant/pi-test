package org.example.services;

import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired private TransactionRepository transactionRepo;

    public void complete(final Transaction t, final UUID cardIdentifier) {
        logger.debug("Completing purchase for transaction {} with card identifier {}", t.getId(), cardIdentifier);

        t.setCardIdentifier(cardIdentifier);
        transactionRepo.save(t);
    }
}
