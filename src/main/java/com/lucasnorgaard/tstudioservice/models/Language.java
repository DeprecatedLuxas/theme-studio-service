package com.lucasnorgaard.tstudioservice.models;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lucasnorgaard.tstudioservice.enums.LanguageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.List;


@Data
@AllArgsConstructor
public class Language {

    @Nullable
    public LanguageType type;
    @Nullable
    public String color;
    @Nullable
    public List<String> extensions;
    @Nullable
    public String tm_scope;
    @Nullable
    public String ace_mode;
    @Nullable
    public Long language_id;
    @Nullable
    public List<String> aliases;
    @Nullable
    public String codemirror_mode;
    @Nullable
    public String codemirror_mime_type;
    @Nullable
    public List<String> interpreters;
    @Nullable
    public String group;
    @Nullable
    public List<String> filenames;
    @Nullable
    public Boolean wrap;
    @Nullable
    public String fs_name;
    @Nullable
    public Boolean searchable;

    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
