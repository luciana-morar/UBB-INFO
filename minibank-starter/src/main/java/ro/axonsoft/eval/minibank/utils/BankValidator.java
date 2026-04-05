package ro.axonsoft.eval.minibank.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.model.dto.AccountRequest;
import ro.axonsoft.eval.minibank.model.dto.TransferRequest;
import ro.axonsoft.eval.minibank.repository.AccountRepository;
import ro.axonsoft.eval.minibank.repository.TransferRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BankValidator {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;

    private static final Long BANK_ID = 1L;
    private static final BigDecimal SAVINGS_LIMIT_EUR = BigDecimal.valueOf(5000);

    public void validateCreateAccount(AccountRequest request) {
        if (request.getOwnerName() == null || request.getOwnerName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner name is required");
        }

        if (!IbanValidator.isValid(request.getIban())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IBAN");
        }

        String currency = request.getCurrency();
        if (!"RON".equals(currency) && !"EUR".equals(currency) &&
                !"USD".equals(currency) && !"GBP".equals(currency)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency must be RON, EUR, USD, or GBP");
        }

        String type = request.getAccountType();
        if (!"CHECKING".equals(type) && !"SAVINGS".equals(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account type must be CHECKING or SAVINGS");
        }

        if (accountRepository.existsByIban(request.getIban())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "IBAN already exists");
        }
    }

    public void validateTransfer(TransferRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }

        if (request.getSourceIban().equals(request.getTargetIban())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot transfer to the same account");
        }
    }

    public void validateAccountsExist(Account source, Account target) {
        if (source == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Source account not found");
        }
        if (target == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Target account not found");
        }
    }

    public void validateSufficientFunds(Account source, BigDecimal amount) {
        if (!BANK_ID.equals(source.getId())) {  // Banca are bani infinit
            if (source.getBalance().compareTo(amount) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
            }
        }
    }

    public void checkDailyLimit(Account source, BigDecimal amount) {
        if (!"SAVINGS".equals(source.getAccountType()) || BANK_ID.equals(source.getId())) {
            return;
        }

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        var todayTransfers = transferRepository.findBySourceIbanAndTransferDateBetween(
                source.getIban(), todayStart, tomorrowStart);

        BigDecimal totalTodayEur = BigDecimal.ZERO;
        for (var t : todayTransfers) {
            totalTodayEur = totalTodayEur.add(convertToEur(t.getAmount(), t.getCurrency()));
        }

        BigDecimal currentEur = convertToEur(amount, source.getCurrency());

        if (totalTodayEur.add(currentEur).compareTo(SAVINGS_LIMIT_EUR) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Daily limit of 5000 EUR exceeded for SAVINGS account");
        }
    }

    private BigDecimal convertToEur(BigDecimal amount, String currency) {
        // Ratele fixe din exchange-rates.yml
        if ("EUR".equals(currency)) return amount;
        if ("RON".equals(currency)) return amount.divide(BigDecimal.valueOf(4.97), 2, java.math.RoundingMode.HALF_EVEN);
        if ("USD".equals(currency)) return amount.multiply(BigDecimal.valueOf(4.56)).divide(BigDecimal.valueOf(4.97), 2, java.math.RoundingMode.HALF_EVEN);
        if ("GBP".equals(currency)) return amount.multiply(BigDecimal.valueOf(5.73)).divide(BigDecimal.valueOf(4.97), 2, java.math.RoundingMode.HALF_EVEN);
        return BigDecimal.ZERO;
    }
}

