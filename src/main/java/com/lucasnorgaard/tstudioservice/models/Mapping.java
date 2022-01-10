package com.lucasnorgaard.tstudioservice.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;


@Setter
@Getter
@AllArgsConstructor
public class Mapping {

    public @Nullable List<String> ids;
    public @Nullable List<String> names;
    public @Nullable List<String> extensions;

}
