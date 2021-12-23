package com.lucasnorgaard.tstudioservice;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lucasnorgaard.tstudioservice.internal.IconPackTask;
import com.lucasnorgaard.tstudioservice.internal.LanguageTask;
import com.lucasnorgaard.tstudioservice.internal.MinIO;
import com.lucasnorgaard.tstudioservice.models.Language;
import lombok.Getter;
import lombok.Setter;
import org.gitlab4j.api.GitLabApi;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @Setter
    public static Map<String, JsonObject> schemas = new HashMap<>();

    public static void main(String[] args) {
        String gitHubToken = System.getenv("TSTUDIO_SERVICE_GITHUB");
        String gitLabToken = System.getenv("TSTUDIO_SERVICE_GITLAB");
        try {
            Gson gson = new Gson();
            Arrays.stream(getResourceFolderFiles("schemas")).forEach(f -> {
                try {
                    JsonObject obj = gson.fromJson(Files.readString(Paths.get(f.getPath())), JsonObject.class);
                    schemas.put(f.getName().split("\\.")[0], obj);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            System.out.println(schemas);
            gitHub = new GitHubBuilder().withOAuthToken(gitHubToken).build();
            gitLabApi = new GitLabApi("https://gitlab.com", gitLabToken);
            minIO = new MinIO();
            languages = Utils.getLanguages();

        } catch (IOException e) {
            e.printStackTrace();
        }
        SpringApplication.run(Application.class, args);

        ScheduledExecutorService scheduler
                = Executors.newScheduledThreadPool(10);

        scheduler.scheduleAtFixedRate(new IconPackTask(), 1, 12, TimeUnit.HOURS);
        scheduler.scheduleAtFixedRate(new LanguageTask(), 2, 12, TimeUnit.HOURS);

    }

    private static File[] getResourceFolderFiles (String folder) {
        ClassLoader loader = Application.class.getClassLoader();
        URL url = loader.getResource(folder);
        String path = url.getPath();
        return new File(path).listFiles();
    }


}
