package org.lear.userservice.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum BusinessErrorCodes {
    NO_CODE(0, NOT_IMPLEMENTED, "No Code"),
    INCORRECT_CURRENT_PASSWORD(300, BAD_REQUEST, "Current password incorrect"),
    NEW_PASSWORD_DOES_NOT_MATCH(301, BAD_REQUEST, "New password not matching"),
    ACCOUNT_DISABLED(303, FORBIDDEN, "User account disabled"),
    BAD_CREDENTIALS(304, FORBIDDEN, "login or password incorrect"),
    ACCOUNT_LOCKED(302, FORBIDDEN, "User account locked"),
    ACCOUNT_ALREADY_EXIST(305, BAD_REQUEST, "email already exist");

    private final int code;
    private final String description;
    private final HttpStatus httpStatus;

    BusinessErrorCodes(int code, HttpStatus httpStatus, String description) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.description = description;

    }
}
