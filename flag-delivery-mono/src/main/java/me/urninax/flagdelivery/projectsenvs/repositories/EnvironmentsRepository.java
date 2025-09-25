package me.urninax.flagdelivery.projectsenvs.repositories;

import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnvironmentsRepository extends JpaRepository<Environment, UUID>{
    @Query(value = """
        select e.*
        from Environment e
        join Project p on p.id = e.project_id
        where p.organisation_id = :orgId
            and p.key = :projectKey
            and e.key = :environmentKey
    """, nativeQuery = true)
    Optional<Environment> findEnvironment(@Param("orgId") UUID orgId,
                                         @Param("projectKey") String projectKey,
                                         @Param("environmentKey") String environmentKey);
}
