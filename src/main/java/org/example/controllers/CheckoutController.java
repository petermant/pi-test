package org.example.controllers;

import org.example.client.MerchantSessionClient;
import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

@Controller
public class CheckoutController {
    private static final String HTTP_CODE = "card-identifier-http-code";
    private static final String ERROR_CODE = "card-identifier-error-code";
    private static final String ERROR_MESSAGE = "card-identifier-error-message";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired private MerchantSessionClient sessionClient;
    @Autowired private TransactionRepository transactionRepo;

    @PostMapping(path = "/checkout", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String checkout(@RequestParam Map<String, String> allParams,
                           @RequestBody MultiValueMap<String, Object> body,
                           Model model) {

        if (hasError(allParams)) {
            logger.debug("Error rendering checkout page: {}", allParams);
            // todo handle different errors as per https://developer-eu.elavon.com/docs/opayo/spec/api-reference#section/Response-Codes/HTTP-Response-Codes
            //      Some of these should not be shown to end users - and some maybe routed differently
            model.addAttribute("errorCode", allParams.get(ERROR_CODE));
            model.addAttribute("errorMessage", allParams.get(ERROR_MESSAGE));
            return "api-error";
        } else {
            UUID merchantSessionKey = sessionClient.getSessionKey().getMerchantSessionKey();

            // todo this is currently a bit dumb, in that every time you refresh the page you get a new transaction
            Transaction t = transactionRepo.save(new Transaction(merchantSessionKey));
            model.addAttribute("merchantSessionKey", merchantSessionKey);
            model.addAttribute("transactionId", t.getId());

            final Object amount = body.get("amount").get(0);
            model.addAttribute("amount", amount);
            logger.debug("Rendering checkout page with session key");
            return "checkout";
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
}
