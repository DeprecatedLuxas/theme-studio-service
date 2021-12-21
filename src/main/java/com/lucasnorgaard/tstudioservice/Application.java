package com.lucasnorgaard.tstudioservice;

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

import java.io.IOException;
import java.util.Map;
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

    public static void main(String[] args) {
        String gitHubToken = System.getenv("TSTUDIO_SERVICE_GITHUB");
        String gitLabToken = System.getenv("TSTUDIO_SERVICE_GITLAB");

        try {
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

}
