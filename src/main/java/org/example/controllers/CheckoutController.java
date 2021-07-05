package org.example.controllers;

import org.example.client.MerchantSessionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class CheckoutController {
    private static final String HTTP_CODE = "card-identifier-http-code";
    private static final String ERROR_CODE = "card-identifier-error-code";
    private static final String ERROR_MESSAGE = "card-identifier-error-message";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired private MerchantSessionClient sessionClient;

    @GetMapping("/checkout")
    public String checkout(@RequestParam Map<String,String> allParams,
                           Model model) {
        //
        if (hasError(allParams)) {
            // todo handle different errors as per https://developer-eu.elavon.com/docs/opayo/spec/api-reference#section/Response-Codes/HTTP-Response-Codes
            //      Some of these should not be shown to end users - and some maybe routed differently
            model.addAttribute("errorCode", allParams.get(ERROR_CODE));
            model.addAttribute("errorMessage", allParams.get(ERROR_MESSAGE));
            return "api-error";
        } else {
            model.addAttribute("merchantSessionKey", sessionClient.getSessionKey().getMerchantSessionKey());
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
