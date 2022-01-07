package com.lucasnorgaard.tstudioservice.service;


import com.google.gson.Gson;
import com.lucasnorgaard.tstudioservice.Application;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class PresetService {


    public List<String> getPresets() {
        return new ArrayList<>(Application.getPresets().keySet());
    }

    public String getPreset(String key) {
        return Application.GSON.toJson(Application.getPresets().get(key));
    }
}
