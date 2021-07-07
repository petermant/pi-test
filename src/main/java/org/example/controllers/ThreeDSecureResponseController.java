package org.example.controllers;

import org.example.client.ThreeDSecureClient;
import org.example.client.dtos.transaction.TransactionResponseDTO;
import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.example.utils.AmountConverter;
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

    @PostMapping("/3DSecure/response/v1")
    public String responseTo3DSecure(
            @RequestParam("PaRes") String paRes,
            @RequestParam("MD") String md,
            Model model) {
        logger.debug("3DSecure response received for transaction {}", md);

        Optional<Transaction> t = transactionRepo.findById(UUID.fromString(md));

        TransactionResponseDTO responseDTO = threeDSecureClient.fallbackComplete(t.get().getOpayoTransactionId(), paRes);

        model.addAttribute("amount", AmountConverter.convertToPounds(t.get().getAmount()));
        model.addAttribute("opayoTransactionId", t.get().getOpayoTransactionId());
        model.addAttribute("avsStatus", responseDTO.getAvsCvcCheck().getStatus());

        return "3DSecure/3d-secure-v1-response";
    }
}
