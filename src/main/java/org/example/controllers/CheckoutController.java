package org.example.controllers;

import org.example.client.MerchantSessionClient;
import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.example.utils.AmountConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
public class CheckoutController {
    private static final String HTTP_CODE = "card-identifier-http-code";
    private static final String ERROR_CODE = "card-identifier-error-code";
    private static final String ERROR_MESSAGE = "card-identifier-error-message";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${opayo.server-uri}") private String serverUri;

    @Autowired private MerchantSessionClient sessionClient;
    @Autowired private TransactionRepository transactionRepo;

    @PostMapping(path = "/checkout", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String checkout(@RequestParam Map<String, String> allParams,
                           @RequestBody MultiValueMap<String, Object> body, Model model) {

        if (hasError(allParams)) {
            return renderError(allParams, model);
        } else {
            UUID merchantSessionKey = sessionClient.getSessionKey().getMerchantSessionKey();

            final Object amountStr = body.get("amount").get(0);
            final long amount = AmountConverter.parseAmount(amountStr);

            final boolean reusable = body.containsKey("reusable") && "on".equals(body.get("reusable").get(0));
            final boolean deferred = body.containsKey("deferred") && "on".equals(body.get("deferred").get(0));

            if (reusable && deferred) {
                model.addAttribute("errorCode", -1);
                model.addAttribute("errorMessage", "Cannot be deferred and reusable, go back and try again");
                return "api-error";
            }

            String type = deferred ? "Deferred" : "Payment";

            // todo this is currently a bit dumb, in that every time you refresh the page you get a new transaction
            Transaction t = new Transaction(type, merchantSessionKey, amount, reusable);
            t.setDeferred(deferred);
            t = transactionRepo.save(t);
            model.addAttribute("merchantSessionKey", merchantSessionKey);
            model.addAttribute("myTransactionId", t.getId());

            // added as the original amount, unparsed, i.e. show 10.00 not 1000
            model.addAttribute("amount", amountStr);
            model.addAttribute("serverUri", serverUri);

            logger.debug("Rendering checkout page with session key, for amount {}", amount);
            return "checkout/checkout";
        }
    }

    @PostMapping(path = "/checkout/re-use", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String checkoutReuse(@RequestParam Map<String, String> allParams,
                           @RequestBody MultiValueMap<String, Object> body, Model model) {

        if (hasError(allParams)) {
            return renderError(allParams, model);
        } else {
            UUID merchantSessionKey = sessionClient.getSessionKey().getMerchantSessionKey();

            final Object amountStr = body.get("amount").get(0);
            final long amount = AmountConverter.parseAmount(amountStr);

            final String txIdStr = body.get("txIdentifier").get(0).toString();
            final UUID txId = UUID.fromString(txIdStr);

            Transaction oldTx = transactionRepo.findById(txId).get();

            // todo this is currently a bit dumb, in that every time you refresh the page you get a new transaction
            Transaction t = new Transaction("Payment", merchantSessionKey, amount, true);
            t.setCardIdentifier(oldTx.getCardIdentifier());
            t = transactionRepo.save(t);

            model.addAttribute("merchantSessionKey", merchantSessionKey);
            model.addAttribute("myTransactionId", t.getId());
            model.addAttribute("cardId", t.getCardIdentifier());

            // added as the original amount, unparsed, i.e. show 10.00 not 1000
            model.addAttribute("amount", amountStr);
            model.addAttribute("serverUri", serverUri);

            logger.debug("Rendering card re-use checkout page with session key, for amount {}", amount);
            return "checkout/checkout-reuse";
        }
    }

    private boolean hasError(final Map<String, String> allParams) {
        try {
            if (allParams.containsKey(HTTP_CODE) && !allParams.get(HTTP_CODE).isEmpty()) {
                return Integer.parseInt(allParams.get(HTTP_CODE)) >= 400;
            } else {
                return false;
            }
        } catch (NumberFormatException nfe) {
            logger.error("Unrecognized value in HTTP code field");
            return true;
        }
    }

    private String renderError(@RequestParam Map<String, String> allParams, Model model) {
        logger.debug("Error rendering checkout page: {}", allParams);
        // todo handle different errors as per https://developer-eu.elavon.com/docs/opayo/spec/api-reference#section/Response-Codes/HTTP-Response-Codes
        //      Some of these should not be shown to end users - and some maybe routed differently
        model.addAttribute("errorCode", allParams.get(ERROR_CODE));
        model.addAttribute("errorMessage", allParams.get(ERROR_MESSAGE));
        return "api-error";
    }
}
