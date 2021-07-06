package org.example.controllers;

import org.example.client.dtos.transaction.TransactionResponseDTO;
import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.example.services.PaymentService;
import org.example.utils.AmountConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Controller
public class CompletePurchaseController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String TRANSACTION_ID = "myTransactionId";
    private static final String CARD_ID = "card-identifier";

    @Autowired private TransactionRepository transactionRepo;
    @Autowired private PaymentService paymentService;

    @Value("${org.example.3DSecureACSRedirect}") private String threeDSecureResponseEndpoint;

    @PostMapping(path = "/complete-purchase", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String completePurchase(@RequestBody MultiValueMap<String, Object> body, Model model) {

        // todo other validation needed here as well, e.g. card-identifier missing, tx ID not a valid UUID, etc
        if (body.containsKey(TRANSACTION_ID)) {
            UUID myTransactionId = UUID.fromString((String)body.getFirst(TRANSACTION_ID));
            UUID cardIdentifier = UUID.fromString((String)body.getFirst(CARD_ID));

            logger.debug("Purchase complete requested for transaction {}", myTransactionId);
            Optional<Transaction> t = transactionRepo.findById(myTransactionId);

            if (t.isPresent()) {
                final TransactionResponseDTO response = paymentService.complete(t.get(), cardIdentifier);

                if ("2007".equals(response.getStatusCode()) && "3DAuth".equals(response.getStatus())) {
                    logger.debug("Purchase completed with 3DAuth required {}, generating fallback form with ACS URL {}", response.getStatusCode(), response.getAcsUrl());

                    model.addAttribute("response", response);
                    model.addAttribute("threeDSResponseEndpoint", threeDSecureResponseEndpoint);
                    model.addAttribute(TRANSACTION_ID, myTransactionId);
                    return "3DSecure/fallback-request-form";
                } else {
                    logger.debug("Purchase completed with 3DSecure status {}, redirecting to purchase completed.", response.getThreeDSecure());
                    return "redirect:/purchase-completed?myTransactionId=" + t.get().getId();
                }
            } else {
                return "purchase-not-found";
            }
        } else {
            model.addAttribute("errorCode", -1);
            model.addAttribute("errorMessage", "Transaction ID missing from request");
            return "api-error";
        }
    }

    @GetMapping("/purchase-completed")
    public String purchaseCompleted(@RequestParam("myTransactionId") UUID myTransactionId, Model model) {
        logger.debug("Purchase completed for transaction {}", myTransactionId);
        Optional<Transaction> t = transactionRepo.findById(myTransactionId);

        if (t.isPresent()) {
            model.addAttribute("amount", AmountConverter.convertToPounds(t.get().getAmount()));
            model.addAttribute("opayoTransactionId", t.get().getOpayoTransactionId());

            return "purchase-completed";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "entity not found");
        }
    }
}
