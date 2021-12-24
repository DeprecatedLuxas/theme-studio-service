package com.lucasnorgaard.tstudioservice.controllers;



import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lucasnorgaard.tstudioservice.Application;
import com.lucasnorgaard.tstudioservice.exceptions.SchemaNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/schemas")
public class SchemaController {

    @GetMapping(value = "/{schema}", produces = "application/json")
    public String getSchema(@PathVariable String schema) {
        JsonObject obj;
        Gson gson = new Gson();
        try {
            obj = Application.getSchemas().get(schema);
        } catch (NullPointerException e) {
            throw new SchemaNotFoundException(schema);
        }
        return gson.toJson(obj);
    }
}
