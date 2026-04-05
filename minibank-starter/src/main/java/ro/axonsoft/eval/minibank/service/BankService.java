package ro.axonsoft.eval.minibank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ro.axonsoft.eval.minibank.config.ExchangeRateProperties;
import ro.axonsoft.eval.minibank.model.*;
import ro.axonsoft.eval.minibank.model.dto.*;
import ro.axonsoft.eval.minibank.repository.*;

import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.model.Transaction;
import ro.axonsoft.eval.minibank.model.Transfer;
import ro.axonsoft.eval.minibank.model.dto.*;
import ro.axonsoft.eval.minibank.repository.AccountRepository;
import ro.axonsoft.eval.minibank.repository.TransactionRepository;
import ro.axonsoft.eval.minibank.repository.TransferRepository;
import ro.axonsoft.eval.minibank.utils.BankValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class BankService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final TransactionRepository transactionRepository;
    private final ExchangeRateProperties exchangeRates;
    private final BankValidator validator;

    private static final Long BANK_ID = 1L;


    public AccountResponse createAccount(AccountRequest request) {
        validator.validateCreateAccount(request);

        Account account = new Account();
        account.setOwnerName(request.getOwnerName());
        account.setIban(request.getIban());
        account.setCurrency(request.getCurrency());
        account.setAccountType(request.getAccountType());
        account.setBalance(BigDecimal.ZERO);
        account.setCreatedAt(LocalDateTime.now());

        account = accountRepository.save(account);
        return mapToAccountResponse(account);
    }

    public AccountResponse getAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        return mapToAccountResponse(account);
    }

    public Page<AccountResponse> listAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(this::mapToAccountResponse);
    }

    public TransferResponse createTransfer(TransferRequest request) {
        validator.validateTransfer(request);

        Transfer existing = checkIdempotency(request.getIdempotencyKey());
        if (existing != null) {
            return mapToTransferResponse(existing);
        }

        Account source = accountRepository.findByIbanWithLock(request.getSourceIban())
                .orElse(null);
        Account target = accountRepository.findByIbanWithLock(request.getTargetIban())
                .orElse(null);
        validator.validateAccountsExist(source, target);

        validator.validateSufficientFunds(source, request.getAmount());
        validator.checkDailyLimit(source, request.getAmount());

        Transfer transfer = executeTransfer(source, target, request);

        return mapToTransferResponse(transfer);
    }

    public TransferResponse getTransfer(Long transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found"));
        return mapToTransferResponse(transfer);
    }

    public Page<TransferResponse> listTransfers(String iban, LocalDateTime fromDate,
                                                LocalDateTime toDate, Pageable pageable) {
        Page<Transfer> transfers;

        if (iban != null && fromDate != null && toDate != null) {
            transfers = transferRepository.findByIbanAndDateRange(iban, fromDate, toDate, pageable);
        } else if (iban != null) {
            transfers = transferRepository.findByIban(iban, pageable);
        } else if (fromDate != null && toDate != null) {
            transfers = transferRepository.findByDateRange(fromDate, toDate, pageable);
        } else {
            transfers = transferRepository.findAll(pageable);
        }

        return transfers.map(this::mapToTransferResponse);
    }

    public Page<TransactionResponse> getAccountTransactions(Long accountId, Pageable pageable) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }

        Page<Transaction> transactions = transactionRepository.findByAccountIdOrderByTimestampAsc(accountId, pageable);
        return transactions.map(this::mapToTransactionResponse);
    }

    public Object getExchangeRates() {
        return exchangeRates.getRates();
    }

    private Transfer checkIdempotency(String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
            return transferRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        }
        return null;
    }

    private Transfer executeTransfer(Account source, Account target, TransferRequest request) {
        // conversia valutară dacă e necesar
        BigDecimal exchangeRate = null;
        BigDecimal convertedAmount = null;

        if (!source.getCurrency().equals(target.getCurrency())) {
            BigDecimal sourceRate = exchangeRates.getRates().get(source.getCurrency());
            BigDecimal targetRate = exchangeRates.getRates().get(target.getCurrency());
            exchangeRate = sourceRate.divide(targetRate, 6, RoundingMode.HALF_EVEN);
            convertedAmount = request.getAmount().multiply(sourceRate)
                    .divide(targetRate, 2, RoundingMode.HALF_EVEN);
        }

        if (!BANK_ID.equals(source.getId())) {
            source.setBalance(source.getBalance().subtract(request.getAmount()));
            accountRepository.save(source);
        }

       BigDecimal creditAmount = convertedAmount != null ? convertedAmount : request.getAmount();
        target.setBalance(target.getBalance().add(creditAmount));
        accountRepository.save(target);

        Transfer transfer = new Transfer();
        transfer.setSourceIban(request.getSourceIban());
        transfer.setTargetIban(request.getTargetIban());
        transfer.setAmount(request.getAmount());
        transfer.setCurrency(source.getCurrency());
        transfer.setTargetCurrency(target.getCurrency());
        transfer.setExchangeRate(exchangeRate);
        transfer.setConvertedAmount(convertedAmount);
        transfer.setIdempotencyKey(request.getIdempotencyKey());
        transfer.setCreatedAt(LocalDateTime.now());
        transfer.setTransferDate(LocalDateTime.now());
        transfer = transferRepository.save(transfer);

        createTransactions(transfer, source, target, request.getAmount(), creditAmount);

        return transfer;
    }

    private void createTransactions(Transfer transfer, Account source, Account target,
                                    BigDecimal sourceAmount, BigDecimal targetAmount) {
        // Bancă -> User
        if (BANK_ID.equals(source.getId())) {
            createTransaction(target.getId(), transfer.getId(), "DEPOSIT",
                    targetAmount, target.getBalance(), null, target.getCurrency());
        }
        // User -> Bancă
        else if (BANK_ID.equals(target.getId())) {
            createTransaction(source.getId(), transfer.getId(), "WITHDRAWAL",
                    sourceAmount, source.getBalance(), null, source.getCurrency());
        }
        // User -> User
        else {
            createTransaction(source.getId(), transfer.getId(), "TRANSFER_OUT",
                    sourceAmount, source.getBalance(), target.getIban(), source.getCurrency());
            createTransaction(target.getId(), transfer.getId(), "TRANSFER_IN",
                    targetAmount, target.getBalance(), source.getIban(), target.getCurrency());
        }
    }

    private void createTransaction(Long accountId, Long transferId, String type,
                                   BigDecimal amount, BigDecimal balanceAfter,
                                   String counterpartyIban, String currency) {
        Transaction tx = new Transaction();
        tx.setAccountId(accountId);
        tx.setTransferId(transferId);
        tx.setTimestamp(LocalDateTime.now());
        tx.setType(type);
        tx.setAmount(amount);
        tx.setCurrency(currency);
        tx.setBalanceAfter(balanceAfter);
        tx.setCounterpartyIban(counterpartyIban);
        transactionRepository.save(tx);
    }

    private AccountResponse mapToAccountResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setOwnerName(account.getOwnerName());
        response.setIban(account.getIban());
        response.setCurrency(account.getCurrency());
        response.setAccountType(account.getAccountType());
        response.setBalance(account.getBalance());
        response.setCreatedAt(account.getCreatedAt());
        return response;
    }

    private TransferResponse mapToTransferResponse(Transfer transfer) {
        TransferResponse response = new TransferResponse();
        response.setId(transfer.getId());
        response.setSourceIban(transfer.getSourceIban());
        response.setTargetIban(transfer.getTargetIban());
        response.setAmount(transfer.getAmount());
        response.setCurrency(transfer.getCurrency());
        response.setTargetCurrency(transfer.getTargetCurrency());
        response.setExchangeRate(transfer.getExchangeRate());
        response.setConvertedAmount(transfer.getConvertedAmount());
        response.setIdempotencyKey(transfer.getIdempotencyKey());
        response.setCreatedAt(transfer.getCreatedAt());
        return response;
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setTimestamp(transaction.getTimestamp());
        response.setType(transaction.getType());
        response.setAmount(transaction.getAmount());
        response.setCurrency(transaction.getCurrency());
        response.setBalanceAfter(transaction.getBalanceAfter());
        response.setCounterpartyIban(transaction.getCounterpartyIban());
        response.setTransferId(transaction.getTransferId());
        return response;
    }
}
