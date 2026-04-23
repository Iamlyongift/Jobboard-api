package com.jobboard.exceptions;


// each one looks like this pattern
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}