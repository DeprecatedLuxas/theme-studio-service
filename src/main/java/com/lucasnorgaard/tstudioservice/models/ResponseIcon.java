package com.lucasnorgaard.tstudioservice.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class ResponseIcon {

    public String url;
    public @Nullable String url_opened;
}
