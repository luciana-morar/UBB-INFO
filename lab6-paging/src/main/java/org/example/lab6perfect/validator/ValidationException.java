package org.example.lab6perfect.validator;

/**
 * Excepție personalizată pentru erori de validare.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
