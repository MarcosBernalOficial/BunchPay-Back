package com.example.wallet.controllers.exceptions;

public class RepeatedPasswordException extends RuntimeException {
    public RepeatedPasswordException(String message) {
        super(message);
    }
}
