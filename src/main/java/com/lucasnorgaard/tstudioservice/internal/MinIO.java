package com.lucasnorgaard.tstudioservice.internal;

import com.lucasnorgaard.tstudioservice.Application;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.Getter;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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

    public Set<String> getIconObjects(String version) {
        Set<String> icons = new LinkedHashSet<>();
        try {
            ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder().bucket(TSTUDIO_ICONS).recursive(true).build();
            Iterable<Result<Item>> results = minioClient.listObjects(listObjectsArgs);
            for (Result<Item> result : results) {
                Item item = result.get();
                String name = item.objectName();
                if (name.startsWith(version)) icons.add(name.split("/")[1]);
            }
            return icons;
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            System.out.println("Error occurred: " + e);
        }
        return icons;
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
