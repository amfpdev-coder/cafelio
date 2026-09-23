package com.cafelio.api.exception;

public class LibraryBookAlreadyExistsException extends RuntimeException{

    public LibraryBookAlreadyExistsException(String message){
        super(message);
    }
}
