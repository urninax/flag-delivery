package me.urninax.flagdelivery.organisation.repositories;

import me.urninax.flagdelivery.organisation.models.invitation.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface InvitationsRepository extends JpaRepository<Invitation, UUID>, JpaSpecificationExecutor<Invitation>{
    @Modifying
    @Query("update Invitation i set i.status='EXPIRED', i.updatedAt=:now where i.id = :id")
    void updateStatusExpired(UUID id, Instant now);
}
