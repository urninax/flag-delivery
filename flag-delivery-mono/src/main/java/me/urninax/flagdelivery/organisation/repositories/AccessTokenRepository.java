package me.urninax.flagdelivery.organisation.repositories;

import me.urninax.flagdelivery.organisation.models.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, UUID>{
    
}
