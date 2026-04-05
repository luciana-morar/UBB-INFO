package ro.axonsoft.eval.minibank.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.axonsoft.eval.minibank.model.dto.AccountRequest;
import ro.axonsoft.eval.minibank.model.dto.AccountResponse;
import ro.axonsoft.eval.minibank.service.BankService;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final BankService bankService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@RequestBody AccountRequest request) {
        return bankService.createAccount(request);
    }

    @GetMapping("/{accountId}")
    public AccountResponse getAccount(@PathVariable Long accountId) {
        return bankService.getAccount(accountId);
    }

    @GetMapping
    public Page<AccountResponse> listAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return bankService.listAccounts(PageRequest.of(page, size));
    }
}
