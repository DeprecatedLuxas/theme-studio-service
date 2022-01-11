package com.lucasnorgaard.tstudioservice.controllers;

import com.google.gson.JsonObject;
import com.lucasnorgaard.tstudioservice.Application;
import com.lucasnorgaard.tstudioservice.internal.MinIO;
import com.lucasnorgaard.tstudioservice.models.ResponseIcon;
import com.lucasnorgaard.tstudioservice.service.IconService;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.errors.MinioException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/icons")
public class IconController {

    private static final String BASE_URL = "https://pkief.vscode-unpkg.net/PKief/material-icon-theme/%VERSION%/extension/icons/%ICON%.svg"
            .replaceAll("%VERSION%", Application.VERSION);
    @Autowired
    @Getter
    private IconService iconService;

    @GetMapping(produces = "application/json")
    public List<String> getIcons() {
        return iconService.getIcons();
    }

    @GetMapping(value = "/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getIconsByVersion(@PathVariable String version) {
        MinIO minIO = Application.getMinIO();
        return ResponseEntity.ok(Application.GSON.toJson(minIO.getIconObjects(version)));
    }

    @GetMapping(value = "/{version}/{iconname}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> getIconByVersionAndName(@PathVariable String version, @PathVariable String iconname) {
        MinIO minIO = Application.getMinIO();
        Set<String> icons = minIO.getIconObjects(version);
        if (!iconname.endsWith(".svg")) {

            iconname = iconname + ".svg";
        }
        boolean found = false;
        String iconXML = null;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", "No icon was found with that name");
        for (String icon : icons) {
            if (icon.equals(iconname)) {
                found = true;
                break;
            }
        }
        if (!found) {
            return ResponseEntity.status(404).header("Content-Type", "application/json").body(Application.GSON.toJson(jsonObject));
        }

        GetObjectArgs getObjectArgs = new GetObjectArgs.Builder().bucket(MinIO.TSTUDIO_ICONS).object(version + "/" + iconname).build();
        try {
            GetObjectResponse getObjectResponse = minIO.getMinioClient().getObject(getObjectArgs);
            iconXML = new String(getObjectResponse.readAllBytes(), StandardCharsets.UTF_8);
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            System.out.println("Error occurred: " + e);
        }

        if (iconXML == null) {
            return ResponseEntity.status(404).header("Content-Type", "application/json").body(Application.GSON.toJson(jsonObject));
        }


        return ResponseEntity.ok(iconXML);
    }

    @GetMapping(value = "/folder/{foldername}", produces = {"image/svg+xml", "application/json"})
    public ResponseEntity<String> getIconByFolderName(@PathVariable String foldername,
                                                      @RequestParam(value = "open", required = false, defaultValue = "false") String open) {
        boolean isOpen = open.equals("true");

        ResponseIcon responseIcon = iconService.getIconByFolderName(foldername);

        responseIcon.url = BASE_URL.replaceAll("%ICON%", responseIcon.url);
        if (responseIcon.url_opened != null) {
            responseIcon.url_opened = BASE_URL.replaceAll("%ICON%", responseIcon.url_opened);
        }


        return iconService.getIcon(responseIcon, isOpen);
    }

    @GetMapping(value = "/version", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getVersion() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("version", Application.VERSION);
        return ResponseEntity.status(200).body(Application.GSON.toJson(jsonObject));
    }

    @GetMapping(value = "/file", produces = {"image/svg+xml", "application/json"})
    public ResponseEntity<String> getFileIcon(@RequestParam(value = "name", required = false, defaultValue = "") String name,
                                              @RequestParam(value = "ext", required = false, defaultValue = "") String ext) {
        System.out.println(name);
        ResponseIcon responseIcon = iconService.getFileIcon(name, ext);

        responseIcon.url = BASE_URL.replaceAll("%ICON%", responseIcon.url);
        if (responseIcon.url_opened != null) {
            responseIcon.url_opened = BASE_URL.replaceAll("%ICON%", responseIcon.url_opened);
        }


        return iconService.getIcon(responseIcon, false);
    }


}
