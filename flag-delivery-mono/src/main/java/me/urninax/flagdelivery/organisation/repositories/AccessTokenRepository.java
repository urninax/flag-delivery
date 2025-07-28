package me.urninax.flagdelivery.organisation.repositories;

import me.urninax.flagdelivery.organisation.models.AccessToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, UUID>{
    Page<AccessToken> findAllByOrganisation_Id(UUID orgId, Pageable pageable);
    Page<AccessToken> findAllByOwner_IdAndOrganisation_Id(UUID ownerId, UUID orgId, Pageable pageable);
}
