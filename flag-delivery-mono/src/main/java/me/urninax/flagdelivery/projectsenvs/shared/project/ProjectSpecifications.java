package me.urninax.flagdelivery.projectsenvs.shared.project;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import me.urninax.flagdelivery.projectsenvs.models.project.ProjectTag;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

public class ProjectSpecifications{
    private ProjectSpecifications(){}

    public static Specification<Project> byOrganisation(UUID organisationId){
        return ((root, cq, cb) -> cb.equal(root.get("organisationId"), organisationId));
    }

    public static Specification<Project> hasQuery(String query){
        return ((root, cq, cb) -> cb.or(
            cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%"),
            cb.like(cb.lower(root.get("key")), "%" + query.toLowerCase() + "%")
        ));
    }

    public static Specification<Project> hasAllTags(List<String> rawTags){
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
            Root<ProjectTag> pt = sq.from(ProjectTag.class);

            Expression<String> tagExpr = cb.lower(pt.get("id").get("tag"));

            sq.select(pt.get("project").get("id"))
                    .where(tagExpr.in(tags))
                    .groupBy(pt.get("project").get("id"))
                    .having(cb.equal(cb.countDistinct(tagExpr), (long) tags.size()));

            return root.get("id").in(sq);
        };
    }

    public static Specification<Project> hasAnyKeyLike(List<String> keys){
        return (root, cq, cb) -> root.get("key").in(keys);
    }
}
