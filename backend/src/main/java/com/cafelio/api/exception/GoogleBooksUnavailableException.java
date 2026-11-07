package com.cafelio.api.exception;

public class GoogleBooksUnavailableException extends RuntimeException {
    public GoogleBooksUnavailableException(String message) {
        super(message);
    }
}
