package me.urninax.flagdelivery.organisation.repositories;

import me.urninax.flagdelivery.organisation.models.AccessToken;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, UUID>{
    Page<AccessToken> findAllByOrganisation_Id(UUID orgId, Pageable pageable);
    Page<AccessToken> findAllByOwner_IdAndOrganisation_Id(UUID ownerId, UUID orgId, Pageable pageable);
    Optional<AccessToken> findByHashedToken(String hashedToken);
    @Modifying
    @Query("""
        update AccessToken t
           set t.role = :newRole
         where t.owner.id = :userId
           and t.isService = false
           and t.role > :newRole
    """)
    int downgradeUserTokens(UUID userId, OrgRole newRole);
}
