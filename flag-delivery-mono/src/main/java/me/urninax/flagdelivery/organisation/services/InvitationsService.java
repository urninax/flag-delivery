package me.urninax.flagdelivery.organisation.services;

import me.urninax.flagdelivery.organisation.events.invitation.InvitationCreatedEvent;
import me.urninax.flagdelivery.organisation.models.invitation.Invitation;
import me.urninax.flagdelivery.organisation.models.invitation.InvitationStatus;
import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.organisation.repositories.InvitationsRepository;
import me.urninax.flagdelivery.organisation.repositories.MembershipsRepository;
import me.urninax.flagdelivery.organisation.shared.InvitationMailDTO;
import me.urninax.flagdelivery.organisation.shared.InvitationOrganisationDTO;
import me.urninax.flagdelivery.organisation.shared.InvitationPublicDTO;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateInvitationRequest;
import me.urninax.flagdelivery.organisation.ui.models.requests.InvitationFilter;
import me.urninax.flagdelivery.organisation.utils.InvitationSpecifications;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.repositories.UsersRepository;
import me.urninax.flagdelivery.user.utils.EntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class InvitationsService{
    private final InvitationsRepository invitationsRepository;
    private final EntityMapper entityMapper;
    private final UsersRepository usersRepository;
    private final MembershipsRepository membershipsRepository;
    private final MembershipsService membershipsService;
    private final InvitationExpiryService invitationExpiryService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${invitation-token.expiration-time-seconds}")
    private long invitationTokenExpirationTime;

    @Autowired
    public InvitationsService(InvitationsRepository invitationsRepository, EntityMapper entityMapper, UsersRepository usersRepository, MembershipsRepository membershipsRepository, MembershipsService membershipsService, InvitationExpiryService invitationExpiryService, ApplicationEventPublisher applicationEventPublisher){
        this.invitationsRepository = invitationsRepository;
        this.entityMapper = entityMapper;
        this.usersRepository = usersRepository;
        this.membershipsRepository = membershipsRepository;
        this.membershipsService = membershipsService;
        this.invitationExpiryService = invitationExpiryService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public void createInvitation(CreateInvitationRequest request, UUID userId){
        Membership membership = membershipsRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no organisation"));

        if(request.getRole().higherThan(membership.getRole())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to grant role higher than user's");
        }

        String token = generateToken();

        Invitation invitation = Invitation.builder()
                .organisation(membership.getOrganisation())
                .email(request.getEmail())
                .role(request.getRole())
                .tokenHash(hashToken(token))
                .rawToken(token)
                .status(InvitationStatus.PENDING)
                .invitedBy(membership.getUser())
                .message(request.getMessage())
                .expiresAt(Instant.now().plusSeconds(invitationTokenExpirationTime))
                .build();

        Invitation invitationEntity = invitationsRepository.save(invitation);

        InvitationMailDTO invitationMailDTO = entityMapper.toMailDTO(invitationEntity);
        invitationMailDTO.setToken(token);

        InvitationCreatedEvent event = new InvitationCreatedEvent(invitationMailDTO);

        applicationEventPublisher.publishEvent(event);
    }

    @Transactional(readOnly = true)
    public InvitationPublicDTO getInvitationDTO(UUID uuid, String token){
        Invitation invitation = invitationsRepository.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));

        byte[] hashedToken = hashToken(token);
        if(!Arrays.equals(invitation.getTokenHash(), hashedToken)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return entityMapper.toPublicDTO(invitation);
    }

    @Transactional(readOnly = true)
    public Page<InvitationOrganisationDTO> listOrganisationInvitations(InvitationFilter filter, Pageable pageable){
        ZoneId zone = ZoneOffset.UTC;

        Specification<Invitation> spec = Specification.allOf(
                InvitationSpecifications.byStatuses(filter.getStatus()),
                InvitationSpecifications.byCreator(filter.getInvitedBy()),
                InvitationSpecifications.byEmail(filter.getEmail()),
                InvitationSpecifications.createdBetween(filter.getCreatedFrom(), filter.getCreatedTo(), zone),
                InvitationSpecifications.expiresBetween(filter.getExpiresFrom(), filter.getExpiresTo(), zone)
        );

        return invitationsRepository.findAll(spec, pageable)
                .map(entityMapper::toOrganisationDTO);
    }

    @Transactional
    public Invitation acceptInvitation(UUID invitationId, String token, UUID userId, boolean isTransferAllowed){
        Invitation inv = invitationsRepository.findById(invitationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));

        byte[] hashedToken = hashToken(token);

        if(!MessageDigest.isEqual(inv.getTokenHash(), hashedToken)){ // different tokens hashes -> FORBIDDEN
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if(!inv.getStatus().isActive()){ // invitation status is not PENDING -> GONE
            throw new ResponseStatusException(HttpStatus.GONE, "Invitation is not active");
        }

        if(inv.getExpiresAt().isBefore(Instant.now())){ // invitation is expired -> GONE
            invitationExpiryService.markExpiredInNewTx(invitationId);
            throw new ResponseStatusException(HttpStatus.GONE, "Invitation is expired");
        }

        UserEntity user = usersRepository.getReferenceById(userId);

        if(emailsNotEquals(user.getEmail(), inv.getEmail())){ // different emails of authenticated user and in invitation -> FORBIDDEN
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

    @Transactional
    public void declineInvitation(UUID invitationId, String token, UUID userId){
        Invitation inv = invitationsRepository.findById(invitationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found."));

        byte[] hashedToken = hashToken(token);

        if(!MessageDigest.isEqual(inv.getTokenHash(), hashedToken)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tokens are not the same");
        }

        UserEntity user = usersRepository.getReferenceById(userId);
        if(emailsNotEquals(inv.getEmail(), user.getEmail())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User's email differs from the one in the invitation");
        }

        if(inv.getStatus() == InvitationStatus.DECLINED){
            return;
        }

        if(inv.getExpiresAt().isBefore(Instant.now())){
            invitationExpiryService.markExpiredInNewTx(invitationId);
            throw new ResponseStatusException(HttpStatus.GONE, "Invitation is expired");
        }

        if(!inv.getStatus().isActive()){
            throw new ResponseStatusException(HttpStatus.GONE, "Invitation is not active");
        }

        inv.setDeclinedAt(Instant.now());
        inv.setStatus(InvitationStatus.DECLINED);
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

    private boolean emailsNotEquals(String email1, String email2){
        if(email1 == null || email2 == null) return true;
        return !email1.equalsIgnoreCase(email2);
    }

    private void finalizeInvitation(Invitation inv){
        inv.setStatus(InvitationStatus.ACCEPTED);
        inv.setAcceptedAt(Instant.now());

        invitationsRepository.save(inv);
    }
}
