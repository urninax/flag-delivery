package me.urninax.flagdelivery.projectsenvs.ui.controllers;

import me.urninax.flagdelivery.projectsenvs.services.ProjectsService;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.project.NamingConventionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectKey}/settings")
public class ProjectsSettingsController{

    private final ProjectsService projectsService;

    public ProjectsSettingsController(ProjectsService projectsService){
        this.projectsService = projectsService;
    }

    @PatchMapping("/flags")
    public ResponseEntity<?> editFlagsSettings(@PathVariable("projectKey") String projectKey,
                                               @RequestBody NamingConventionRequest request){
        projectsService.editProjectFlagsSettings(projectKey, request);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
