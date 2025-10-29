package com.example.wallet.controllers.exceptions;

public class DniAlreadyExistException extends RuntimeException {
    public DniAlreadyExistException(String message) {
        super(message);
    }
}

