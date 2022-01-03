package com.lucasnorgaard.tstudioservice.controllers;


import com.lucasnorgaard.tstudioservice.models.Preset;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/preset")
public class PresetController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String getPresets() {
        return "";
    }

}
