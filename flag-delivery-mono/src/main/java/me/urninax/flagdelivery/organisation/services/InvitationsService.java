package me.urninax.flagdelivery.organisation.services;

import me.urninax.flagdelivery.organisation.models.invitation.Invitation;
import me.urninax.flagdelivery.organisation.repositories.InvitationsRepository;
import me.urninax.flagdelivery.organisation.shared.InvitationDTO;
import me.urninax.flagdelivery.user.utils.EntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class InvitationsService{
    private final InvitationsRepository invitationsRepository;
    private final EntityMapper entityMapper;

    @Autowired
    public InvitationsService(InvitationsRepository invitationsRepository, EntityMapper entityMapper){
        this.invitationsRepository = invitationsRepository;
        this.entityMapper = entityMapper;
    }

    @Transactional(readOnly = true)
    public InvitationDTO getInvitationDTO(String token){
        byte[] hashedToken = hashToken(token);
        Invitation invitation = invitationsRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));

        return entityMapper.toDTO(invitation);
    }

    private String generateToken() throws NoSuchAlgorithmException{
        byte[] bytes = new byte[32];
        SecureRandom sr = SecureRandom.getInstanceStrong();
        sr.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    byte[] hashToken(String rawToken){
        MessageDigest md;
        try{
            md = MessageDigest.getInstance("SHA-256");
        }catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
        return md.digest(rawToken.getBytes(StandardCharsets.US_ASCII));
    }
}
