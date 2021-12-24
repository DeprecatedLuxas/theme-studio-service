package com.lucasnorgaard.tstudioservice.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class SchemaNotFoundException extends RuntimeException {

    public SchemaNotFoundException(String name) {
        super("Schema with name " + name + " was not found");
    }
}
