package ro.axonsoft.eval.minibank.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.axonsoft.eval.minibank.service.BankService;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {
    private final BankService bankService;

    @GetMapping
    public Object getExchangeRates() {

        return bankService.getExchangeRates();
    }
}