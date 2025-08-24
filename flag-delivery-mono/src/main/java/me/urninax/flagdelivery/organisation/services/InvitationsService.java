package me.urninax.flagdelivery.organisation.services;

import me.urninax.flagdelivery.organisation.models.Organisation;
import me.urninax.flagdelivery.organisation.models.invitation.Invitation;
import me.urninax.flagdelivery.organisation.models.invitation.InvitationStatus;
import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.organisation.repositories.InvitationsRepository;
import me.urninax.flagdelivery.organisation.repositories.MembershipsRepository;
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
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class InvitationsService{
    private final InvitationsRepository invitationsRepository;
    private final EntityMapper entityMapper;
    private final UsersRepository usersRepository;
    private final OrganisationsRepository organisationsRepository;
    private final MembershipsRepository membershipsRepository;
    private final MembershipsService membershipsService;

    @Value("${invitation-token.expiration-time-seconds}")
    private long invitationTokenExpirationTime;

    @Autowired
    public InvitationsService(InvitationsRepository invitationsRepository, EntityMapper entityMapper, UsersRepository usersRepository, OrganisationsRepository organisationsRepository, MembershipsRepository membershipsRepository, MembershipsService membershipsService){
        this.invitationsRepository = invitationsRepository;
        this.entityMapper = entityMapper;
        this.usersRepository = usersRepository;
        this.organisationsRepository = organisationsRepository;
        this.membershipsRepository = membershipsRepository;
        this.membershipsService = membershipsService;
    }

    public Invitation createInvitation(CreateInvitationRequest request, UUID userId){
        UserOrgProjection userOrgProjection = usersRepository.findProjectedById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User was not found"));

        //TODO: user cannot create invitation with role, higher that user's

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
    public InvitationDTO getInvitationDTO(UUID uuid, String token){
        Invitation invitation = invitationsRepository.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));

        byte[] hashedToken = hashToken(token);
        if(!Arrays.equals(invitation.getTokenHash(), hashedToken)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return entityMapper.toDTO(invitation);
    }

    @Transactional
    public Invitation acceptInvitation(UUID invitationId, String token, UUID userId, boolean isTransferAllowed){
        Invitation inv = invitationsRepository.findById(invitationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));

        byte[] hashedToken = hashToken(token);

        if(!Arrays.equals(inv.getTokenHash(), hashedToken)){ // different tokens hashes -> FORBIDDEN
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if(!inv.getStatus().isActive()){ // invitation status is not PENDING -> GONE
            throw new ResponseStatusException(HttpStatus.GONE, "Invitation is not active");
        }

        if(inv.getExpiresAt().isBefore(Instant.now())){ // invitation is expired -> GONE
            inv.setStatus(InvitationStatus.EXPIRED);
            invitationsRepository.save(inv);
            throw new ResponseStatusException(HttpStatus.GONE, "Invitation is expired");
        }

        UserEntity user = usersRepository.getReferenceById(userId);

        if(!emailsEquals(user.getEmail(), inv.getEmail())){ // different emails of authenticated user and in invitation -> FORBIDDEN
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Optional<Membership> membershipOptional = membershipsRepository.findById(userId);

        if(membershipOptional.isEmpty()){ // user has no organisation -> accept invitation
            membershipsService.addMembership(inv.getOrganisation().getId(), userId, inv.getRole(), false);
            finalizeInvitation(inv);
            return inv;
        }

        Membership membership = membershipOptional.get();
        if(!membership.getOrganisation().getId().equals(inv.getOrganisation().getId())){ // invitation for another organisation
            if(membership.isOwner()){
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User is owner of the current organisation. Transfer is impossible.");
            }
            if(!isTransferAllowed){
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User has an organisation. Transfer was not allowed.");
            }

            membership.setRole(inv.getRole());
            membership.setOrganisation(inv.getOrganisation());
            membership.setOwner(false);
            membershipsRepository.save(membership);

            finalizeInvitation(inv);
            return inv;
        }

        if(inv.getRole().higherThan(membership.getRole())){ // same organisation, but invitation role is higher than current
            membership.setRole(inv.getRole());
            membershipsRepository.save(membership);
        }

        finalizeInvitation(inv);
        return inv;
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

    private boolean emailsEquals(String email1, String email2){
        if(email1 == null || email2 == null) return false;
        return email1.equalsIgnoreCase(email2);
    }

    private void finalizeInvitation(Invitation inv){
        inv.setStatus(InvitationStatus.ACCEPTED);
        inv.setAcceptedAt(Instant.now());

        invitationsRepository.save(inv);
    }
}
