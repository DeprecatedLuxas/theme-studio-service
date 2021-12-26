package com.lucasnorgaard.tstudioservice.internal;

import com.lucasnorgaard.tstudioservice.Application;
import com.lucasnorgaard.tstudioservice.models.IconPack;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import org.kohsuke.github.GHContent;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IconPackTask implements Runnable {

    public void run() {
        try {
            System.out.println("Running Material Task");

            List<String> missingIconNames = new ArrayList<>();
            missingIconNames.add("folder.svg");
            missingIconNames.add("folder-open.svg");
            missingIconNames.add("folder-root.svg");
            missingIconNames.add("folder-root-open.svg");


            List<GHContent> olderIcons = Application.getGitHub().getRepository("pkief/vscode-material-icon-theme").getDirectoryContent("icons", "4f703960b3214c97c448157f034e778aaed9604e");
            olderIcons = olderIcons.stream().filter(c -> missingIconNames.contains(c.getName())).collect(Collectors.toList());

            // Getting the latest icons.
            List<GHContent> icons = Application.getGitHub().getRepository("pkief/vscode-material-icon-theme").getDirectoryContent("icons");
            List<GHContent> mergedIcons = Stream.of(olderIcons, icons).flatMap(Collection::stream).collect(Collectors.toList());

            List<IconPack> iconPackList = mergedIcons.stream().map(icon -> {
                try {
                    return new IconPack(icon.getName(), icon.read());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());

            iconPackList.forEach(iconPack -> {
                try {
                    PutObjectArgs args = PutObjectArgs.builder()
                            .bucket("tstudio-iconpacks")
                            .object(iconPack.getName())
                            .stream(iconPack.getContent(), iconPack.getContent().available(), -1)
                            .contentType("image/svg+xml")
                            .build();
                    Application.getMinIO().getMinioClient().putObject(args);
                } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("Material Task completed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
