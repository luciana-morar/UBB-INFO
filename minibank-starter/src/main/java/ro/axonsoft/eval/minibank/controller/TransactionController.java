package ro.axonsoft.eval.minibank.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ro.axonsoft.eval.minibank.model.dto.TransactionResponse;
import ro.axonsoft.eval.minibank.service.BankService;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class TransactionController {
    private final BankService bankService;

    @GetMapping("/{accountId}/transactions")
    public Page<TransactionResponse> getTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return bankService.getAccountTransactions(accountId, PageRequest.of(page, size));
    }
}