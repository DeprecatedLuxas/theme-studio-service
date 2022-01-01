package com.lucasnorgaard.tstudioservice.controllers;

import com.lucasnorgaard.tstudioservice.Application;
import com.lucasnorgaard.tstudioservice.models.ResponseIcon;
import com.lucasnorgaard.tstudioservice.service.IconService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/icons")
public class IconController {

    @Autowired
    @Getter
    private IconService iconService;

    @GetMapping(produces = "application/json")
    public List<String> getIcons() {
        return iconService.getIcons();
    }


    private static final String BASE_URL = "https://pkief.vscode-unpkg.net/PKief/material-icon-theme/%VERSION%/extension/icons/%ICON%.svg"
            .replaceAll("%VERSION%", Application.VERSION);


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
