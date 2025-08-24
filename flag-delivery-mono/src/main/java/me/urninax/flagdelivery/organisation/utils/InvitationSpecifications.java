package me.urninax.flagdelivery.organisation.utils;

import jakarta.annotation.Nullable;
import me.urninax.flagdelivery.organisation.models.invitation.Invitation;
import me.urninax.flagdelivery.organisation.models.invitation.InvitationStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class InvitationSpecifications{
    private InvitationSpecifications(){}

    public static Specification<Invitation> byStatuses(@Nullable List<InvitationStatus> statuses){
        return (root, query, criteriaBuilder) ->
                (statuses == null || statuses.isEmpty())
                        ? criteriaBuilder.conjunction()
                        : root.get("status").in(statuses);
    }

    public static Specification<Invitation> byEmail(@Nullable String email){
        return ((root, query, criteriaBuilder) ->
                (email == null || email.isBlank())
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("email"), email)
        );
    }

    public static Specification<Invitation> createdBetween(@Nullable LocalDate from,
                                                           @Nullable LocalDate to,
                                                           ZoneId zone){
        return (root, query, criteriaBuilder) -> {
            if(from == null && to == null) return criteriaBuilder.conjunction();
            if(from != null && to != null){
                Instant fromInstant = from.atStartOfDay(zone).toInstant();
                Instant toInstant   = to.plusDays(1).atStartOfDay(zone).toInstant();
                return criteriaBuilder.between(root.get("createdAt"), fromInstant, toInstant);
            }

            if (from != null) {
                Instant fromInstant = from.atStartOfDay(zone).toInstant();
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromInstant);
            }

            Instant toInstant = to.plusDays(1).atStartOfDay(zone).toInstant();
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toInstant);
        };
    }

    public static Specification<Invitation> expiresBetween(@Nullable LocalDate from,
                                                           @Nullable LocalDate to,
                                                           ZoneId zone){
        return (root, query, criteriaBuilder) -> {
            if(from == null && to == null) return criteriaBuilder.conjunction();
            if(from != null && to != null){
                Instant fromInstant = from.atStartOfDay(zone).toInstant();
                Instant toInstant   = to.plusDays(1).atStartOfDay(zone).toInstant();
                return criteriaBuilder.between(root.get("expiresAt"), fromInstant, toInstant);
            }

            if (from != null) {
                Instant fromInstant = from.atStartOfDay(zone).toInstant();
                return criteriaBuilder.greaterThanOrEqualTo(root.get("expiresAt"), fromInstant);
            }

            Instant toInstant = to.plusDays(1).atStartOfDay(zone).toInstant();
            return criteriaBuilder.lessThanOrEqualTo(root.get("expiresAt"), toInstant);
        };
    }

    public static Specification<Invitation> byCreator(@Nullable String name){
        return (root, query, criteriaBuilder) -> {
            if(name == null || name.isBlank()){
                return criteriaBuilder.conjunction();
            }

            var invitedBy = root.join("invitedBy");

            var fullName = criteriaBuilder.concat(
                    criteriaBuilder.concat(invitedBy.get("firstName"), " "),
                    invitedBy.get("lastName")
            );

            return criteriaBuilder.like(criteriaBuilder.lower(fullName), "%" + name.toLowerCase() + "%");
        };
    }
}
