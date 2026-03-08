package org.example.lab6perfect.validator;

import org.example.lab6perfect.domain.Friendship;

public class FriendshipValidator implements Validator<Friendship> {

    @Override
    public void validate(Friendship friendship) throws ValidationException {
        if(friendship.getUser1()==null || friendship.getUser2()==null)
            throw new ValidationException("Prietenii sunt nuli.");
        if(friendship.getUser1().equals(friendship.getUser2()))
            throw new ValidationException("Un utilizator nu poate fi prieten cu el insusi.");
    }
}
