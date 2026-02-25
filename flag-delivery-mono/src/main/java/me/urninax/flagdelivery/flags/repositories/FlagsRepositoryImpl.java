package me.urninax.flagdelivery.flags.repositories;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.models.QFeatureFlag;
import me.urninax.flagdelivery.flags.ui.requests.ListAllFlagsRequest;
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
public class FlagsRepositoryImpl extends AbstractQueryDslRepository<FeatureFlag> implements FlagsRepositoryCustom{
    private static final QFeatureFlag flag = QFeatureFlag.featureFlag;

    public FlagsRepositoryImpl(JPAQueryFactory factory){
        super(factory, flag, Map.of(
                "created_at", QFeatureFlag.featureFlag.createdAt,
                "name", QFeatureFlag.featureFlag.name,
                "key", QFeatureFlag.featureFlag.key
        ), flag.createdAt.desc());
    }

    @Override
    public List<FeatureFlag> findAllWithFilter(UUID projectId, ListAllFlagsRequest request, Sort sort){
        return fetchList(applyFilters(projectId, request), sort);
    }

    @Override
    public Page<FeatureFlag> findPageWithFilter(UUID projectId, ListAllFlagsRequest request, Pageable pageable){
        return fetchPage(applyFilters(projectId, request), pageable);
    }

    private BooleanBuilder applyFilters(UUID projectId, ListAllFlagsRequest request){
        BooleanBuilder where = new BooleanBuilder();
        where.and(flag.project.id.eq(projectId));

        if(request != null){
            if(request.query() != null && !request.query().isBlank()){
                String term = request.query();
                where.and(
                        flag.name.containsIgnoreCase(term)
                                .or(flag.key.containsIgnoreCase(term))
                                .or(flag.description.containsIgnoreCase(term))
                );
            }

            applyCommonFilters(where, null, null, request.tags(), flag.name, flag.key, flag.tags);

            if(request.maintainer() != null){
                where.and(flag.maintainer.id.eq(request.maintainer()));
            }

            if (request.type() != null && !request.type().isBlank() &&
                    (request.type().equals("temporary") || request.type().equals("permanent"))) {

                boolean isTemporary = request.type().equals("temporary");
                where.and(flag.temporary.eq(isTemporary));
            }
        }

        return where;
    }
}
