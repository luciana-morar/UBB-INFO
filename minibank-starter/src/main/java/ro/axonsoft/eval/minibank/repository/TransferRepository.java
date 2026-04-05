package ro.axonsoft.eval.minibank.repository;

import ro.axonsoft.eval.minibank.model.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT t FROM Transfer t WHERE t.sourceIban = :iban OR t.targetIban = :iban")
    Page<Transfer> findByIban(@Param("iban") String iban, Pageable pageable);

    @Query("SELECT t FROM Transfer t WHERE t.transferDate BETWEEN :from AND :to")
    Page<Transfer> findByDateRange(@Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to,
                                   Pageable pageable);

    @Query("SELECT t FROM Transfer t WHERE (t.sourceIban = :iban OR t.targetIban = :iban) " +
            "AND t.transferDate BETWEEN :from AND :to")
    Page<Transfer> findByIbanAndDateRange(@Param("iban") String iban,
                                          @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to,
                                          Pageable pageable);

    @Query("SELECT t FROM Transfer t WHERE t.sourceIban = :iban AND t.transferDate BETWEEN :start AND :end")
    java.util.List<Transfer> findBySourceIbanAndTransferDateBetween(@Param("iban") String iban,
                                                                    @Param("start") LocalDateTime start,
                                                                    @Param("end") LocalDateTime end);
}
