package me.urninax.flagdelivery.projectsenvs.ui.controllers;

import me.urninax.flagdelivery.projectsenvs.ui.models.requests.CreateProjectRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectsController{

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody CreateProjectRequest request){
        return null;
    }

    @GetMapping
    public ResponseEntity<?> listProjects(){
        return null;
    }

    @GetMapping("/{projectKey}")
    public ResponseEntity<?> getProject(){
        return null;
    }

    @PatchMapping("/{projectKey}")
    public ResponseEntity<?> patchProject(){
        return null;
    }
}
