package me.urninax.flagdelivery.projectsenvs.repositories.project;

import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.project.ListAllProjectsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface ProjectsRepositoryCustom{
    Page<Project> findPageWithFilter(UUID organisationId, ListAllProjectsRequest request, Pageable pageable);
    List<Project> findAllWithFilter(UUID organisationId, ListAllProjectsRequest request, Sort sort);
}
