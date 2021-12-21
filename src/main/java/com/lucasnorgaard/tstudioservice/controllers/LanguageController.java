package com.lucasnorgaard.tstudioservice.controllers;

import com.lucasnorgaard.tstudioservice.Application;
import com.lucasnorgaard.tstudioservice.models.Language;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/languages")
public class LanguageController {



    @GetMapping(produces = "application/json")
    public Map<String, Language> getLanguage() {
        return Application.getLanguages();
    }
}
