package com.lucasnorgaard.tstudioservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.InputStream;

@Data
@AllArgsConstructor
public class IconPack {

    public String name;
    public InputStream content;

}
