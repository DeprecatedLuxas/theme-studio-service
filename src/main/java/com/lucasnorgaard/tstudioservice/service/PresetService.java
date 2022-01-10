package com.lucasnorgaard.tstudioservice.service;


import com.lucasnorgaard.tstudioservice.Application;
import com.lucasnorgaard.tstudioservice.models.TStudioPreset;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class PresetService {


    public List<String> getPresets() {
        return new ArrayList<>(Application.getPresets().keySet());
    }

    public TStudioPreset getPreset(String key) {
        return Application.getPresets().get(key);
    }
}
