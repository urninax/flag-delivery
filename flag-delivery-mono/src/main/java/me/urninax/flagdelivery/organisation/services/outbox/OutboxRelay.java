package me.urninax.flagdelivery.organisation.services.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.organisation.models.mailoutbox.MailOutboxEvent;
import me.urninax.flagdelivery.organisation.models.mailoutbox.MailStatus;
import me.urninax.flagdelivery.organisation.repositories.MailOutboxRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay{
    private final MailOutboxRepository mailOutboxRepository;
    private final OutboxWorker worker;

    @Scheduled(fixedDelayString = "PT5S")
    public void processOutbox(){
        List<MailOutboxEvent> events = mailOutboxRepository.findTop10ByStatusOrderByCreatedAtAsc(MailStatus.PENDING);

        for(MailOutboxEvent event : events){
            try{
                worker.processEvent(event);
            }catch(JsonProcessingException e){
                log.error("Error while processing JsonNode: {}: {}", event.getId(), e.getMessage());
                worker.markAsFailed(event.getId());
            }catch(Exception e){
                log.error("Failed to process outbox event {}: {}", event.getId(), e.getMessage());
                worker.markAsFailed(event.getId());
            }
        }
    }
}
