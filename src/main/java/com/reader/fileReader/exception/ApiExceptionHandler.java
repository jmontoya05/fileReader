package com.reader.fileReader.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;
@ControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(BadRequestsException.class)
    public ResponseEntity<Object> badRequestExeption(BadRequestsException e){
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ExceptionModel exceptionModel = new ExceptionModel(
                e.getMessage(),
                httpStatus,
                ZonedDateTime.now(ZoneId.of("America/Bogota"))
                );
        return new ResponseEntity<>(exceptionModel, httpStatus);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<Object> internalServerErrorException(InternalServerErrorException e){
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionModel exceptionModel = new ExceptionModel(
                e.getMessage(),
                httpStatus,
                ZonedDateTime.now(ZoneId.of("America/Bogota"))
        );
        return new ResponseEntity<>(exceptionModel, httpStatus);
    }
}
