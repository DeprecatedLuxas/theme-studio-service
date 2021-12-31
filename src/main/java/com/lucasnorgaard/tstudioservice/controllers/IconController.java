package com.lucasnorgaard.tstudioservice.controllers;

import com.lucasnorgaard.tstudioservice.Application;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/icon")
public class IconController {


    @GetMapping(value = "/{icon}", produces = {"image/svg+xml", "application/json"})
    public ResponseEntity<String> getIcon(@PathVariable String icon) {
        OkHttpClient httpClient = Application.getHttpClient();

        try {

            if (icon.contains(".")) {
                icon = icon.split("\\.")[0];
            }

            String url = "https://pkief.vscode-unpkg.net/PKief/material-icon-theme/%VERSION%/extension/icons/%ICON%.svg"
                    .replaceAll("%VERSION%", Application.VERSION)
                    .replaceAll("%ICON%", icon);

            Request iconRequest = new Request.Builder().url(url).build();

            try (Response iconResponse = httpClient.newCall(iconRequest).execute()) {


                int code = iconResponse.code();

                if (code != 200) {
                    return ResponseEntity.status(404).header("Content-Type", "application/json")
                            .body("{\"error\": \"The specified icon does not exist\"}");
                }
                icon = Objects.requireNonNull(iconResponse.body()).string();

            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(404).header("Content-Type", "application/json")
                    .body("{\"error\": \"The specified icon does not exist\"}");
        }
        return ResponseEntity.ok(icon);
    }

}
