package com.lucasnorgaard.tstudioservice.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LanguageType {

    DATA("data"),
    MARKUP("markup"),
    PROGRAMMING("programming"),
    PROSE("prose"),
    NIL("nil");

    private final String name;
}
