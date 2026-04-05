package ro.axonsoft.eval.minibank.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "transfers", indexes = {
        @Index(name = "idx_idempotency_key", columnList = "idempotencyKey"),
        @Index(name = "idx_source_iban", columnList = "sourceIban"),
        @Index(name = "idx_target_iban", columnList = "targetIban"),
        @Index(name = "idx_transfer_date", columnList = "transferDate")
})
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sourceIban;

    @Column(nullable = false)
    private String targetIban;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    private String targetCurrency;

    @Column(precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Column(precision = 19, scale = 2)
    private BigDecimal convertedAmount;

    @Column(unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime transferDate;
}