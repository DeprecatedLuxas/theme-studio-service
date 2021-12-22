package com.lucasnorgaard.tstudioservice;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lucasnorgaard.tstudioservice.models.Language;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
            Type langMapType = new TypeToken<Map<String, Language>>() {}.getType();
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
}
