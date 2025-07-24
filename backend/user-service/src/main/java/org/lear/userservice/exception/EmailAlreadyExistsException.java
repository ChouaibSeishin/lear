package org.lear.userservice.exception;

import lombok.Getter;
import org.lear.userservice.handler.BusinessErrorCodes;


@Getter
public class EmailAlreadyExistsException extends RuntimeException {
    private final BusinessErrorCodes errorCode;

    public EmailAlreadyExistsException(BusinessErrorCodes errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
