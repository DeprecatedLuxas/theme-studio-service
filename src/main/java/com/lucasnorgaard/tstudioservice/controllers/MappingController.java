package com.lucasnorgaard.tstudioservice.controllers;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lucasnorgaard.tstudioservice.Application;
import com.lucasnorgaard.tstudioservice.models.Mapping;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mappings")
public class MappingController {

    private final List<String> INCLUDE = List.of("file_mappings.json", "folder_mappings.json", "language_mappings.json");

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMappings() {
        List<String> mappingNames = new ArrayList<>();
        try {
            GitHub gitHub = Application.getGitHub();
            GHRepository repository = gitHub.getRepository("DeprecatedLuxas/icon-mappings");

            mappingNames = repository.getDirectoryContent("")
                    .stream()
                    .map(GHContent::getName)
                    .filter(INCLUDE::contains)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
            if (e instanceof GHFileNotFoundException) {
                System.out.println("DeprecatedLuxas/icon-mappinggs was not found");
                return ResponseEntity.status(404).header("Content-Type", "application/json")
                        .body("{\"error\": \"GitHub 'DeprecatedLuxas/icon-mappings' not found\"}");
            }

        }
//        if (mappingNames.size() < 1) {
//            return ResponseEntity.status(404).header("Content-Type", "application/json")
//                    .body("{\"error\": \"No mappings found\"}");
//        }
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        Gson gson = new Gson();
        return ResponseEntity.ok(gson.toJson(mappingNames, listType));
    }

    @GetMapping(value = "/{mapping}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMappingByName(@PathVariable String mapping) {
        String mappingStr = null;
        try {

            if (!INCLUDE.contains(mapping)) {
                System.out.println("Mapping " + mapping + " was not INCLUDE list.");
                return ResponseEntity.status(404).header("Content-Type", "application/json")
                        .body("{\"error\": \"Mapping was not found\"}");
            }
            GitHub gitHub = Application.getGitHub();
            GHRepository repository = gitHub.getRepository("DeprecatedLuxas/icon-mappings");

            GHContent content = repository.getFileContent(mapping);
            mappingStr = new String(content.read().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            if (e instanceof  GHFileNotFoundException) {
                System.out.println("Mapping " + mapping + " was not found");
                return ResponseEntity.status(500).header("Content-Type", "application/json")
                        .body("{\"error\": \"Internal Server Error\"}");
            }
            e.printStackTrace();
        }
        if (mappingStr == null) {
            System.out.println("Mapping " + mapping + ".json was null");
            return ResponseEntity.status(500).header("Content-Type", "application/json")
                    .body("{\"error\": \"Internal Server Error\"}");
        }

        return ResponseEntity.ok(mappingStr);
    }
}
