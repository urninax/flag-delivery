package me.urninax.flagdelivery.organisation.listeners;

import me.urninax.flagdelivery.organisation.events.invitation.InvitationAcceptedEvent;
import me.urninax.flagdelivery.organisation.events.invitation.InvitationCreatedEvent;
import me.urninax.flagdelivery.organisation.services.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class InvitationEventsListener{
    private final MailService mailService;

    @Autowired
    public InvitationEventsListener(MailService mailService){
        this.mailService = mailService;
    }

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
