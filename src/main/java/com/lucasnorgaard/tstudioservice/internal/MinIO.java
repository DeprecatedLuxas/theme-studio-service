package com.lucasnorgaard.tstudioservice.internal;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.Getter;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MinIO {


    @Getter
    public MinioClient minioClient;


    public MinIO() {
        //
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


            checkBucketExist("tstudio-iconpacks");
            checkBucketExist("tstudio-repositories");
    }

    public void checkBucketExist(String bucketName) {
        try {
            boolean bucketExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExist) {
                  minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            } else {
                System.out.println("Found a bucket called " + bucketName);
            }
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
