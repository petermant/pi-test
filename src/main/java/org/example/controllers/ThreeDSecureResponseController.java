package org.example.controllers;

import org.example.client.ThreeDSecureClient;
import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.UUID;

@Controller
public class ThreeDSecureResponseController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired private ThreeDSecureClient threeDSecureClient;
    @Autowired private TransactionRepository transactionRepo;

    @PostMapping("/3DSecure/response")
    public String responseTo3DSecure(
            @RequestParam("PaRes") String paRes,
            @RequestParam("MD") String md,
            Model model) {
        logger.debug("3DSecure response received for transaction {}", md);

        Optional<Transaction> t = transactionRepo.findById(UUID.fromString(md));

        threeDSecureClient.fallbackComplete(t.get().getOpayoTransactionId(), paRes);

        return "3DSecureResponse";
    }
}
