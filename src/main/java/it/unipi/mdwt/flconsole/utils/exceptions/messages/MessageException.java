package it.unipi.mdwt.flconsole.utils.exceptions.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageException extends RuntimeException {
    private final MessageTypeErrorsEnum errorType;
}
