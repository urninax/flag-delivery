package me.urninax.flagdelivery.organisation.repositories;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.shared.MemberWithActivityDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MembershipsRepositoryImpl implements MembershipsRepositoryCustom{
    private final EntityManager em;


    @Override
    public Page<MemberWithActivityDTO> findMembers(UUID orgId, @Nullable Instant from, @Nullable List<OrgRole> roles, Pageable pageable){
        var cb = em.getCriteriaBuilder();

        var cq = cb.createQuery(MemberWithActivityDTO.class);
        var m = cq.from(Membership.class);
        var u = m.join("user", JoinType.INNER);
        var ua = u.join("userActivity", JoinType.LEFT);

        var predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(m.get("organisation").get("id"), orgId));

        if(from != null){
            predicates.add(cb.greaterThanOrEqualTo(ua.get("lastSeen"), from));
        }
        if(roles != null && !roles.isEmpty()){
            predicates.add(m.get("role").in(roles));
        }

        var fullName = cb.concat(
                cb.coalesce(u.get("firstName"), ""),
                cb.concat(" ", cb.coalesce(u.get("lastName"), "")));

        cq.select(cb.construct(
                MemberWithActivityDTO.class,
                u.get("id"),
                fullName,
                u.get("email"),
                m.get("role"),
                ua.get("lastSeen")
        ));

        cq.where(predicates.toArray(new Predicate[0]));
        cq.distinct(true);

        if (pageable.getSort().isSorted()) {
            var orders = pageable.getSort().stream()
                    .map(order -> {
                        Expression<?> expr = switch (order.getProperty()) {
                            case "email"    -> u.get("email");
                            case "role"     -> m.get("role");
                            case "lastSeen" -> ua.get("lastSeen");
                            case "name"     -> fullName;
                            default         -> u.get("id");
                        };
                        return order.isAscending() ? cb.asc(expr) : cb.desc(expr);
                    }).toList();
            cq.orderBy(orders);
        }

        var query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        var content = query.getResultList();

        var countCq = cb.createQuery(Long.class);
        var m2  = countCq.from(Membership.class);
        var u2  = m2.join("user", JoinType.INNER);
        var ua2 = u2.join("userActivity", JoinType.LEFT);

        var countPreds = new java.util.ArrayList<Predicate>();
        countPreds.add(cb.equal(m2.get("organisation").get("id"), orgId));
        if (from != null) {
            countPreds.add(cb.greaterThanOrEqualTo(ua2.get("lastSeen"), from));
        }
        if (roles != null && !roles.isEmpty()) {
            countPreds.add(m2.get("role").in(roles));
        }
        countCq.select(cb.countDistinct(u2.get("id")));
        countCq.where(countPreds.toArray(Predicate[]::new));
        long total = em.createQuery(countCq).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}
