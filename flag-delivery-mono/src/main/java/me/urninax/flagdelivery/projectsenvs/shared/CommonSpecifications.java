package me.urninax.flagdelivery.projectsenvs.shared;

import org.springframework.data.jpa.domain.Specification;

public class CommonSpecifications{
    public static <T> Specification<T> hasQuery(String query){
        return ((root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%"),
                cb.like(cb.lower(root.get("key")), "%" + query.toLowerCase() + "%")
        ));
    }
}
