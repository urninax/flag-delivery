package me.urninax.flagdelivery.projectsenvs.ui.controllers;

import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.projectsenvs.services.EnvironmentsService;
import me.urninax.flagdelivery.projectsenvs.shared.environment.EnvironmentDTO;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.environment.CreateEnvironmentRequest;
import me.urninax.flagdelivery.shared.security.enums.AuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresAuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectKey}/environments")
@RequiresAuthMethod(AuthMethod.ACCESS_TOKEN)
public class EnvironmentsController{

    private final EnvironmentsService environmentsService;

    public EnvironmentsController(EnvironmentsService environmentsService){
        this.environmentsService = environmentsService;
    }

    @PostMapping
    @RequiresRole(OrgRole.ADMIN)
    public ResponseEntity<?> createEnvironment(@PathVariable("projectKey") String projectKey,
                                               @RequestBody CreateEnvironmentRequest request){
        EnvironmentDTO environmentDTO = environmentsService.createEnvironment(projectKey, request);
        return new ResponseEntity<>(environmentDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{environmentKey}")
    @RequiresRole(OrgRole.READER)
    public ResponseEntity<?> getEnvironment(@PathVariable("projectKey") String projectKey,
                                            @PathVariable("environmentKey") String environmentKey){
        EnvironmentDTO environmentDTO = environmentsService.getEnvironment(projectKey, environmentKey);
        return new ResponseEntity<>(environmentDTO, HttpStatus.OK);
    }

    @GetMapping()
    @RequiresRole(OrgRole.READER)
    public ResponseEntity<?> listEnvironments(){
        return null;
    }

    @DeleteMapping
    @RequiresRole(OrgRole.ADMIN)
    public ResponseEntity<?> deleteEnvironment(){
        return null;
    }

    @PatchMapping
    @RequiresRole(OrgRole.ADMIN)
    public ResponseEntity<?> updateEnvironment(){
        return null;
    }
}

