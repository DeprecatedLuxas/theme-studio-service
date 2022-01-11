package com.lucasnorgaard.tstudioservice;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.lucasnorgaard.tstudioservice.internal.MinIO;
import com.lucasnorgaard.tstudioservice.models.Language;
import com.lucasnorgaard.tstudioservice.models.TStudioPreset;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import okhttp3.Request;
import okhttp3.Response;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class Helpers {

    public Map<String, TStudioPreset> getPresets() {

        GitHub gitHub = Application.getGitHub();
        Map<String, TStudioPreset> presets = new HashMap<>();
        try {
            GHRepository repository = gitHub.getRepository("DeprecatedLuxas/tstudio-presets");
            List<GHContent> contents = repository.getDirectoryContent("presets")
                    .stream()
                    .filter(f -> f.getName().contains(".tstudio-preset")).collect(Collectors.toList());


            for (GHContent content : contents) {
                try {
                    String fileContent = new String(content.read().readAllBytes(), StandardCharsets.UTF_8);
                    TStudioPreset tStudioPreset = Application.GSON.fromJson(fileContent, TStudioPreset.class);
                    presets.put(content.getName().replace(".tstudio-preset", ""), tStudioPreset);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return presets;
    }

    public Map<String, Language> getLanguages() {
        System.out.println("Fetching Languages");
        Map<String, Language> languages = new HashMap<>();

        try {
            GHRepository repo = Application.getGitHub().getRepository("github/linguist");
            GHContent content = repo.getFileContent("lib/linguist/languages.yml");
            Yaml yaml = new Yaml();
            Type langMapType = new TypeToken<Map<String, Language>>() {
            }.getType();
            Object json = yaml.loadAs(content.read(), Object.class);

            languages = Application.GSON.fromJson(Application.GSON.toJson(json), langMapType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Updated Languages");
        return languages;
    }

    public void getAndUploadIcons() {
        List<String> icons = Application.validIcons;

        for (String icon : icons) {
            Request request = new Request.Builder().url(icon).build();
            try (Response response = Application.getHttpClient().newCall(request).execute()) {
                InputStream body = Objects.requireNonNull(response.body()).byteStream();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(body.readAllBytes());
                PutObjectArgs args = PutObjectArgs.builder()
                        .bucket(MinIO.TSTUDIO_ICONS)
                        .object(Application.VERSION + "/" + Application.HELPERS.getFormattedIconName(icon, Application.VERSION))
                        .stream(inputStream, inputStream.available(), -1)
                        .contentType("image/svg+xml")
                        .build();
                Application.getMinIO().getMinioClient().putObject(args);

            } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                e.printStackTrace();
            }
        }
    }

    public String getFormattedIconName(String path, String version) {
        if (path.contains("https://raw.githubusercontent.com/DeprecatedLuxas/icon-mappings")) {
            int sub = path.indexOf("https://raw.githubusercontent.com/DeprecatedLuxas/icon-mappings/main/custom-icons/")
                    + "https://raw.githubusercontent.com/DeprecatedLuxas/icon-mappings/main/custom-icons/".length();
            return path.substring(sub);
        }
        int sub = path.indexOf("https://pkief.vscode-unpkg.net/PKief/material-icon-theme/" + version + "/extension/icons/")
                + ("https://pkief.vscode-unpkg.net/PKief/material-icon-theme/" + version + "/extension/icons/").length();
        return path.substring(sub);
    }

    public void getCustomIcons() throws IOException {
        List<String> icons = Application.validIcons;
        List<GHContent> customIcons = Application.getGitHub()
                .getRepository("DeprecatedLuxas/icon-mappings")
                .getDirectoryContent("custom-icons")
                .stream().filter(f -> f.getName().contains(".svg"))
                .collect(Collectors.toList());

        for (GHContent customIcon : customIcons) {
            icons.add(customIcon.getDownloadUrl());
        }
        System.out.println("Adding " + customIcons.size() + " custom-icons to list");
    }
}
