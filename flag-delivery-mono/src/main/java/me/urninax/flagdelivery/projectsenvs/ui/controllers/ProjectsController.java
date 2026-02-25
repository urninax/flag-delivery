package me.urninax.flagdelivery.projectsenvs.ui.controllers;

import jakarta.validation.Valid;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.projectsenvs.services.ProjectsService;
import me.urninax.flagdelivery.projectsenvs.shared.project.ProjectDTO;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.project.CreateProjectRequest;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.project.ListAllProjectsRequest;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.project.PatchProjectRequest;
import me.urninax.flagdelivery.shared.security.enums.AuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresAuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresRole;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @RequiresRole(OrgRole.READER)
    public ResponseEntity<?> listProjects(@RequestParam(value = "filter", required = false) ListAllProjectsRequest request,
                                          Sort sort){
        List<ProjectDTO> projectsDTOs = projectsService.getProjects(request, sort);
        return new ResponseEntity<>(projectsDTOs, HttpStatus.OK);
    }

    @GetMapping("/{projectKey}")
    @RequiresRole(OrgRole.READER)
    public ResponseEntity<?> getProject(@PathVariable String projectKey,
                                        @RequestParam(required = false) String expand){
        ProjectDTO projectDTO = projectsService.getProject(projectKey, expand);
        return new ResponseEntity<>(projectDTO, HttpStatus.OK);
    }

    @PatchMapping("/{projectKey}")
    @RequiresRole(OrgRole.ADMIN)
    public ResponseEntity<?> patchProject(@PathVariable String projectKey,
                                          @Valid @RequestBody PatchProjectRequest request){
        projectsService.patchProject(projectKey, request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{projectKey}")
    @RequiresRole(OrgRole.OWNER)
    public ResponseEntity<?> deleteProject(@PathVariable String projectKey){
        projectsService.deleteProject(projectKey);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
