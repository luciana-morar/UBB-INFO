package ro.axonsoft.eval.minibank.model.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    private String sourceIban;
    private String targetIban;
    private BigDecimal amount;
    private String idempotencyKey;
}
