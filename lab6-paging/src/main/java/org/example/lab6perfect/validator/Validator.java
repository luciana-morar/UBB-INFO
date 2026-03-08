package org.example.lab6perfect.validator;

/**
 * Interfața Strategy pentru validatori.
 * Fiecare validator știe să valideze un tip de entitate (User, Friendship, Message etc.)
 *Fiecare implementare definește strategia proprie de validare.
 */
public interface Validator<T> {
    void validate(T entity) throws ValidationException;
}
