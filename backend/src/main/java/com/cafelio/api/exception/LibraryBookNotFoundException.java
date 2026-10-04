package com.cafelio.api.exception;

public class LibraryBookNotFoundException extends RuntimeException{
    public LibraryBookNotFoundException(String message){
        super(message);
    }
}
