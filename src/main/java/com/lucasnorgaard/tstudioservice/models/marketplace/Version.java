package com.lucasnorgaard.tstudioservice.models.marketplace;


import lombok.AllArgsConstructor;

import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class Version {

    public String version;
    public String flags;
    public String lastUpdated;
    public List<File> files;
    public List<Property> properties;
    public String assetUri;
    public String fallbackAssetUri;
}
