package me.urninax.flagdelivery.organisation.repositories;

import me.urninax.flagdelivery.organisation.models.membership.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MembershipsRepository extends JpaRepository<Membership, UUID>{
}
