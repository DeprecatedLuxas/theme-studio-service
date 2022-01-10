package com.lucasnorgaard.tstudioservice.internal;

import com.lucasnorgaard.tstudioservice.Application;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.Getter;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MinIO {


    public static String TSTUDIO_REPOSITORIES = "tstudio-repositories";
    public static String TSTUDIO_ICONS = "tstudio-icons";
    @Getter
    public MinioClient minioClient;


    public MinIO() {
        String accessKey = System.getenv("MINIO_ACCESS");
        String secretKey = System.getenv("MINIO_SECRET");
        String minioEndpoint = System.getenv("MINIO_ENDPOINT");
        minioClient = MinioClient
                .builder()
                .endpoint(minioEndpoint)
                .credentials(
                        accessKey,
                        secretKey
                ).build();

        bucketExist(TSTUDIO_REPOSITORIES);
        bucketExist(TSTUDIO_ICONS);

    }

    public static String getName(String path, String version) {
        if (path.contains("https://raw.githubusercontent.com/DeprecatedLuxas/icon-mappings")) {
            int sub = path.indexOf("https://raw.githubusercontent.com/DeprecatedLuxas/icon-mappings/main/custom-icons/")
                    + "https://raw.githubusercontent.com/DeprecatedLuxas/icon-mappings/main/custom-icons/".length();
            return path.substring(sub);
        }
        int sub = path.indexOf("https://pkief.vscode-unpkg.net/PKief/material-icon-theme/" + version + "/extension/icons/")
                + ("https://pkief.vscode-unpkg.net/PKief/material-icon-theme/" + version + "/extension/icons/").length();
        return path.substring(sub);
    }

    private void bucketExist(String bucketName) {
        try {
            boolean exist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exist) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            } else {
                System.out.println("Found a bucket called " + bucketName);
            }
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
