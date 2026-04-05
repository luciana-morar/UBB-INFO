package ro.axonsoft.eval.minibank.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.axonsoft.eval.minibank.model.dto.TransferRequest;
import ro.axonsoft.eval.minibank.model.dto.TransferResponse;
import ro.axonsoft.eval.minibank.service.BankService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {
    private final BankService bankService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse createTransfer(@RequestBody TransferRequest request) {
        return bankService.createTransfer(request);
    }

    @GetMapping("/{transferId}")
    public TransferResponse getTransfer(@PathVariable Long transferId) {
        return bankService.getTransfer(transferId);
    }

    @GetMapping
    public Page<TransferResponse> listTransfers(
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return bankService.listTransfers(iban, fromDate, toDate, PageRequest.of(page, size));
    }
}