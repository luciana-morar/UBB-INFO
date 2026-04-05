package ro.axonsoft.eval.minibank.repository;

import ro.axonsoft.eval.minibank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByIban(String iban);
    boolean existsByIban(String iban);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.iban = :iban")
    Optional<Account> findByIbanWithLock(@Param("iban") String iban);
}
