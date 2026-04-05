package ro.axonsoft.eval.minibank.model.dto;

import lombok.Data;

@Data
public class AccountRequest {
    private String ownerName;
    private String iban;
    private String currency;
    private String accountType;
}
