package com.lucasnorgaard.tstudioservice.models;

import com.lucasnorgaard.tstudioservice.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;


@AllArgsConstructor
@Getter
@Setter
public class Content {

    public String name;
    public FileType type;
    public @Nullable String extension;
    public @Nullable
    List<Content> children;
    public String lightIconPath;
    // If type is a file, its null.
    public @Nullable String openLightIconPath;
    public String darkIconPath;
    // If type is a file, its null.
    public @Nullable String openDarkIconPath;
}
