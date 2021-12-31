package com.lucasnorgaard.tstudioservice.controllers;

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





    @GetMapping(value = "/{icon}", produces = {"image/svg+xml", "application/json"})
    public ResponseEntity<String> getIcon(@PathVariable String icon) {
        return iconService.getIcon(icon);
    }

}
