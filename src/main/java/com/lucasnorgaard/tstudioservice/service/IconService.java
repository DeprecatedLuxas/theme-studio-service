package com.lucasnorgaard.tstudioservice.service;


import com.lucasnorgaard.tstudioservice.Application;
import com.lucasnorgaard.tstudioservice.Utils;
import com.lucasnorgaard.tstudioservice.models.ResponseIcon;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IconService {

    public List<String> getIcons() {
        return Application.validIcons.stream().map(u -> Application.HELPERS.getFormattedIconName(u, Application.VERSION).substring(1)).collect(Collectors.toList());
    }


//    public ResponseEntity<String> getIcon(String icon) {
//        OkHttpClient httpClient = Application.getHttpClient();
//
//        try {
//
//            if (icon.contains(".")) {
//                icon = icon.split("\\.")[0];
//            }
//
//            String url = "https://pkief.vscode-unpkg.net/PKief/material-icon-theme/%VERSION%/extension/icons/%ICON%.svg"
//                    .replaceAll("%VERSION%", Application.VERSION)
//                    .replaceAll("%ICON%", icon);
//
//            Request iconRequest = new Request.Builder().url(url).build();
//
//            try (Response iconResponse = httpClient.newCall(iconRequest).execute()) {
//
//
//                int code = iconResponse.code();
//
//                if (code != 200) {
//                    return ResponseEntity.status(404).header("Content-Type", "application/json")
//                            .body("{\"error\": \"The specified icon does not exist\"}");
//                }
//                icon = Objects.requireNonNull(iconResponse.body()).string();
//
//            }
//
//        } catch (IOException e) {
//            System.err.println(e.getMessage());
//            return ResponseEntity.status(404).header("Content-Type", "application/json")
//                    .body("{\"error\": \"The specified icon does not exist\"}");
//        }
//        return ResponseEntity.ok(icon);
//    }

    public ResponseEntity<?> getIcon(ResponseIcon responseIcon, boolean open) {
        OkHttpClient httpClient = Application.getHttpClient();
        String icon = "";
        try {

            String url = responseIcon.url;
            if (open && responseIcon.url_opened != null) {
                url = responseIcon.url_opened;
            }
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
        return ResponseEntity.ok().header("Content-Type", "image/svg+xml").body(icon);
    }

    public ResponseIcon getFileIcon(String name, String ext) {
        return Utils.getCorrectFileIcon(name, ext);
    }

    public ResponseIcon getIconByFolderName(String name) {
        return Utils.getCorrectFolderIcon(name);
    }

}
