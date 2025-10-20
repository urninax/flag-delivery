package me.urninax.flagdelivery.organisation.repositories;

import me.urninax.flagdelivery.organisation.models.membership.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipsRepository extends JpaRepository<Membership, UUID>, MembershipsRepositoryCustom{
    Optional<Membership> findByUserIdAndOrganisation_Id(UUID userId, UUID organisationId);
    Optional<Membership> findByUser_Email(String email);
    boolean existsByUserIdAndOrganisation_Id(UUID userId, UUID organisationId);
}
