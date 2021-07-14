package org.example.controllers;

import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Controller
public class ViewTransactionsController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired private TransactionRepository transactionRepo;

    @GetMapping("/transactions/all")
    public String allTransactions(Model model) {
        final Iterable<Transaction> allTxs = transactionRepo.findAll();
        model.addAttribute("allTransactions", allTxs);
        return "transactions/all-transactions";
    }

    @GetMapping("/transactions/one/{transactionId}")
    public String viewTransaction(@PathVariable("transactionId") UUID transactionId, Model model) {
        logger.debug("Searching for transaction {}", transactionId);

        final Optional<Transaction> tx = transactionRepo.findById(transactionId);

        if (tx.isPresent()) {
            model.addAttribute("tx", tx.get());

            return "transactions/one-transaction";
        } else {
            return "purchase-not-found";
        }
    }
}
