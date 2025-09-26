package me.urninax.flagdelivery.projectsenvs.shared.environment;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import me.urninax.flagdelivery.projectsenvs.models.environment.EnvironmentTag;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

public class EnvironmentSpecifications{
    private EnvironmentSpecifications(){}

    public static Specification<Environment> byOrgAndProjectKey(UUID orgId, String projectKey){
        return (root, query, criteriaBuilder) -> {
            Join<Environment, Project> projectJoin = root.join("project");

            return criteriaBuilder.and(
                    criteriaBuilder.equal(projectJoin.get("organisationId"), orgId),
                    criteriaBuilder.equal(projectJoin.get("key"), projectKey)
            );
        };
    }

    public static Specification<Environment> hasAllTags(List<String> rawTags){
        return (root, query, cb) -> {
            if (rawTags == null || rawTags.isEmpty()) {
                return cb.conjunction();
            }

            List<String> tags = rawTags.stream()
                    .filter(t -> t != null && !t.isBlank())
                    .map(s -> s.trim().toLowerCase())
                    .distinct()
                    .toList();

            if (tags.isEmpty()) {
                return cb.conjunction();
            }

            Subquery<UUID> sq = query.subquery(UUID.class);
            Root<EnvironmentTag> pt = sq.from(EnvironmentTag.class);

            Expression<String> tagExpr = cb.lower(pt.get("id").get("tag"));

            sq.select(pt.get("environment").get("id"))
                    .where(tagExpr.in(tags))
                    .groupBy(pt.get("environment").get("id"))
                    .having(cb.equal(cb.countDistinct(tagExpr), (long) tags.size()));

            return root.get("id").in(sq);
        };
    }
}
