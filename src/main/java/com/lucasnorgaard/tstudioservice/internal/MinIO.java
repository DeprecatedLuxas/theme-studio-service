package com.lucasnorgaard.tstudioservice.internal;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.Getter;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MinIO {


    @Getter
    public MinioClient minioClient;


    public MinIO() {
        try {
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


            boolean exist = minioClient.bucketExists(BucketExistsArgs.builder().bucket("tstudio-repositories").build());
            if (!exist) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("tstudio-repositories").build());
            } else {
                System.out.println("Found a bucket called " + "tstudio-repositories");
            }
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
