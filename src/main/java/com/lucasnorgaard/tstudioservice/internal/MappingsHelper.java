package com.lucasnorgaard.tstudioservice.internal;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.lucasnorgaard.tstudioservice.Application;
import com.lucasnorgaard.tstudioservice.models.Mapping;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MappingsHelper {


    public static void parseMappings(String mappings) throws IOException {
        Gson gson = new Gson();
        Type mappingType = new TypeToken<Map<String, Mapping>>() {
        }.getType();
        JsonObject jsonObject = gson.fromJson(mappings, JsonObject.class);
        GitHub github = Application.getGitHub();
        GHRepository repository = github.getRepository("DeprecatedLuxas/icon-mappings");
        if (jsonObject.has("files")) {
            GHContent fileMappingGH = repository.getFileContent("file_mappings.json");
            String fileMapping = new String(fileMappingGH.read().readAllBytes(), StandardCharsets.UTF_8);

            Map<String, Mapping> ghFileMappings = gson.fromJson(fileMapping, mappingType);
            Map<String, Mapping> fileMappings = parseMapping("files", jsonObject.get("files"));

            System.out.println(gson.toJson(ghFileMappings, mappingType));
            System.out.println(gson.toJson(fileMappings, mappingType));

            System.out.println(gson.toJson(mergeMappings(ghFileMappings, fileMappings)));

        }

        if (jsonObject.has("folder")) {
            parseMapping("folder", jsonObject.get("folder"));
        }

        // Map<String, Employee> result = Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
        //        .collect(Collectors.toMap(
        //               Map.Entry::getKey,
        //              Map.Entry::getValue,
        //            (value1, value2) -> new Employee(value2.getId(), value1.getName())));
    }

    private static Map<String, Mapping> mergeMappings(Map<String, Mapping> ghMappings, Map<String, Mapping> newMappings) {
        Map<String, Mapping> merged = new LinkedHashMap<>(ghMappings);

        for (Map.Entry<String, Mapping> entry : newMappings.entrySet()) {
            String key = entry.getKey();
            Mapping mapping = entry.getValue();


        }

        return merged;
        /*
        return Stream.concat(ghMappings.entrySet().stream(), newMappings.entrySet().stream()).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (v1, v2) -> new Mapping(
                        combineLists(v1.names, v2.names), combineLists(v1.extensions, v2.extensions)
                )
        ));
        */

    }

    private static List<String> combineLists(@Nullable List<String> list1, @Nullable List<String> list2) {
        if (list1 == null && list2 == null) return null;
        if (list1 == null) {
            list1 = new ArrayList<>();
        }
        if (list2 == null) {
            list2 = new ArrayList<>();
        }
        if (list1.equals(list2)) return list1;
        return Stream.concat(list1.stream(), list2.stream())
                .collect(Collectors.toList());
    }


    private static Map<String, Mapping> parseMapping(String type, JsonElement mapping) {
        Gson gson = new Gson();
        Map<String, Mapping> mappings = new LinkedHashMap<>();
        JsonObject jsonObject = gson.fromJson(mapping, JsonObject.class);
        if (jsonObject.has("icons")) {
            String nameKey = type.equals("folder") ? "folderNames" : "fileNames";
            String extensionsKey = type.equals("folder") ? "folderExtensions" : "fileExtensions";
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
                Mapping mappingObj = new Mapping(
                        names,
                        extensions
                );
                mappings.put(name, mappingObj);
            });

        }
        return mappings;
    }
}
