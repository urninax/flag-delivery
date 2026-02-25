package me.urninax.flagdelivery.projectsenvs.repositories.environment;

import me.urninax.flagdelivery.flags.utils.FlagConfigEnvironmentProjection;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface EnvironmentsRepository extends JpaRepository<Environment, UUID>, EnvironmentsRepositoryCustom{
    @Query(value = """
        select e
        from Environment e
        join e.project p
        where p.organisationId = :orgId
            and p.key = :projectKey
            and e.key = :environmentKey
    """)
    Optional<Environment> findEnvironment(@Param("orgId") UUID orgId,
                                         @Param("projectKey") String projectKey,
                                         @Param("environmentKey") String environmentKey);

    Set<FlagConfigEnvironmentProjection> findAllByProject_Id(UUID projectId);

    @Query("""
        select count(e)
        from Environment e
        join e.project p
        where p.organisationId = :orgId
            and p.key = :projectKey
    """)
    int countEnvironmentByOrgIdAndProjectKey(UUID orgId, String projectKey);
}
