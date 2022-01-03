package com.lucasnorgaard.tstudioservice;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.lucasnorgaard.tstudioservice.models.Language;
import com.lucasnorgaard.tstudioservice.models.Mapping;
import com.lucasnorgaard.tstudioservice.models.ResponseIcon;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class Utils {


    public static Optional<String> getFileExtension(String filename) {
        return Optional.ofNullable(filename).filter(f -> f.contains(".")).map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    public static Map<String, Language> getLanguages() {
        Map<String, Language> languages = new HashMap<>();

        try {
            GHRepository repo = Application.getGitHub().getRepository("github/linguist");
            GHContent content = repo.getFileContent("lib/linguist/languages.yml");
            Yaml yaml = new Yaml();
            Gson gson = new Gson();
            Type langMapType = new TypeToken<Map<String, Language>>() {
            }.getType();
            Object json = yaml.loadAs(content.read(), Object.class);

            languages = gson.fromJson(gson.toJson(json), langMapType);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return languages;
    }

    public static <K, V extends Comparable<V>> K maxUsingStreamAndLambda(Map<K, V> map) {
        Optional<Map.Entry<K, V>> maxEntry = map.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue()
                );

        return maxEntry.get().getKey();
    }

    public static ResponseIcon getCorrectFolderIcon(String name) {
        ResponseIcon icon = new ResponseIcon(
                "folder",
                "folder-open"
        );

        for (Map.Entry<String, List<String>> entry : Application.getFolderIconMap().entrySet()) {
            List<String> names = entry.getValue();
            String key = entry.getKey();
            if (names.contains(name)) {
                icon.url = key;
                icon.url_opened = key + "-open";
                break;
            }
        }
        return icon;
    }

    public static ResponseIcon getCorrectFileIcon(String name, String ext) {
        ResponseIcon icon = new ResponseIcon(
                "file",
                null
        );

        System.out.println(ext);
        System.out.println(name);

        if (!ext.equals("")) {
            System.out.println("Running");
            for (Map.Entry<String, List<String>> entry : Application.getFileIconExtMap().entrySet()) {
                List<String> exts = entry.getValue();
                String key = entry.getKey();
                System.out.println(exts);
                if (exts == null) return icon;

                if (exts.contains(ext)) {
                    icon.url = key;
                    icon.url_opened = null;
                    break;
                }
            }
        }
        System.out.println(icon);

        if (!name.equals("")) {
            System.out.println("Running 2");

            for (Map.Entry<String, List<String>> entry : Application.getFileIconNameMap().entrySet()) {
                List<String> names = entry.getValue();
                String key = entry.getKey();
                if (names == null) return icon;
                if (names.contains(name)) {
                    icon.url = key;
                    icon.url_opened = null;
                    break;
                }
            }
        }
        System.out.println(icon);

        return icon;
    }

    public static String getCorrectIcon(String ext, String name, boolean dir) {
        String icon = "";
        Map<String, List<String>> extMap = Application.getFileIconExtMap();
        Map<String, List<String>> nameMap = Application.getFileIconNameMap();
        Map<String, List<String>> folderNameMap = Application.getFolderIconMap();


        for (Map.Entry<String, List<String>> entry : extMap.entrySet()) {
            List<String> exts = entry.getValue();
            if (exts == null) return icon;
            if (exts.contains(ext)) {
                icon = entry.getKey();
                break;
            }
        }

        for (Map.Entry<String, List<String>> entry : nameMap.entrySet()) {
            List<String> names = entry.getValue();
            if (names == null) return icon;
            if (names.contains(name)) {
                icon = entry.getKey();
                break;
            }
        }

        if (dir) {
            for (Map.Entry<String, List<String>> entry : folderNameMap.entrySet()) {
                List<String> names = entry.getValue();
                if (names == null) return icon;
                if (names.contains(name)) {
                    icon = entry.getKey();
                    break;
                }
            }
        }

        if (icon.equals("")) {
            icon = dir ? "folder" : "file";
        }
        return icon;
    }


    public static void parseMappings(String mappings) throws IOException {
        Gson gson = new Gson();

        JsonObject jsonObject = gson.fromJson(mappings, JsonObject.class);
        GitHub github = Application.getGitHub();
        GHRepository repository = github.getRepository("DeprecatedLuxas/icon-mappings");
        if (jsonObject.has("files")) {
            parseMapping("files", jsonObject.get("files"));
            System.out.println(gson.toJson(parseMapping("files", jsonObject.get("files"))));;
        }

        if (jsonObject.has("folder")) {
            parseMapping("folder", jsonObject.get("folder"));
        }
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
