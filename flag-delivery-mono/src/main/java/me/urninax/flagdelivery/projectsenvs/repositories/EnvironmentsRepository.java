package me.urninax.flagdelivery.projectsenvs.repositories;

import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnvironmentsRepository extends JpaRepository<Environment, UUID>, JpaSpecificationExecutor<Environment>{
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

    @Override
    @EntityGraph(attributePaths = "tags")
    List<Environment> findAll(Specification<Environment> spec, Sort sort);
}
