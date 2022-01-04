package com.lucasnorgaard.tstudioservice.models.marketplace;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class Marketplace {

    public Publisher publisher;
    public String extensionId;
    public String extensionName;
    public String displayName;
    public String flags;
    public String lastUpdated;
    public String publishedDate;
    public String releaseDate;
    public String shortDescription;
    public List<Version> versions;
    public List<String> categories;
    public List<String> tags;
    public List<Statistic> statistics;
    public List<InstallationTarget> installationTargets;
    public int deploymentType;

}
