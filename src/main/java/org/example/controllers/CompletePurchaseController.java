package org.example.controllers;

import org.example.client.dtos.transaction.TransactionResponseDTO;
import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.example.services.PaymentService;
import org.example.utils.AmountConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;
import java.util.UUID;

@Controller
public class CompletePurchaseController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String TRANSACTION_ID = "transactionId";
    private static final String CARD_ID = "card-identifier";

    @Autowired private TransactionRepository transactionRepo;
    @Autowired private PaymentService paymentService;

    @PostMapping(path = "/complete-purchase", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String completePurchase(@RequestBody MultiValueMap<String, Object> body,
                                   Model model) {

        // todo other validation needed here as well, e.g. card-identifier missing, tx ID not a valid UUID, etc
        if (body.containsKey(TRANSACTION_ID)) {
            UUID transactionId = UUID.fromString((String)body.getFirst(TRANSACTION_ID));
            UUID cardIdentifier = UUID.fromString((String)body.getFirst(CARD_ID));

            logger.debug("Purchase complete requested for transaction {}", transactionId);
            Optional<Transaction> t = transactionRepo.findById(transactionId);

            if (t.isPresent()) {
                final TransactionResponseDTO response = paymentService.complete(t.get(), cardIdentifier);

                // todo I've ended up calling all the ID's by different keys to the Opayo API, by mistake ... maybe make them the same
                model.addAttribute("amount", AmountConverter.convertToPounds(t.get().getAmount()));
                model.addAttribute("paymentId", response.getTransactionId());

                return "purchase-completed";
            } else {
                return "purchase-not-found";
            }
        } else {
            model.addAttribute("errorCode", -1);
            model.addAttribute("errorMessage", "Transaction ID missing from request");
            return "api-error";
        }
    }
}
