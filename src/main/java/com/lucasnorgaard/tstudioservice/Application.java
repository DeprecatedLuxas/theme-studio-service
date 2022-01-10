package com.lucasnorgaard.tstudioservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.lucasnorgaard.tstudioservice.internal.MappingsHelper;
import com.lucasnorgaard.tstudioservice.internal.MinIO;
import com.lucasnorgaard.tstudioservice.models.Language;
import com.lucasnorgaard.tstudioservice.models.TStudioPreset;
import lombok.Getter;
import lombok.Setter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.gitlab4j.api.GitLabApi;
import org.kohsuke.github.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SpringBootApplication
public class Application {

    private static final String folderUrl = "http://104.248.169.204:3000/iconmapping/folder";
    private static final String fileUrl = "http://104.248.169.204:3000/iconmapping/file";
    public static Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    public static Helpers HELPERS = new Helpers();
    @Getter
    public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(12);
    public static String VERSION;
    @Getter
    public static GitHub gitHub;
    @Getter
    public static GitLabApi gitLabApi;
    @Getter
    public static MinIO minIO;
    @Getter
    @Setter
    public static Map<String, Language> languages;
    @Getter
    public static Map<String, List<String>> fileIconExtMap = new HashMap<>();
    @Getter
    public static Map<String, List<String>> fileIconNameMap = new HashMap<>();
    @Getter
    public static Map<String, List<String>> folderIconMap = new HashMap<>();
    @Getter
    public static OkHttpClient httpClient = new OkHttpClient();

    @Getter
    public static Map<String, TStudioPreset> presets = new HashMap<>();
    public static List<String> validIcons;
    public static String LAST_COMMIT_SHA = "4518e34d463168d064e62630ebae45f2f2db8fd0";

    public static void main(String[] args) {

        String gitHubToken = System.getenv("TSTUDIO_SERVICE_GITHUB");
        String gitLabToken = System.getenv("TSTUDIO_SERVICE_GITLAB");


        try {
            gitHub = new GitHubBuilder().withOAuthToken(gitHubToken).build();

            presets = Application.HELPERS.getPresets();
            List<GHContent> directoryContent = gitHub.getRepository("DeprecatedLuxas/icon-mappings").getDirectoryContent("/custom-icons");
            System.out.println(directoryContent.stream().map(GHContent::getName).filter(name -> name.contains(".svg")).collect(Collectors.toList()));

            Request versionRequest = new Request.Builder().url("https://raw.githubusercontent.com/PKief/vscode-material-icon-theme/main/package.json").build();

            try (Response versionResponse = httpClient.newCall(versionRequest).execute()) {
                int code = versionResponse.code();

                // If the package.json is missing, or other stuff happens.
                // Set version to the latest version. I checked during writing this code.
                if (code != 200) VERSION = "4.11.0";
                String responseBody = Objects.requireNonNull(versionResponse.body()).string();
                JsonObject jsonObject = GSON.fromJson(responseBody, JsonObject.class);
                VERSION = jsonObject.get("version").getAsString();
                System.out.println("Found Material Icon Theme v" + VERSION);
            }

            Request iconsRequest = new Request.Builder().url("https://pkief.vscode-unpkg.net/PKief/material-icon-theme/" + VERSION + "/extension/icons/").build();

            try (Response iconsResponse = httpClient.newCall(iconsRequest).execute()) {

                String responseBody = Objects.requireNonNull(iconsResponse.body()).string();

                TypeToken<List<String>> listTypeToken = new TypeToken<>() {
                };
                validIcons = GSON.fromJson(responseBody, listTypeToken.getType());
                System.out.println("Found " + validIcons.size() + " icons from Material Icon Theme v" + VERSION);
            }


            Application.getFileMaps();
            Application.getFolderMaps();


            gitLabApi = new GitLabApi("https://gitlab.com", gitLabToken);
            minIO = new MinIO();
            languages = HELPERS.getLanguages();

        } catch (IOException e) {
            e.printStackTrace();
        }

        SpringApplication.run(Application.class, args);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    PagedIterator<GHCommit> commits = Application.getGitHub().getRepository("PKief/vscode-material-icon-theme").listCommits()._iterator(0);
                    if (commits.hasNext()) {
                        String sha = commits.next().getSHA1();
                        if (!LAST_COMMIT_SHA.equals(sha)) {
                            LAST_COMMIT_SHA = sha;
                            System.out.println("Change detected. Updating SHA to " + sha);
                            Request mapRequest = new Request.Builder().url("http://104.248.169.204:3000/iconmapping/all").build();
                            try (Response response = httpClient.newCall(mapRequest).execute()) {
                                int code = response.code();
                                if (code != 200) return;
                                MappingsHelper.parseMappings(Objects.requireNonNull(response.body()).string());
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1, TimeUnit.HOURS);
        scheduler.scheduleAtFixedRate(() -> Application.setLanguages(HELPERS.getLanguages()), 2, 12, TimeUnit.HOURS);

    }

    private static void getFileMaps() {
        try {


            Request fileRequest = new Request.Builder()
                    .url(fileUrl)
                    .build();

            try (Response response = httpClient.newCall(fileRequest).execute()) {
                JsonObject jsonObject = GSON.fromJson(Objects.requireNonNull(response.body()).string(), JsonObject.class);
                JsonArray icons = jsonObject.getAsJsonArray("icons");
                icons.forEach(o -> {
                    JsonObject object = GSON.fromJson(o, JsonObject.class);
                    String name = object.get("name").getAsString();
                    List<String> fileNames = GSON.fromJson(object.getAsJsonArray("fileNames"), List.class);
                    List<String> fileExtensions = GSON.fromJson(object.getAsJsonArray("fileExtensions"), List.class);
                    if (name.contains("type")) System.out.println(name);
                    if (fileNames != null) {
                        fileIconNameMap.put(name, fileNames);
                    }

                    if (fileExtensions != null) {

                        fileIconExtMap.put(name, fileExtensions);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void getFolderMaps() {
        try {

            Request folderRequest = new Request.Builder()
                    .url(folderUrl)
                    .build();

            try (Response response = httpClient.newCall(folderRequest).execute()) {
                JsonArray jsonArray = GSON.fromJson(Objects.requireNonNull(response.body()).string(), JsonArray.class);
                JsonObject jsonObject = (JsonObject) jsonArray.get(0);
                JsonArray icons = jsonObject.getAsJsonArray("icons");
                icons.forEach(o -> {
                    JsonObject object = GSON.fromJson(o, JsonObject.class);
                    String name = object.get("name").getAsString();
                    List<String> folderNames = GSON.fromJson(object.getAsJsonArray("folderNames"), List.class);
                    if (object.has("enabledFor")) return;
                    if (folderNames != null) {
                        folderIconMap.put(name, folderNames);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
