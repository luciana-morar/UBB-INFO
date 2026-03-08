package org.example.lab6perfect.validator;

import org.example.lab6perfect.domain.Persoana;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.domain.duck.Duck;

public class UserValidator implements Validator<User> {
    @Override
    public void validate(User user) throws ValidationException {
        StringBuilder errors = new StringBuilder();

        if(user.getUsername() == null || user.getUsername().isBlank())
            errors.append("Username invalid.\n");
        if(user.getEmail()== null || !user.getEmail().contains("@"))
            errors.append("Email invalid.\n");
        if(user.getPassword() == null || user.getPassword().length()<3)
            errors.append("Parola invalida! Trebuie sa aiba minim 3 caractere.\n");

        if(user instanceof Duck duck){
            if(duck.getViteza()<=0)
                errors.append("Viteza ratei invalida! Trebuie sa fie posizitiva.\n");
            if(duck.getRezistenta()<=0)
                errors.append("Rezistenta ratei invalida!Trebuie sa fie pozitiva.\n");
        }

        if(user instanceof Persoana persoana){
            if(persoana.getNume()==null || persoana.getNume().isBlank())
                errors.append("Nume invalid.\n");
            if(persoana.getPrenume()==null || persoana.getPrenume().isBlank())
                errors.append("Prenume invalid.\n");

        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toString());
        }
    }
}
