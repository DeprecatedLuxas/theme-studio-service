package com.lucasnorgaard.tstudioservice.service;


import com.google.gson.Gson;
import com.lucasnorgaard.tstudioservice.Application;
import com.lucasnorgaard.tstudioservice.Utils;
import com.lucasnorgaard.tstudioservice.enums.FileType;
import com.lucasnorgaard.tstudioservice.exceptions.RepositoryNotFoundException;
import com.lucasnorgaard.tstudioservice.models.Content;
import com.lucasnorgaard.tstudioservice.models.Repository;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import lombok.Getter;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class GitHubService {

    private final String LIGHT_ICON_PATH = "http://104.248.169.204:8080/icon/{icon}_light";
    private final String DARK_ICON_PATH = "http://104.248.169.204:8080/icon/{icon}";


    @Autowired
    @Getter
    private LanguageService languageService;


    public Repository getRepoWithId(String id) {
        GHRepository repo;
        Repository repository;
        try {
            repo = Application.getGitHub().getRepositoryById(Long.parseLong(id));
            repository = getOrUpload(repo);
        } catch (IOException e) {
            throw new RepositoryNotFoundException(id);
        }
        return repository;
    }

    public Repository getOrUpload(GHRepository repo) {
        MinioClient minioClient = Application.getMinIO().getMinioClient();
        Repository repository = null;
        String name = repo.getFullName().replaceAll("/", "-");


        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("tstudio-repositories").object("github/" + name + ".json").build();
            GetObjectResponse response = minioClient.getObject(getObjectArgs);

            repository = new Gson().fromJson(new String(response.readAllBytes(), StandardCharsets.UTF_8), Repository.class);
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            if (e instanceof MinioException) {
                // Maybe if they change the exception message, this will still work.
                if (e.getMessage().contains("key does not exist")) {
                    try {
                        repository = new Repository(repo.getName(), repo.getOwnerName(), repo.getLanguage(), this.getContent(repo));
                        InputStream stream = new ByteArrayInputStream(new Gson().toJson(repository).getBytes(StandardCharsets.UTF_8));
                        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                                .bucket("tstudio-repositories")
                                .object("github/" + name + ".json")
                                .stream(stream, stream.available(), -1)
                                .contentType("application/json")
                                .build();
                        minioClient.putObject(putObjectArgs);
                    } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                throw new RepositoryNotFoundException(Long.toString(repo.getId()));
            }
        }

        return repository;
    }


    // TODO: Improve this please, this is cursed.
    public List<Content> getContent(GHRepository repository) {
        Optional<String> ext;
        List<Content> contents = new ArrayList<>();
        try {
            List<GHContent> rootContent = repository.getDirectoryContent("/");
            rootContent = rootContent.stream().sorted(Comparator.comparing(GHContent::isDirectory, Comparator.reverseOrder())).collect(Collectors.toList());
            int itemsLength = 0;
            List<String> dirs = rootContent.stream().filter(GHContent::isDirectory).map(GHContent::getName).collect(Collectors.toList());
            Collections.shuffle(dirs);
            if (dirs.size() > 4) dirs = dirs.subList(0, 4);
            Content contentObj;
            for (GHContent content : rootContent) {

                List<Content> contentChildren = new ArrayList<>();
                Integer MAX_AMOUNT = 100;
                if (content.isDirectory() && dirs.contains(content.getName())) {
                    contentObj = new Content(
                            content.getName(),
                            FileType.DIRECTORY,
                            null,
                            null,

                            LIGHT_ICON_PATH.replace("{icon}", Utils.getCorrectIcon("", content.getName(), true)),
                            LIGHT_ICON_PATH.replace("{icon}", Utils.getCorrectIcon("", content.getName(), true) + "-open"),
                            DARK_ICON_PATH.replace("{icon}", Utils.getCorrectIcon("", content.getName(), true)),
                            DARK_ICON_PATH.replace("{icon}", Utils.getCorrectIcon("", content.getName(), true) + "-open")
                    );
                    List<GHContent> dirContent = getDirectoryContent(repository, content.getName());

                    List<String> dirContentDirs = dirContent.stream().filter(GHContent::isDirectory).map(GHContent::getName).collect(Collectors.toList());
                    Collections.shuffle(dirs);
                    if (dirContentDirs.size() > 3) dirContentDirs = dirContentDirs.subList(0, 3);
                    if (dirContent.size() > 24) {
                        dirContent = dirContent.subList(0, 24);
                    }
                    for (GHContent dirC : dirContent) {
                        if (dirC.isDirectory() && dirContentDirs.contains(dirC.getName())) {
                            List<Content> contentContentChildren = new ArrayList<>();
                            Content dirCContent = new Content(
                                    dirC.getName(),
                                    FileType.DIRECTORY,
                                    null,
                                    null,
                                    LIGHT_ICON_PATH.replace("{icon}", Utils.getCorrectIcon("", dirC.getName(), true)),
                                    LIGHT_ICON_PATH.replace("{icon}", Utils.getCorrectIcon("", dirC.getName(), true) + "-open"),
                                    DARK_ICON_PATH.replace("{icon}", Utils.getCorrectIcon("", dirC.getName(), true)),
                                    DARK_ICON_PATH.replace("{icon}", Utils.getCorrectIcon("", dirC.getName(), true) + "-open")
                            );
                            List<GHContent> dirCDirList = repository.getDirectoryContent(dirC.getPath());
                            if (dirCDirList.size() > 4) dirCDirList = dirCDirList.subList(0, 4);
                            for (GHContent dirCDir : dirCDirList) {
                                Optional<String> extension = Utils.getFileExtension(dirCDir.getName());
                                if (dirCDir.isDirectory()) {
                                    contentContentChildren.add(new Content(
                                            dirCDir.getName(),
                                            FileType.DIRECTORY,
                                            null,
                                            null,
                                            LIGHT_ICON_PATH.replace("{icon}", Utils.getCorrectIcon("", dirCDir.getName(), true)),
                                            LIGHT_ICON_PATH.replace("{icon}", Utils.getCorrectIcon("", dirCDir.getName(), true) + "-open"),
                                            DARK_ICON_PATH.replace("{icon}", Utils.getCorrectIcon("", dirCDir.getName(), true)),
                                            DARK_ICON_PATH.replace("{icon}", Utils.getCorrectIcon("", dirCDir.getName(), true) + "-open")
                                    ));
                                } else {
                                    extension.ifPresent(s -> contentContentChildren.add(new Content(
                                            dirCDir.getName(),
                                            FileType.FILE,
                                            "." + s,
                                            null,
                                            LIGHT_ICON_PATH.replace("{icon}", Utils.getCorrectIcon(s, dirCDir.getName(), false)),
                                            null,
                                            DARK_ICON_PATH.replace("{icon}", Utils.getCorrectIcon(s, dirCDir.getName(), false)),
                                            null
                                    )));
                                }

                            }
                            dirCContent.setChildren(contentContentChildren);
                            itemsLength += contentContentChildren.size();
                            contentChildren.add(dirCContent);
                        }

                        if (dirC.isFile() && itemsLength < MAX_AMOUNT) {
                            ext = Utils.getFileExtension(dirC.getName());
                            ext.ifPresent(s -> contentChildren.add(new Content(
                                    dirC.getName(),
                                    FileType.FILE,
                                    "." + s,
                                    null,
                                    LIGHT_ICON_PATH.replace("{icon}", Utils.getCorrectIcon(s, dirC.getName(), false)),
                                    null,
                                    DARK_ICON_PATH.replace("{icon}", Utils.getCorrectIcon(s, dirC.getName(), false)),
                                    null
                            )));


                        }
                    }
                    contentObj.setChildren(contentChildren);
                    itemsLength += contentChildren.size();
                    contents.add(contentObj);

                } else if (content.isFile() && itemsLength < MAX_AMOUNT) {
                    ext = Utils.getFileExtension(content.getName());
                    ext.ifPresent(s -> contents.add(new Content(
                            content.getName(),
                            FileType.FILE,
                            "." + s,
                            null,
                            LIGHT_ICON_PATH.replace("{icon}", Utils.getCorrectIcon(s, content.getName(), false)),
                            null,
                            DARK_ICON_PATH.replace("{icon}", Utils.getCorrectIcon(s, content.getName(), false)),
                            null
                    )));
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }


    public List<GHContent> getDirectoryContent(GHRepository repo, String dirPath) throws IOException {
        return repo.getDirectoryContent(dirPath);
    }
}
