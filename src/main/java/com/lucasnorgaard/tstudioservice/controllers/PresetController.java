package com.lucasnorgaard.tstudioservice.controllers;


import com.lucasnorgaard.tstudioservice.models.Preset;
import com.lucasnorgaard.tstudioservice.models.TStudioPreset;
import com.lucasnorgaard.tstudioservice.service.PresetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/preset")
public class PresetController {

    @Autowired
    private PresetService presetService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, TStudioPreset> getPresets() {
        return this.presetService.getPresets();
    }

}
