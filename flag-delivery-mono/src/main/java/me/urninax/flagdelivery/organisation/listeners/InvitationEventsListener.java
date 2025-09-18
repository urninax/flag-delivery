package me.urninax.flagdelivery.organisation.listeners;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.events.invitation.InvitationAcceptedEvent;
import me.urninax.flagdelivery.organisation.events.invitation.InvitationCreatedEvent;
import me.urninax.flagdelivery.organisation.services.MailService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class InvitationEventsListener{
    private final MailService mailService;

    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInvitationCreated(InvitationCreatedEvent event){
        mailService.sendInvitation(event.getDto());
    }

    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInvitationAccepted(InvitationAcceptedEvent event){
        mailService.sendInviteAcceptedGreeting(event.getDto());
    }
}
