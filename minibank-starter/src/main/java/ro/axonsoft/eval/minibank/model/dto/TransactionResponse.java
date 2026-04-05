package ro.axonsoft.eval.minibank.model.dto;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private LocalDateTime timestamp;
    private String type;
    private BigDecimal amount;
    private String currency;
    private BigDecimal balanceAfter;
    private String counterpartyIban;
    private Long transferId;
}