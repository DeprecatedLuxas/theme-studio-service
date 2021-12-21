package com.lucasnorgaard.tstudioservice.service;


import com.lucasnorgaard.tstudioservice.Application;
import com.lucasnorgaard.tstudioservice.Utils;
import com.lucasnorgaard.tstudioservice.models.Language;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class LanguageService {


    public String getLanguageByExt(String name) {
        String lang = "";
        Optional<String> extension = Utils.getFileExtension(name);
        if (extension.isPresent()) {

            Map<String, Language> languageMap = Application.getLanguages();
            for (Map.Entry<String, Language> entry : languageMap.entrySet()) {

                Language language = entry.getValue();

                if (language.getExtensions() != null && language.getExtensions().contains("."+extension.get())) {
                    lang = entry.getKey();
                    break;
                }
            }
        }


        return lang;
    }


}
