package me.urninax.flagdelivery.projectsenvs.repositories.environment;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import me.urninax.flagdelivery.projectsenvs.models.environment.QEnvironment;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.environment.ListAllEnvironmentsRequest;
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
public class EnvironmentsRepositoryImpl extends AbstractQueryDslRepository<Environment> implements EnvironmentsRepositoryCustom{
    private static final QEnvironment environment = QEnvironment.environment;

    public EnvironmentsRepositoryImpl(JPAQueryFactory factory) {
        super(factory, environment,
                Map.of(
                        "created_at", environment.createdAt,
                        "critical", environment.critical,
                        "name", environment.name,
                        "key", environment.key
                ),
                environment.createdAt.desc()
        );
    }

    @Override
    public Page<Environment> findPageWithFilter(UUID organisationId, String projectKey, ListAllEnvironmentsRequest request, Pageable pageable){
        BooleanBuilder whereClause = applyFilters(organisationId, projectKey, request);
        return fetchPage(whereClause, pageable);
    }

    @Override
    public List<Environment> findAllWithFilter(UUID organisationId, String projectKey, ListAllEnvironmentsRequest request, Sort sort){
        return fetchList(applyFilters(organisationId, projectKey, request), sort);
    }

    private BooleanBuilder applyFilters(UUID organisationId, String projectKey, ListAllEnvironmentsRequest request){
        BooleanBuilder whereClause = new BooleanBuilder();
        whereClause.and(environment.project.organisationId.eq(organisationId))
                .and(environment.project.key.eq(projectKey));

        if(request != null){
            applyCommonFilters(whereClause, request.query(), request.keys(), request.tags(),
                    environment.name, environment.key, environment.tags);
        }

        return whereClause;
    }
}
