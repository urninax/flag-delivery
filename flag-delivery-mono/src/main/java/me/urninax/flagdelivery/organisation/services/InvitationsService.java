package me.urninax.flagdelivery.organisation.services;

import me.urninax.flagdelivery.organisation.models.Organisation;
import me.urninax.flagdelivery.organisation.models.invitation.Invitation;
import me.urninax.flagdelivery.organisation.models.invitation.InvitationStatus;
import me.urninax.flagdelivery.organisation.repositories.InvitationsRepository;
import me.urninax.flagdelivery.organisation.repositories.OrganisationsRepository;
import me.urninax.flagdelivery.organisation.shared.InvitationDTO;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateInvitationRequest;
import me.urninax.flagdelivery.organisation.utils.projections.UserOrgProjection;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.repositories.UsersRepository;
import me.urninax.flagdelivery.user.utils.EntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class InvitationsService{
    private final InvitationsRepository invitationsRepository;
    private final EntityMapper entityMapper;
    private final UsersRepository usersRepository;
    private final OrganisationsRepository organisationsRepository;

    @Value("${invitation-token.expiration-time-seconds}")
    private long invitationTokenExpirationTime;

    @Autowired
    public InvitationsService(InvitationsRepository invitationsRepository, EntityMapper entityMapper, UsersRepository usersRepository, OrganisationsRepository organisationsRepository){
        this.invitationsRepository = invitationsRepository;
        this.entityMapper = entityMapper;
        this.usersRepository = usersRepository;
        this.organisationsRepository = organisationsRepository;
    }

    public Invitation createInvitation(CreateInvitationRequest request, UUID userId){
        UserOrgProjection userOrgProjection = usersRepository.findProjectedById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User was not found"));

        Organisation organisation = organisationsRepository.getReferenceById(userOrgProjection.getOrganisationId());
        UserEntity user = usersRepository.getReferenceById(userOrgProjection.getId());
        String token = generateToken();

        Invitation invitation = Invitation.builder()
                .organisation(organisation)
                .email(request.getEmail())
                .role(request.getRole())
                .tokenHash(hashToken(token))
                .rawToken(token)
                .status(InvitationStatus.PENDING)
                .invitedBy(user)
                .message(request.getMessage())
                .expiresAt(Instant.now().plusSeconds(invitationTokenExpirationTime))
                .build();

        return invitationsRepository.save(invitation);
    }

    @Transactional(readOnly = true)
    public InvitationDTO getInvitationDTO(String token){
        byte[] hashedToken = hashToken(token);
        Invitation invitation = invitationsRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));

        return entityMapper.toDTO(invitation);
    }

    private String generateToken(){
        byte[] bytes = new byte[32];
        try{
            SecureRandom sr = SecureRandom.getInstanceStrong();
            sr.nextBytes(bytes);
        }catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
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
