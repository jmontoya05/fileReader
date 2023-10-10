package com.reader.fileReader.exception;

public class InternalServerErrorException extends RuntimeException{
    public InternalServerErrorException() {
        super("Error de servidor");
    }

    public InternalServerErrorException(String message) {
        super(message);
    }
}
