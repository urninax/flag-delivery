package me.urninax.flagdelivery.organisation.repositories;

import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.user.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MembershipsRepository extends JpaRepository<Membership, UUID>{
    UUID user(UserEntity user);
}
