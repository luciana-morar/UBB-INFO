package ro.axonsoft.eval.minibank.utils;

public class IbanValidator {

    public static boolean isValid(String iban) {
        if (iban == null || iban.trim().isEmpty()) {
            return false;
        }

        iban = iban.replaceAll("\\s+", "").toUpperCase();

        if (iban.length() < 24) {
            return false;
        }

        if (!iban.startsWith("RO")) {
            return false;
        }

        if (iban.matches("[A-Z0-9]+")) {
            return true;
        }

        return false;
    }
}
