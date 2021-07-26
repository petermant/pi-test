package org.example.controllers;

import org.example.client.MerchantSessionClient;
import org.example.client.TransactionClient;
import org.example.client.dtos.transaction.CredentialType;
import org.example.client.dtos.transaction.TransactionRequestDTO;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@Controller
public class CompletePurchaseController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String TRANSACTION_ID = "myTransactionId";
    private static final String CARD_ID = "card-identifier";

    @Autowired private TransactionRepository transactionRepo;
    @Autowired private PaymentService paymentService;
    @Autowired private TransactionClient transactionClient;
    @Autowired private MerchantSessionClient sessionClient;

    @Value("${org.example.3DSecureACSRedirectV1}") private String threeDSecureV1ResponseEndpoint;

    @Value("${opayo.server-uri}") private String serverUri;

    @PostMapping(path = "/complete-purchase", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String completePurchase(@RequestBody MultiValueMap<String, Object> body, Model model) {
        logger.debug("Complete purchase called with body {}", body);

        // todo other validation needed here as well, e.g. card-identifier missing, tx ID not a valid UUID, etc
        if (body.containsKey(TRANSACTION_ID) && body.containsKey(CARD_ID)) {
            UUID myTransactionId = UUID.fromString((String)body.getFirst(TRANSACTION_ID));
            UUID cardIdentifier = UUID.fromString((String)body.getFirst(CARD_ID));

            logger.debug("Purchase complete requested for transaction {}", myTransactionId);
            Optional<Transaction> t = transactionRepo.findById(myTransactionId);

            if (t.isPresent()) {
                CredentialType credentialType = new CredentialType("First", "CIT");
                return completePurchaseInternal(model, t.get().isDeferred() ? "Deferred" : "Payment", t.get(), cardIdentifier, credentialType, true, null);
            } else {
                return "purchase-not-found";
            }

        } else {
            return returnErrors(body, model);
        }
    }

    private String returnErrors(@RequestBody MultiValueMap<String, Object> body, Model model) {
        if (body.containsKey("card-identifier-http-code")) {
            model.addAttribute("errorCode", body.getFirst("card-identifier-http-code"));
            model.addAttribute("errorMessage", body.getFirst("card-identifier-error-message"));
            return "api-error";
        } else {
            model.addAttribute("errorCode", -1);
            model.addAttribute("errorMessage", "Something went wrong");
            return "api-error";
        }
    }

    @PostMapping(path = "/complete-purchase/re-use", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String completePurchaseReuse(@RequestBody MultiValueMap<String, Object> body, Model model) {
        logger.debug("Complete purchase with re-used card ID, called with body {}", body);

        // card ID is blank at this point
        if (body.containsKey(TRANSACTION_ID)) {
            UUID myTransactionId = UUID.fromString((String)body.getFirst(TRANSACTION_ID));

            logger.debug("Purchase complete with re-used card ID requested for transaction {}", myTransactionId);
            Optional<Transaction> t = transactionRepo.findById(myTransactionId);

            if (t.isPresent()) {
                CredentialType credentialType = new CredentialType("Subsequent", "CIT");
                return completePurchaseInternal(model, "Payment", t.get(), t.get().getCardIdentifier(), credentialType, null, true);
            } else {
                return "purchase-not-found";
            }

        } else return returnErrors(body, model);
    }

    @PostMapping(path = "/complete-purchase/repeat", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String completePurchaseRepeat(@RequestBody MultiValueMap<String, Object> body, Model model) {
        logger.debug("Complete purchase with repeat of transaction, called with body {}", body);

        if (body.containsKey(TRANSACTION_ID)) {
            UUID myTransactionId = UUID.fromString((String)body.getFirst(TRANSACTION_ID));

            logger.debug("Purchase complete for repeat of transaction {}", myTransactionId);
            Optional<Transaction> t = transactionRepo.findById(myTransactionId);

            if (t.isPresent()) {
                UUID merchantSessionKey = sessionClient.getSessionKey().getMerchantSessionKey();
                final Object amountStr = body.getFirst("amount");
                final long amount = AmountConverter.parseAmount(amountStr);

                CredentialType credentialType = new CredentialType("Subsequent", "MIT");
                final String type = "Repeat";
                Transaction repeatTrans = new Transaction(type, merchantSessionKey, amount, t.get());
                transactionRepo.save(repeatTrans);

                // todo should check what happens if pass in card identifier
                return completePurchaseInternal(model, type, repeatTrans, null /*t.get().getCardIdentifier()*/, credentialType, null, true);
            } else {
                return "purchase-not-found";
            }

        } else return returnErrors(body, model);
    }

    @PostMapping(path = "/complete-purchase/release/{transactionId}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String completePurchaseRelease(@PathVariable("transactionId") UUID transactionId, Model model) {
        logger.debug("Complete purchase with release of deferred transaction, called with transactionId {}", transactionId);

        Optional<Transaction> t = transactionRepo.findById(transactionId);

        if (t.isPresent()) {
            Transaction tx = t.get();
            if (tx.isDeferred() && tx.getTransactionType().equals("Deferred")) {
                HashMap<String, Object> response = paymentService.release(tx);

                if (response.containsKey("code") && response.containsKey("description")) {
                    model.addAttribute("errorCode", response.get("code"));
                    model.addAttribute("errorMessage", response.get("description"));
                    return "api-error";
                } else {
                    tx.setTransactionType("Released");
                    transactionRepo.save(tx);
                    logger.debug("Purchase released, redirecting to purchase completed.");

                    return "redirect:/purchase-completed?myTransactionId=" + tx.getId();
                }
            } else {
                model.addAttribute("errorCode", -1);
                model.addAttribute("errorMessage", "Transaction cannot be released unless it is deferred: " +t.get());
                return "api-error";
            }
        } else {
            return "purchase-not-found";
        }
    }

    @PostMapping(path = "/complete-purchase/refund/{transactionId}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String completePurchaseRefund(@PathVariable("transactionId") UUID transactionId, @RequestParam("amount") String refundAmountStr, Model model) {
        logger.debug("Complete purchase with refund, called with transactionId {}", transactionId);

        Optional<Transaction> t = transactionRepo.findById(transactionId);

        if (refundAmountStr.equals("")) {
            model.addAttribute("errorCode", -1);
            model.addAttribute("errorMessage", "Amount was blank");
            return "api-error";
        }

        if (t.isPresent()) {
            Transaction tx = t.get();

            long refundAmount = AmountConverter.parseAmount(refundAmountStr);
            Transaction refundTx = new Transaction("Refund", null, refundAmount, false);
            refundTx.setOpayoTransactionId(tx.getOpayoTransactionId());
            refundTx.setReferenceTransactionId(tx.getOpayoTransactionId());
            refundTx = transactionRepo.save(refundTx);

            HashMap<String, Object> response = paymentService.refund(refundTx);

            if (response.containsKey("code") && response.containsKey("description")) {
                model.addAttribute("errorCode", response.get("code"));
                model.addAttribute("errorMessage", response.get("description"));
                return "api-error";
            } else {
                refundTx.setTransactionType(tx.getAmount() == refundAmount ? "Refunded" : "Partially refunded");
                refundTx.setAmount(-refundAmount);
                transactionRepo.save(refundTx);
                logger.debug("Purchase refunded, redirecting to purchase completed.");

                return "redirect:/purchase-completed?myTransactionId=" + tx.getId();
            }
        } else {
            return "purchase-not-found";
        }
    }

    private String completePurchaseInternal(Model model, final String transactionType, Transaction t, UUID cardIdentifier, CredentialType credentialType, final Boolean save, final Boolean reusable) {
        final TransactionResponseDTO response = paymentService.complete(transactionType, t, cardIdentifier, credentialType, save, reusable);

        if ("2007".equals(response.getStatusCode()) && "3DAuth".equals(response.getStatus())) {
            logger.debug("Purchase completed with 3DAuth required {}, generating fallback form with ACS URL {}", response.getStatusCode(), response.getAcsUrl());

            model.addAttribute("response", response);
            model.addAttribute("threeDSResponseEndpoint", threeDSecureV1ResponseEndpoint);
            model.addAttribute(TRANSACTION_ID, t.getId());
            return "3DSecure/fallback-request-form";
        } else if ("2021".equals(response.getStatusCode()) && "3DAuth".equals(response.getStatus())) {
            logger.debug("Purchase completed with 3DAuth required {}, generating fallback form with ACS URL {}", response.getStatusCode(), response.getAcsUrl());

            model.addAttribute("response", response);
            model.addAttribute(TRANSACTION_ID, Base64.getEncoder().encodeToString(t.getId().toString().getBytes(StandardCharsets.UTF_8)));

            return "3DSecure/challenge-request-form";
        } else {
            logger.debug("Purchase completed with 3DSecure status {}, redirecting to purchase completed.", response.getThreeDSecure());
            return "redirect:/purchase-completed?myTransactionId=" + t.getId();
        }
    }

    @GetMapping("/purchase-completed")
    public String purchaseCompleted(@RequestParam("myTransactionId") UUID myTransactionId, Model model) {
        logger.debug("Purchase completed for transaction {}", myTransactionId);
        Optional<Transaction> t = transactionRepo.findById(myTransactionId);

        if (t.isPresent()) {
            try {
                logger.debug("Tried to get transaction, response was {}", transactionClient.getTransaction(t.get().getOpayoTransactionId()));
            } catch (Exception e) {
                logger.error("Couldn't get details with Opayo TX ID", e);
            }

            final TransactionResponseDTO responseDTO = transactionClient.getTransaction(t.get().getOpayoTransactionId());

            model.addAttribute("amount", AmountConverter.convertToPounds(t.get().getAmount()));
            model.addAttribute("opayoTransactionId", t.get().getOpayoTransactionId());
            model.addAttribute("avsStatus", responseDTO.getAvsCvcCheck() == null ? null : responseDTO.getAvsCvcCheck().getStatus());

            model.addAttribute("serverUri", serverUri);

            return "purchase-completed";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "entity not found");
        }
    }
}
