package me.urninax.flagdelivery.organisation.services;

import me.urninax.flagdelivery.organisation.repositories.InvitationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class InvitationExpiryService{

    private final InvitationsRepository invitationsRepository;

    @Autowired
    public InvitationExpiryService(InvitationsRepository invitationsRepository){
        this.invitationsRepository = invitationsRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markExpiredInNewTx(UUID id){
        invitationsRepository.updateStatusExpired(id, Instant.now());
    }
}
