package me.urninax.flagdelivery.projectsenvs.ui.controllers;

import jakarta.validation.Valid;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.projectsenvs.services.ProjectsService;
import me.urninax.flagdelivery.projectsenvs.shared.project.ProjectDTO;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.CreateProjectRequest;
import me.urninax.flagdelivery.shared.security.enums.AuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresAuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiresAuthMethod(AuthMethod.ACCESS_TOKEN)
public class ProjectsController{

    private final ProjectsService projectsService;

    public ProjectsController(ProjectsService projectsService){
        this.projectsService = projectsService;
    }

    @PostMapping
    @RequiresRole(OrgRole.ADMIN)
    public ResponseEntity<?> createProject(@RequestBody @Valid CreateProjectRequest request){
        ProjectDTO createdProject = projectsService.createProject(request);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
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
