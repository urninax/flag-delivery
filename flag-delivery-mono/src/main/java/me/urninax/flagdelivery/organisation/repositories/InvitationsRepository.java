package me.urninax.flagdelivery.organisation.repositories;

import me.urninax.flagdelivery.organisation.models.invitation.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationsRepository extends JpaRepository<Invitation, UUID>{
    Optional<Invitation> findByTokenHash(byte[] tokenHash);
}
