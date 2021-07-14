package org.example.controllers;

import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
public class PurchaseController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired private TransactionRepository transactionRepo;

    @GetMapping("/")
    public String redirectHomeToPurchase() {
        return "redirect:/purchase";
    }

    @GetMapping("/purchase")
    public String purchase(@RequestParam Map<String,String> allParams,
                           Model model) {
        return "purchase/purchase";
    }

    @GetMapping("/purchase/re-use/{transactionId}")
    public String purchase(@PathVariable("transactionId") UUID transactionId,
                           Model model) {
        Optional<Transaction> tx = transactionRepo.findById(transactionId);

        if (tx.isPresent()) {
            model.addAttribute("tx", tx.get());
            return "purchase/purchase-reuse";
        } else {
            return "purchase-not-found";
        }
    }
}
