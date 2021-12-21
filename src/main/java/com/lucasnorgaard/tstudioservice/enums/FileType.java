package com.lucasnorgaard.tstudioservice.enums;


import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum FileType {

    DIRECTORY("directory"),
    FILE("file");

    private String name;
}
