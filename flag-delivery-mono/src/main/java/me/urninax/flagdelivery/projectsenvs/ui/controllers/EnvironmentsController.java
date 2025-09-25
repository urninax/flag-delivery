package me.urninax.flagdelivery.projectsenvs.ui.controllers;

import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.projectsenvs.services.EnvironmentsService;
import me.urninax.flagdelivery.projectsenvs.shared.environment.EnvironmentDTO;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.environment.CreateEnvironmentRequest;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectKey}/environments")
public class EnvironmentsController{

    private final EnvironmentsService environmentsService;

    public EnvironmentsController(EnvironmentsService environmentsService){
        this.environmentsService = environmentsService;
    }

    @GetMapping
    @RequiresRole(OrgRole.READER)
    public ResponseEntity<?> getEnvironments(){
        return null;
    }

    @PostMapping
    @RequiresRole(OrgRole.ADMIN)
    public ResponseEntity<?> createEnvironment(@PathVariable("projectKey") String projectKey,
                                               @RequestBody CreateEnvironmentRequest request){
        EnvironmentDTO environmentDTO = environmentsService.createEnvironment(projectKey, request);
        return new ResponseEntity<>(environmentDTO, HttpStatus.CREATED);
    }
}

