package me.urninax.flagdelivery.flags.repositories;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.models.QFeatureFlag;
import me.urninax.flagdelivery.flags.ui.requests.ListAllFlagsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class FlagsRepositoryImpl implements FlagsRepositoryCustom{
    private final JPAQueryFactory factory;
    private final QFeatureFlag flag = QFeatureFlag.featureFlag;

    private static final Map<String, ComparableExpressionBase<?>> SORT_MAP = Map.of(
            "created_at", QFeatureFlag.featureFlag.createdAt,
            "name", QFeatureFlag.featureFlag.name,
            "key", QFeatureFlag.featureFlag.key
    );

    @Override
    public Page<FeatureFlag> findAllWithFilter(UUID projectId, ListAllFlagsRequest request, Pageable pageable){
        BooleanBuilder whereClause = new BooleanBuilder();
        whereClause.and(flag.project.id.eq(projectId));

        if(request != null){
            if(!request.query().isBlank()){
                whereClause.and(flag.name.containsIgnoreCase(request.query()))
                        .or(flag.key.containsIgnoreCase(request.query()))
                        .or(flag.description.containsIgnoreCase(request.query()));
            }

            if(!request.tags().isEmpty()){
                whereClause.and(flag.tags.any().id.tag.in(request.tags()));
            }

            if(!request.maintainer().isBlank()){
                try{
                    UUID maintainerId = UUID.fromString(request.maintainer());
                    whereClause.and(flag.maintainer.id.eq(maintainerId));
                }catch(IllegalArgumentException exc){
                    log.error("Invalid maintainer UUID provided");
                }
            }

            if(!request.type().isBlank() && (request.type().equals("temporary") || request.type().equals("permanent"))){
                boolean temporary = request.type().equals("temporary");
                whereClause.and(flag.temporary.eq(temporary));
            }
        }

        List<FeatureFlag> results = factory
                .selectFrom(flag)
                .where(whereClause)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(sanitizeSort(pageable.getSort()).toArray(new OrderSpecifier[0]))
                .fetch();

        long total = factory
                .select(flag.count())
                .from(flag)
                .where(whereClause)
                .fetchFirst();

        return new PageImpl<>(results, pageable, total);
    }

    private List<OrderSpecifier<?>> sanitizeSort(Sort sort){
        if(sort.isUnsorted()){
            return List.of(flag.createdAt.desc());
        }

        List<OrderSpecifier<?>> orderSpecifiers = new LinkedList<>();

        for(Sort.Order order : sort){
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            ComparableExpressionBase<?> path = SORT_MAP.get(order.getProperty());

            if (path == null){
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Sorting by "+order.getProperty()+" is not supported");
            }

            orderSpecifiers.add(new OrderSpecifier<>(direction, path));
        }

        return orderSpecifiers;
    }
}
