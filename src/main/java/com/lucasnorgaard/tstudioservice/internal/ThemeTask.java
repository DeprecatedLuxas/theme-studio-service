package com.lucasnorgaard.tstudioservice.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.lucasnorgaard.tstudioservice.Application;
import com.lucasnorgaard.tstudioservice.models.Preset;
import com.lucasnorgaard.tstudioservice.models.marketplace.Marketplace;
import com.lucasnorgaard.tstudioservice.models.marketplace.Property;
import com.lucasnorgaard.tstudioservice.models.marketplace.Publisher;
import com.lucasnorgaard.tstudioservice.models.marketplace.Version;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.kohsuke.github.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class ThemeTask implements Runnable {


    public List<String> EXCLUDED_WORDS = List.of("icons", "icon", "icon-theme");

    @Override
    public void run() {


        try {
            GHRepository repository = Application.getGitHub().getRepository("DeprecatedLuxas/tstudio-presets");
            GHContent file = repository.getFileContent("preset-list.json");
            String fileContent = new String(file.read().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Preset> presets = new HashMap<>();
            String query = "{\"assetTypes\":[\"Microsoft.VisualStudio.Services.Icons.Default\",\"Microsoft.VisualStudio.Services.Icons.Branding\",\"Microsoft.VisualStudio.Services.Icons.Small\"],\"filters\":[{\"criteria\":[{\"filterType\":8,\"value\":\"Microsoft.VisualStudio.Code\"},{\"filterType\":10,\"value\":\"target:\\\"Microsoft.VisualStudio.Code\\\" \"},{\"filterType\":12,\"value\":\"37888\"},{\"filterType\":5,\"value\":\"Themes\"}],\"direction\":2,\"pageSize\":100,\"pageNumber\":1,\"sortBy\":4,\"sortOrder\":0,\"pagingToken\":null}],\"flags\":870}";
            RequestBody requestBody = RequestBody.create(query, MediaType.get("application/json"));
            Request request = new Request.Builder().url("https://marketplace.visualstudio.com/_apis/public/gallery/extensionquery").post(requestBody).build();

            try (Response response = Application.getHttpClient().newCall(request).execute()) {
                int code = response.code();

                if (code != 200) return;
                String body = Objects.requireNonNull(response.body()).string();
                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .create();
                JsonObject responseObject = gson.fromJson(body, JsonObject.class);
                JsonArray jsonArray = responseObject.get("results")
                        .getAsJsonArray()
                        .get(0)
                        .getAsJsonObject()
                        .get("extensions")
                        .getAsJsonArray();
                Type listTypeToken = new TypeToken<List<Marketplace>>() {
                }.getType();

                List<Marketplace> marketplaceList = gson.fromJson(jsonArray, listTypeToken);
                marketplaceList = marketplaceList.stream().filter(m -> {
                    Version version = m.versions.get(0);

                    List<Property> properties = version.properties;
                    // We only allow GitHub Links.
                    properties = properties.stream().filter(p -> p.key.equals("Microsoft.VisualStudio.Services.Links.GitHub")).collect(Collectors.toList());
                    return properties.size() >= 1;
                }).filter(m -> {
                    String extensionName = m.extensionName;
                    String displayName = m.displayName;
                    String shortDescription = m.shortDescription;
                    List<String> tags = m.tags;

                    if (EXCLUDED_WORDS.stream().anyMatch(extensionName::contains)) {
                        return false;
                    }
                    if (EXCLUDED_WORDS.stream().anyMatch(displayName::contains)) {
                        return false;
                    }
                    if (EXCLUDED_WORDS.stream().anyMatch(shortDescription::contains)) {
                        return false;
                    }
                    return EXCLUDED_WORDS.stream().noneMatch(tags::contains);

                }).limit(14).collect(Collectors.toList());
                // Limiting this to 14, because marketplace api has a max request of 15 per minute.
                marketplaceList.forEach(m -> {
                    Version version = m.versions.get(0);
                    Publisher publisher = m.publisher;
                    String publisherName = publisher.publisherName;
                    List<Property> properties = version.properties;
                    Property property = properties.stream()
                            .filter(p -> p.key.equals("Microsoft.VisualStudio.Services.Links.GitHub"))
                            .collect(Collectors.toList())
                            .get(0);
                    String downloadUrl = "https://marketplace.visualstudio.com/_apis/public/gallery/publishers/"
                            + publisherName
                            + "/vsextensions/"
                            + m.extensionName
                            + "/"
                            + version.version
                            + "/vspackage";
                    Preset preset = new Preset(property.value, downloadUrl);
                    System.out.println("                      ");
//                    System.out.println(version.version);
//                    System.out.println(m.extensionName);
//                    System.out.println(m.displayName);
//                    System.out.println(m.extensionId);
//                    System.out.println(publisher.publisherName);
//                    System.out.println(publisher.publisherId);
                    System.out.println(version.assetUri);

                    System.out.println(version.fallbackAssetUri);
                    // https://dracula-theme.gallerycdn.vsassets.io/extensions/dracula-theme/theme-dracula/2.24.1/1639416410130/Microsoft.VisualStudio.Code.Manifest
                    // https://dracula-theme.gallerycdn.vsassets.io/extensions/dracula-theme/theme-dracula/2.24.1/1639416410000/Microsoft.VisualStudio.Code.Manifest
                    // https://dracula-theme.gallerycdn.vsassets.io/extensions/dracula-theme/theme-dracula/2.24.1/1639416273023/Microsoft.VisualStudio.Code.Manifest
                    // https://dracula-theme.gallerycdn.vsassets.io/extensions/dracula-theme/theme-dracula/2.24.1/1639416410/Microsoft.VisualStudio.Code.Manifest
                    // https://dracula-theme.gallerycdn.vsassets.io/extensions/dracula-theme/theme-dracula/2.24.1/1639416410/Microsoft.VisualStudio.Code.Manifest

                    // https://hookyqr.gallerycdn.vsassets.io/extensions/hookyqr/beautify/1.5.0/1556863124877/Microsoft.VisualStudio.Code.Manifest
                    // https://%publisher%.gallerycdn.vsassets.io/extensions/%name%/%extension%/%version%/%unix in someway%/Microsoft.VisualStudio.Code.Manifest
                    presets.put(m.extensionName, preset);
                });
                Map<String, Preset> oldContent = gson.fromJson(fileContent, new TypeToken<Map<String, Preset>>() {
                }.getType());
                if (presets.equals(oldContent)) {
                    System.out.println("preset-list.json is the same as parsed presets");

                    return;
                }
                String sha = repository.getRef("heads/main").getObject().getSha();
                String blobSha = repository.getFileContent("preset-list.json").getSha();
                List<String> branches = repository.getBranches().keySet()
                        .stream()
                        .filter(c -> c.contains("new-preset-list"))
                        .collect(Collectors.toList());

                String branchName = "new-preset-list-" + branches.size();

                String refName = "refs/heads/" + branchName;
                // repository.createRef(refName, sha);
                // repository.createContent()
                //        .content(gson.toJson(presets))
                //         .message("Added list of presets github url")
                //        .path("preset-list.json")
                //        .sha(blobSha)
                //        .branch(branchName)
                //       .commit();

                // repository.createPullRequest("added presets", refName, "main", "## Added new presets");
//                for (Preset preset : presets.values()) {
//                    Thread.sleep(2000);
////                    Application.getScheduler().execute(new DownloadPresetVSIX(preset));
//                }
                System.out.println(presets.size());
                // Links to get manifests i think?
                // https://hookyqr.gallerycdn.vsassets.io/extensions/hookyqr/beautify/1.5.0/1556863124877/Microsoft.VisualStudio.Code.Manifest
                // https://%publisher%.gallerycdn.vsassets.io/extensions/%name%/%extension%/%version%/%unix in someway%/Microsoft.VisualStudio.Code.Manifest
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
