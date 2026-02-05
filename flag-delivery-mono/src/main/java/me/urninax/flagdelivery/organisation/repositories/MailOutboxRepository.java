package me.urninax.flagdelivery.organisation.repositories;

import me.urninax.flagdelivery.organisation.models.mailoutbox.MailOutboxEvent;
import me.urninax.flagdelivery.organisation.models.mailoutbox.MailStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MailOutboxRepository extends JpaRepository<MailOutboxEvent, UUID>{
    List<MailOutboxEvent> findTop10ByStatusOrderByCreatedAtAsc(MailStatus status);
}
