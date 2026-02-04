package me.urninax.flagdelivery.projectsenvs.ui.controllers;

import jakarta.validation.Valid;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.projectsenvs.services.ProjectsService;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.project.NamingConventionRequest;
import me.urninax.flagdelivery.shared.security.enums.AuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresAuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectKey}/settings")
@RequiresAuthMethod(AuthMethod.ACCESS_TOKEN)
public class ProjectsSettingsController{

    private final ProjectsService projectsService;

    public ProjectsSettingsController(ProjectsService projectsService){
        this.projectsService = projectsService;
    }

    @PatchMapping("/flags")
    @RequiresRole(OrgRole.ADMIN)
    public ResponseEntity<?> editFlagsSettings(@PathVariable String projectKey,
                                               @Valid @RequestBody NamingConventionRequest request){
        projectsService.editProjectFlagsSettings(projectKey, request);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
