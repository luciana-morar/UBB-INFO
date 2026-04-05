package ro.axonsoft.eval.minibank.model.dto;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransferResponse {
    private Long id;
    private String sourceIban;
    private String targetIban;
    private BigDecimal amount;
    private String currency;
    private String targetCurrency;
    private BigDecimal exchangeRate;
    private BigDecimal convertedAmount;
    private String idempotencyKey;
    private LocalDateTime createdAt;
}
