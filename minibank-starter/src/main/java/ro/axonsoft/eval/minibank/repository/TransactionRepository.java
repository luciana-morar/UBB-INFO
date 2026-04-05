package ro.axonsoft.eval.minibank.repository;

import ro.axonsoft.eval.minibank.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByAccountIdOrderByTimestampAsc(Long accountId, Pageable pageable);
}
