package org.example.lab6perfect.validator;
import org.example.lab6perfect.domain.Message;

public class MessageValidator implements Validator<Message> {
    @Override
    public void validate(Message message) throws ValidationException {
        if(message.getSender()==null || message.getReceiver()==null)
            throw new ValidationException("Sender or Receiver is null.");
        if(message.getContent()==null)
            throw new ValidationException("Content is null.");
        if(message.getSender().equals(message.getReceiver()))
            throw new ValidationException("Sender and Receiver are the same.");
    }
}
