package vn.sun.membermanagementsystem.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN.value());
    }

    public ForbiddenException() {
        super("Access denied", HttpStatus.FORBIDDEN.value());
    }
}
