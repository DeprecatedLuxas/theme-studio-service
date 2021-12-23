package com.lucasnorgaard.tstudioservice.service;


import com.lucasnorgaard.tstudioservice.Application;
import com.lucasnorgaard.tstudioservice.Utils;
import com.lucasnorgaard.tstudioservice.enums.FileType;
import com.lucasnorgaard.tstudioservice.exceptions.RepositoryNotFoundException;
import com.lucasnorgaard.tstudioservice.models.Content;
import com.lucasnorgaard.tstudioservice.models.Repository;
import lombok.Getter;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class GitHubService {

    private final String ICON_PATH = "http://104.248.169.204:8080/icon/{icon}";


    @Autowired
    @Getter
    private LanguageService languageService;


    // TODO: Add minio to also work with this.
    public Repository getRepoWithId(String id) {
        GHRepository repo;
        try {
            repo = Application.getGitHub().getRepositoryById(Long.parseLong(id));
        } catch (IOException e) {
            throw new RepositoryNotFoundException(id);
        }
        if (repo == null) {
            throw new RepositoryNotFoundException(id);
        }
        return new Repository(repo.getName(), repo.getOwnerName(), repo.getLanguage(), this.getContent(repo));
    }

    // TODO: Improve this please, this is cursed.
    public List<Content> getContent(GHRepository repository) {

        List<Content> contents = new ArrayList<>();
        String language = repository.getLanguage();
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
                            "",
                            "",
                            "",
                            ""
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
                                    "",
                                    "",
                                    "",
                                    ""
                            );
                            List<GHContent> dirCDirList = repository.getDirectoryContent(dirC.getPath());
                            if (dirCDirList.size() > 4) dirCDirList = dirCDirList.subList(0, 4);
                            for (GHContent dirCDir : dirCDirList) {
                                boolean isDir = dirCDir.getType().equals("dir");
                                Optional<String> extension = Utils.getFileExtension(dirCDir.getName());
                                extension.ifPresent(s -> contentContentChildren.add(new Content(
                                        dirCDir.getName(),
                                        isDir ? FileType.DIRECTORY : FileType.FILE,
                                        s,
                                        null,
                                        "",
                                        "",
                                        "",
                                        ""
                                )));
                            }
                            dirCContent.setChildren(contentContentChildren);
                            itemsLength += contentContentChildren.size();
                            contentChildren.add(dirCContent);
                        }

                        if (dirC.isFile() && itemsLength < MAX_AMOUNT) {
                            Content cc = new Content(
                                    dirC.getName(),
                                    FileType.FILE,
                                    null,
                                    null,
                                    "",
                                    "",
                                    "",
                                    ""
                            );

                            contentChildren.add(cc);

                        }
                    }
                    contentObj.setChildren(contentChildren);
                    itemsLength += contentChildren.size();
                    contents.add(contentObj);

                } else if (content.isFile() && itemsLength < MAX_AMOUNT) {
                    Content cc = new Content(
                            content.getName(),
                            FileType.FILE,
                            null,
                            null,
                            "",
                            "",
                            "",
                            ""
                    );

                    contents.add(cc);

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
