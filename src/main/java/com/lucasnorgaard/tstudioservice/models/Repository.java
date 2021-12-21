package com.lucasnorgaard.tstudioservice.models;

import lombok.Data;

import java.util.List;

@Data
public class Repository {

    private final String name;
    private final String owner;
    private final String language;
    private final List<Content> files;



}
