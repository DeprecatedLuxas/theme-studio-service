package com.lucasnorgaard.tstudioservice.controllers;

import com.lucasnorgaard.tstudioservice.models.Repository;
import com.lucasnorgaard.tstudioservice.service.GitHubService;
import com.lucasnorgaard.tstudioservice.service.GitLabService;
import lombok.Getter;
import org.kohsuke.github.GHRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/repo")
public class RepoController {

    @Autowired
    @Getter
    private GitHubService gitHubService;

    @Autowired
    @Getter
    private GitLabService gitLabService;

    @GetMapping(value = "/github/{id}", produces = "application/json")
    public ResponseEntity<Repository> getGitHubRepo(@PathVariable String id) {
        return ResponseEntity.ok(getGitHubService().getRepoWithId(id));
    }

    // @GetMapping(value = "/gitlab/{id}", produces = "application/json")
    // public ResponseEntity<Repository> getGitLabRepo(@PathVariable String id) {
    //     return ResponseEntity.ok(getGitLabService().getRepoWithId(id));
    // }

}
