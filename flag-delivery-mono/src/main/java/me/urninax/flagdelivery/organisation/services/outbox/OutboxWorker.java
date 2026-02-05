package me.urninax.flagdelivery.organisation.services.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.models.mailoutbox.MailOutboxEvent;
import me.urninax.flagdelivery.organisation.models.mailoutbox.MailStatus;
import me.urninax.flagdelivery.organisation.repositories.MailOutboxRepository;
import me.urninax.flagdelivery.organisation.services.MailService;
import me.urninax.flagdelivery.organisation.shared.InvitationMailDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxWorker{
    private final MailOutboxRepository repository;
    private final MailService mailService;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processEvent(MailOutboxEvent event) throws JsonProcessingException{
        try{
            switch(event.getType()){
                case "INVITATION_CREATED" -> {
                    InvitationMailDTO invitationMailDTO = objectMapper.treeToValue(event.getPayload(), InvitationMailDTO.class);
                    mailService.sendInvitation(invitationMailDTO);
                }
                case "INVITATION_ACCEPTED" -> {
                    InvitationMailDTO invitationMailDTO = objectMapper.treeToValue(event.getPayload(), InvitationMailDTO.class);
                    mailService.sendInviteAcceptedGreeting(invitationMailDTO);
                }
                default -> throw new IllegalArgumentException("Unknown type: "+event.getType());
            }
            event.setStatus(MailStatus.PROCESSED);
            repository.save(event);
        }catch(Exception exc){
            throw new RuntimeException(exc);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsFailed(UUID eventId){
        repository.findById(eventId).ifPresent(event -> {
            int newRetryCount = event.getRetryCount() + 1;
            event.setRetryCount(newRetryCount);

            if(newRetryCount >= 5){
                event.setStatus(MailStatus.FAILED);
            }
            repository.save(event);
        });
    }
}
