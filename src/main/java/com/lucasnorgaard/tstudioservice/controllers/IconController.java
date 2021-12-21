package com.lucasnorgaard.tstudioservice.controllers;

import com.lucasnorgaard.tstudioservice.Application;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/icon")
public class IconController {


    @GetMapping(value = "/{icon}", produces = {"image/svg+xml", "application/json"})
    public ResponseEntity<String> getIcon(@PathVariable String icon) {
        MinioClient minioClient = Application.getMinIO().getMinioClient();
        try {
            if (icon.contains(".")) {
                icon = icon.split("\\.")[0];
            }
            GetObjectArgs args = GetObjectArgs.builder().bucket("tstudio-iconpacks").object("material/" + icon + ".svg").build();
            InputStream stream = minioClient.getObject(args);
            icon = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            stream.close();
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(404).header("Content-Type", "application/json")
                    .body("{\"error\": \"The specified icon does not exist\"}");
        }
        return ResponseEntity.ok(icon);
    }

}
