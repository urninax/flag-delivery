package me.urninax.flagdelivery.projectsenvs.repositories.project;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import me.urninax.flagdelivery.projectsenvs.models.project.QProject;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.project.ListAllProjectsRequest;
import me.urninax.flagdelivery.shared.querydsl.AbstractQueryDslRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class ProjectsRepositoryImpl extends AbstractQueryDslRepository<Project> implements ProjectsRepositoryCustom{
    private static final QProject project = QProject.project;

    public ProjectsRepositoryImpl(JPAQueryFactory factory) {
        super(factory, project,
                Map.of(
                        "created_at", project.createdAt,
                        "name", project.name,
                        "key", project.key
                ),
                project.createdAt.desc()
        );
    }

    @Override
    public Page<Project> findPageWithFilter(UUID organisationId, ListAllProjectsRequest request, Pageable pageable){
        return fetchPage(applyFilters(organisationId, request), pageable);
    }

    @Override
    public List<Project> findAllWithFilter(UUID organisationId, ListAllProjectsRequest request, Sort sort){
        return fetchList(applyFilters(organisationId, request), sort);
    }

    private BooleanBuilder applyFilters(UUID organisationId, ListAllProjectsRequest request){
        BooleanBuilder where = new BooleanBuilder();
        where.and(project.organisationId.eq(organisationId));

        if(request != null){
            applyCommonFilters(where, request.query(), request.keys(), request.tags(),
                    project.name, project.key, project.tags);
        }

        return where;
    }
}
