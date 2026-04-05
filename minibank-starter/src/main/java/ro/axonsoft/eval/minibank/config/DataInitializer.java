package ro.axonsoft.eval.minibank.config;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.repository.AccountRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final AccountRepository accountRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        if (accountRepository.count() == 0) {
            // SQL direct - ignoră complet orice problemă cu version
            entityManager.createNativeQuery(
                    "INSERT INTO accounts (id, owner_name, iban, currency, account_type, balance, created_at) " +
                            "VALUES (1, 'SYSTEM', 'RO49AAAA1B31007593840000', 'RON', 'CHECKING', 999999999.99, CURRENT_TIMESTAMP)"
            ).executeUpdate();
        }
    }
}