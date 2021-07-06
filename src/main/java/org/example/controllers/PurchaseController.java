package org.example.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class PurchaseController {

    @GetMapping("/")
    public String redirectHomeToPurchase() {
        return "redirect:/purchase";
    }

    @GetMapping("/purchase")
    public String purchase(@RequestParam Map<String,String> allParams,
                           Model model) {
        return "purchase";
    }
}
