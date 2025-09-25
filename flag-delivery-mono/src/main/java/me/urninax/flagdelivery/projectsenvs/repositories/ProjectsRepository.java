package me.urninax.flagdelivery.projectsenvs.repositories;

import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectsRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project>{
    Optional<Project> findByOrganisationIdAndKey(UUID organisationId, String key);

    @Query("SELECT p.id FROM Project p WHERE p.key = :key")
    Optional<UUID> findIdByKey(@Param("key") String key);

    void deleteByOrganisationIdAndKey(UUID organisationId, String key);
}
