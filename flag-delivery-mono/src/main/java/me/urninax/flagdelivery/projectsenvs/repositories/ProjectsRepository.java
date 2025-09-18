package me.urninax.flagdelivery.projectsenvs.repositories;

import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectsRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project>{
    Optional<Project> findByOrganisationIdAndKey(UUID organisationId, String key);
    void deleteByOrganisationIdAndKey(UUID organisationId, String key);
}
