package com.lucasnorgaard.tstudioservice.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class RepositoryNotFoundException extends RuntimeException {

    public RepositoryNotFoundException(String id) {
        super("Repository was not found with " + id);
    }
}
