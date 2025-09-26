package me.urninax.flagdelivery.flags.ui.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectKey}/flags")
public class FlagsController{

    @PostMapping
    public ResponseEntity<?> createFlag(){
        return null;
    }

    @GetMapping
    public ResponseEntity<?> listFlags(){
        return null;
    }

    @GetMapping("/{flagKey}")
    public ResponseEntity<?> getFlag(){
        return null;
    }
}
