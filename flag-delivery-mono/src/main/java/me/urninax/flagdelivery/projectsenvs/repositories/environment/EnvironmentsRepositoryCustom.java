package me.urninax.flagdelivery.projectsenvs.repositories.environment;

import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.environment.ListAllEnvironmentsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface EnvironmentsRepositoryCustom{
    Page<Environment> findPageWithFilter(UUID organisationId, String projectKey, ListAllEnvironmentsRequest request, Pageable pageable);
    List<Environment> findAllWithFilter(UUID organisationId, String projectKey, ListAllEnvironmentsRequest request, Sort sort);
}
