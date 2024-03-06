package it.unipi.mdwt.flconsole.utils.exceptions.business;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class BusinessException extends RuntimeException {
    private final BusinessTypeErrorsEnum errorType;
}

