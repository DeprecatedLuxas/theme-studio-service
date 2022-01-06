package com.lucasnorgaard.tstudioservice.controllers;


import com.google.gson.JsonObject;
import com.lucasnorgaard.tstudioservice.service.PresetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/preset")
public class PresetController {

    @Autowired
    private PresetService presetService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getPresets() {
        return this.presetService.getPresets();
    }

    @GetMapping(value = "/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getPreset(@PathVariable String key) {
        return this.presetService.getPreset(key);
    }

}
