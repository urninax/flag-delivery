package me.urninax.flagdelivery.shared.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import me.urninax.flagdelivery.shared.exceptions.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractQueryDslRepository<T>{
    protected final JPAQueryFactory factory;
    private final EntityPathBase<T> entityPath;
    private final Map<String, ComparableExpressionBase<?>> sortMap;
    private final OrderSpecifier<?> defaultSort;

    protected AbstractQueryDslRepository(
            JPAQueryFactory factory,
            EntityPathBase<T> entityPath,
            Map<String, ComparableExpressionBase<?>> sortMap,
            OrderSpecifier<?> defaultSort) {
        this.factory = factory;
        this.entityPath = entityPath;
        this.sortMap = sortMap;
        this.defaultSort = defaultSort;
    }

    protected Page<T> fetchPage(BooleanBuilder where, Pageable pageable){
        List<T> results = factory
                .selectFrom(entityPath)
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(sanitizeSort(pageable.getSort()).toArray(new OrderSpecifier[0]))
                .fetch();

        long total = factory
                .select(entityPath.count())
                .from(entityPath)
                .where(where)
                .fetchFirst();

        return new PageImpl<>(results, pageable, total);
    }

    protected List<T> fetchList(BooleanBuilder where, Sort sort){
        return factory
                .selectFrom(entityPath)
                .where(where)
                .orderBy(sanitizeSort(sort).toArray(new OrderSpecifier[0]))
                .fetch();
    }

    private List<OrderSpecifier<?>> sanitizeSort(Sort sort) {
        if (sort.isUnsorted()) {
            return List.of(defaultSort);
        }
        List<OrderSpecifier<?>> specifiers = new LinkedList<>();
        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            ComparableExpressionBase<?> path = sortMap.get(order.getProperty());
            if (path == null) {
                throw new BadRequestException("Sorting by " + order.getProperty() + " unsupported");
            }
            specifiers.add(new OrderSpecifier<>(direction, path));
        }

        if(specifiers.isEmpty()){
            specifiers.add(defaultSort);
        }

        return specifiers;
    }

    protected void applyCommonFilters(
            BooleanBuilder where,
            String query,
            List<String> keys,
            List<String> tags,
            StringPath namePath,
            StringPath keyPath,
            Path<Set<String>> tagsPath) {

        if (query != null && !query.isBlank()) {
            where.and(namePath.containsIgnoreCase(query).or(keyPath.containsIgnoreCase(query)));
        }

        if (keys != null && !keys.isEmpty()) {
            where.and(keyPath.in(keys));
        }

        if (tags != null && !tags.isEmpty()) {
            List<String> normalizedTags = tags.stream()
                    .filter(t -> t != null && !t.isBlank())
                    .map(t -> t.trim().toLowerCase())
                    .distinct()
                    .toList();

            if (!normalizedTags.isEmpty()) {
                where.and(JsonbExpressions.containsAll(tagsPath, normalizedTags));
            }
        }
    }
}
