package it.unipi.mdwt.flconsole.utils.exceptions.dao;

import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessTypeErrorsEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class DaoException extends RuntimeException {
    private final DaoTypeErrorsEnum errorType;
}

