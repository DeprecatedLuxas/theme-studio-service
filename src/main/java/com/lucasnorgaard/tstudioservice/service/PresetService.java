package com.lucasnorgaard.tstudioservice.service;


import com.lucasnorgaard.tstudioservice.Application;
import com.lucasnorgaard.tstudioservice.models.TStudioPreset;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PresetService {



    public Map<String, TStudioPreset> getPresets() {
        return Application.getPresets();
    }
}
