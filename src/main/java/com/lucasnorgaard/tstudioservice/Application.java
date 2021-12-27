package com.lucasnorgaard.tstudioservice;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lucasnorgaard.tstudioservice.internal.IconPackTask;
import com.lucasnorgaard.tstudioservice.internal.LanguageTask;
import com.lucasnorgaard.tstudioservice.internal.MinIO;
import com.lucasnorgaard.tstudioservice.models.Language;
import lombok.Getter;
import lombok.Setter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.gitlab4j.api.GitLabApi;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class Application {

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

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    private static final String folderUrl = "http://104.248.169.204:3000/iconmapping/folder";
    private static final String fileUrl = "http://104.248.169.204:3000/iconmapping/file";



    public static void main(String[] args) {


        String gitHubToken = System.getenv("TSTUDIO_SERVICE_GITHUB");
        String gitLabToken = System.getenv("TSTUDIO_SERVICE_GITLAB");
        try {

            Application.getFileMaps();
            Application.getFolderMaps();

            gitHub = new GitHubBuilder().withOAuthToken(gitHubToken).build();
            gitLabApi = new GitLabApi("https://gitlab.com", gitLabToken);
            minIO = new MinIO();
            languages = Utils.getLanguages();

        } catch (IOException e) {
            e.printStackTrace();
        }

        SpringApplication.run(Application.class, args);


        scheduler.scheduleAtFixedRate(new IconPackTask(), 1, 12, TimeUnit.HOURS);
        scheduler.scheduleAtFixedRate(new LanguageTask(), 2, 12, TimeUnit.HOURS);

    }

    private static void getFileMaps() {
        try {
            Gson gson = new Gson();

            Request fileRequest = new Request.Builder()
                    .url(fileUrl)
                    .build();

            try (Response response = httpClient.newCall(fileRequest).execute()) {
                JsonObject jsonObject = gson.fromJson(Objects.requireNonNull(response.body()).string(), JsonObject.class);
                JsonArray icons = jsonObject.getAsJsonArray("icons");
                icons.forEach(o -> {
                    JsonObject object = gson.fromJson(o, JsonObject.class);
                    String name = String.valueOf(object.get("name"));
                    name = name.replaceAll("\"", "");
                    List<String> fileNames = gson.fromJson(object.getAsJsonArray("fileNames"), List.class);
                    List<String> fileExtensions = gson.fromJson(object.getAsJsonArray("fileExtensions"), List.class);

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
            Gson gson = new Gson();

            Request folderRequest = new Request.Builder()
                    .url(folderUrl)
                    .build();

            try (Response response = httpClient.newCall(folderRequest).execute()) {
                JsonArray jsonArray = gson.fromJson(Objects.requireNonNull(response.body()).string(), JsonArray.class);
                JsonObject jsonObject = (JsonObject) jsonArray.get(0);
                JsonArray icons = jsonObject.getAsJsonArray("icons");
                icons.forEach(o -> {
                    JsonObject object = gson.fromJson(o, JsonObject.class);
                    String name = String.valueOf(object.get("name"));
                    name = name.replaceAll("\"", "");
                    List<String> folderNames = gson.fromJson(object.getAsJsonArray("folderNames"), List.class);

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
