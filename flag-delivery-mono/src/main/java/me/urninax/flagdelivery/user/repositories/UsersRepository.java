package me.urninax.flagdelivery.user.repositories;

import me.urninax.flagdelivery.organisation.utils.projections.UserOrgProjection;
import me.urninax.flagdelivery.user.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<UserEntity, UUID>{
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
        SELECT u.id AS id,
        m.id.organisationId as organisationId
        FROM UserEntity u
        LEFT JOIN u.membership m
        WHERE u.id = :userId
    """)
    Optional<UserOrgProjection> findProjectedById(UUID userId);
}
