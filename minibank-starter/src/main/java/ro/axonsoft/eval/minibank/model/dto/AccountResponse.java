package ro.axonsoft.eval.minibank.model.dto;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountResponse {
    private Long id;
    private String ownerName;
    private String iban;
    private String currency;
    private String accountType;
    private BigDecimal balance;
    private LocalDateTime createdAt;
}