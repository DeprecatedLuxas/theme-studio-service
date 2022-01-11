package com.lucasnorgaard.tstudioservice.internal;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.lucasnorgaard.tstudioservice.Application;
import com.lucasnorgaard.tstudioservice.models.Mapping;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MappingsHelper {


    public static void parseMappings(String mappings) {
        try {
            Gson gson = Application.GSON;
            Type mappingType = new TypeToken<Map<String, Mapping>>() {
            }.getType();
            JsonObject jsonObject = gson.fromJson(mappings, JsonObject.class);
            GitHub github = Application.getGitHub();
            GHRepository repository = github.getRepository("DeprecatedLuxas/icon-mappings");
            String sha = repository.getRef("heads/main").getObject().getSha();
            List<String> branches = repository.getBranches().keySet()
                    .stream()
                    .filter(c -> c.contains("new-mappings"))
                    .collect(Collectors.toList());

            String branchName = "new-mappings-" + branches.size();

            String refName = "refs/heads/" + branchName;
            List<String> mappingKeys = new ArrayList<>();
            repository.createRef(refName, sha);
            if (jsonObject.has("file")) {
                mappingKeys.add("file");
                GHContent fileMappingGH = repository.getFileContent("file_mappings.json");
                String fileMapping = new String(fileMappingGH.read().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, Mapping> ghFileMappings = gson.fromJson(fileMapping, mappingType);
                Map<String, Mapping> fileMappings = parseMapping("file", jsonObject.get("file"));
                String blobSha = repository.getFileContent("file_mappings.json").getSha();

                repository.createContent()
                        .content(gson.toJson(mergeMappings(ghFileMappings, fileMappings)))
                        .message("Updated file_mappings to new version")
                        .path("file_mappings.json")
                        .sha(blobSha)
                        .branch(branchName)
                        .commit();
            }

            if (jsonObject.has("folder")) {
                mappingKeys.add("folder");
                GHContent folderMappingGH;
                Map<String, Mapping> folderMappings;
                folderMappings = parseMapping("folder", jsonObject.get("folder"));
                String blobSha = null;
                try {

                    folderMappingGH = repository.getFileContent("folder_mappings.json");
                    String folderMapping = new String(folderMappingGH.read().readAllBytes(), StandardCharsets.UTF_8);
                    Map<String, Mapping> ghFolderMappings = gson.fromJson(folderMapping, mappingType);

                    blobSha = folderMappingGH.getSha();
                    folderMappings = mergeMappings(ghFolderMappings, folderMappings);
                } catch (GHFileNotFoundException ignored) {
                }


                GHContentBuilder builder = repository.createContent()
                        .content(gson.toJson(folderMappings))
                        .message("Updated folder_mappings to new version")
                        .path("folder_mappings.json");

                if (blobSha != null) {
                    builder.sha(blobSha);
                }

                builder.branch(branchName).commit();
            }

            if (jsonObject.has("language")) {
                mappingKeys.add("language");


                GHContent languageMappingGH;
                Map<String, Mapping> languageMappings;
                languageMappings = parseMapping("language", jsonObject.get("language"));
                String blobSha = null;
                try {

                    languageMappingGH = repository.getFileContent("language_mappings.json");
                    String folderMapping = new String(languageMappingGH.read().readAllBytes(), StandardCharsets.UTF_8);
                    Map<String, Mapping> ghLanguageMappings = gson.fromJson(folderMapping, mappingType);

                    blobSha = languageMappingGH.getSha();
                    languageMappings = mergeMappings(ghLanguageMappings, languageMappings);
                } catch (GHFileNotFoundException ignored) {
                }


                GHContentBuilder builder = repository.createContent()
                        .content(gson.toJson(languageMappings))
                        .message("Updated language_mappings to new version")
                        .path("language_mappings.json");

                if (blobSha != null) {
                    builder.sha(blobSha);
                }

                builder.branch(branchName).commit();
            }

//            repository.createPullRequest("updated new mappings", refName, "main", getBody(mappingKeys));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getBody(List<String> mappings) {
        StringBuilder body = new StringBuilder();

        body.append("## New Mappings\n\n");

        mappings.forEach(m -> body.append("- [x] ").append(m).append("\n"));

        return body.toString();
    }


    private static Map<String, Mapping> mergeMappings(Map<String, Mapping> ghMappings, Map<String, Mapping> newMappings) {
        Map<String, Mapping> merged = new LinkedHashMap<>(ghMappings);

        for (Map.Entry<String, Mapping> entry : newMappings.entrySet()) {
            String key = entry.getKey();
            Mapping mapping = entry.getValue();
            if (merged.get(key) != null) {
                List<String> mergedNames = merged.get(key).names;
                List<String> mergedExt = merged.get(key).extensions;
                List<String> names = combineLists(mergedNames, mapping.names);
                List<String> exts = combineLists(mergedExt, mapping.extensions);
                if (names.size() >= 1) {
                    merged.get(key).names = names;
                }
                if (exts.size() >= 1) {
                    merged.get(key).extensions = exts;
                }
            } else {
                merged.put(key, mapping);
            }

        }

        return merged;
    }

    private static List<String> combineLists(@Nullable List<String> list1, @Nullable List<String> list2) {
        if (list1 == null && list2 == null) return new ArrayList<>();
        if (list1 == null) {
            list1 = new ArrayList<>();
        }
        if (list2 == null) {
            list2 = new ArrayList<>();
        }
        if (list1.equals(list2)) return list1;
        return Stream.concat(list1.stream(), list2.stream()).collect(Collectors.toList());
    }


    private static Map<String, Mapping> parseMapping(String type, JsonElement mapping) {
        Gson gson = Application.GSON;
        Map<String, Mapping> mappings = new LinkedHashMap<>();
        JsonElement jsonElement = type.equals("file") ? gson.fromJson(mapping, JsonObject.class) : gson.fromJson(mapping, JsonArray.class);

        if (type.equals("language")) {

            JsonArray jsonArray = jsonElement.getAsJsonArray();
            Map<String, Mapping> finalMappings = mappings;
            jsonArray.forEach(l -> {
                JsonObject obj = l.getAsJsonObject();
                if (!obj.has("ids")) return;
                if (!obj.has("icon")) return;
                JsonObject iconObj = obj.getAsJsonObject("icon");
                if (!iconObj.has("name")) return;
                String name = iconObj.get("name").getAsString();
                List<String> ids = gson.fromJson(obj.get("ids"), new TypeToken<List<String>>() {
                }.getType());
                Mapping mappingObj = new Mapping(ids, null, null);
                finalMappings.put(name, mappingObj);
            });
        } else {
            String nameKey = type.equals("folder") ? "folderNames" : "fileNames";
            String extensionsKey = type.equals("folder") ? "folderExtensions" : "fileExtensions";
            if (type.equals("folder")) {
                // For now we gonna take the first one.
                mappings = handleIcons(jsonElement.getAsJsonArray().get(0).getAsJsonObject(), nameKey, extensionsKey);
            } else {
                // Type is file
                mappings = handleIcons(jsonElement.getAsJsonObject(), nameKey, extensionsKey);
            }
        }

        return mappings;
    }

    private static Map<String, Mapping> handleIcons(JsonObject jsonObject, String nameKey, String extensionsKey) {
        Gson gson = Application.GSON;
        Map<String, Mapping> mappings = new LinkedHashMap<>();
        if (jsonObject.has("icons")) {
            JsonArray icons = jsonObject.getAsJsonArray("icons");
            icons.forEach(i -> {
                JsonObject object = gson.fromJson(i, JsonObject.class);
                String name = object.get("name").getAsString();
                List<String> names = null;
                List<String> extensions = null;
                Type listType = new TypeToken<List<String>>() {
                }.getType();
                if (object.has(nameKey)) {
                    names = gson.fromJson(object.getAsJsonArray(nameKey), listType);
                }

                if (object.has(extensionsKey)) {
                    extensions = gson.fromJson(object.getAsJsonArray(extensionsKey), listType);
                }
                Mapping mappingObj = new Mapping(null, names, extensions);
                mappings.put(name, mappingObj);
            });

        }
        return mappings;
    }
}
