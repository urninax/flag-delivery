package me.urninax.flagdelivery.organisation.services;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.events.invitation.InvitationAcceptedEvent;
import me.urninax.flagdelivery.organisation.events.invitation.InvitationCreatedEvent;
import me.urninax.flagdelivery.organisation.models.Organisation;
import me.urninax.flagdelivery.organisation.models.invitation.Invitation;
import me.urninax.flagdelivery.organisation.models.invitation.InvitationStatus;
import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.repositories.InvitationsRepository;
import me.urninax.flagdelivery.organisation.repositories.MembershipsRepository;
import me.urninax.flagdelivery.organisation.shared.InvitationMailDTO;
import me.urninax.flagdelivery.organisation.shared.InvitationOrganisationDTO;
import me.urninax.flagdelivery.organisation.shared.InvitationPublicDTO;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateInvitationRequest;
import me.urninax.flagdelivery.organisation.ui.models.requests.InvitationFilter;
import me.urninax.flagdelivery.organisation.utils.InvitationSpecifications;
import me.urninax.flagdelivery.organisation.utils.InvitationTokenUtils;
import me.urninax.flagdelivery.shared.exceptions.ForbiddenException;
import me.urninax.flagdelivery.organisation.utils.exceptions.invitation.*;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import me.urninax.flagdelivery.user.models.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationsService{
    private final InvitationsRepository invitationsRepository;
    private final EntityMapper entityMapper;
    private final MembershipsRepository membershipsRepository;
    private final MembershipsService membershipsService;
    private final InvitationExpiryService invitationExpiryService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CurrentUser currentUser;
    private final EntityManager em;

    @Value("${invitation-token.expiration-time-seconds}")
    private long invitationTokenExpirationTime;

    @Transactional
    public void createInvitation(CreateInvitationRequest request){
        UUID userId = currentUser.getUserId();
        UUID orgId = currentUser.getOrganisationId();
        OrgRole userRole = currentUser.getOrgRole();

        invitationsRepository.findByEmailAndOrganisation_Id(request.getEmail(), orgId)
                .ifPresent(inv -> {
                    if(inv.getStatus().isActive()){
                        throw new InvitationAlreadyExistsException();
                    }
                });

        membershipsService.findByUserEmail(request.getEmail())
                .ifPresent(membership -> {
                    if(membership.getOrganisation().getId().equals(orgId)){
                        throw new UserAlreadyInSameOrganisationException();
                    }
                });

        UserEntity userRef = em.getReference(UserEntity.class, userId);
        Organisation orgRef = em.getReference(Organisation.class, orgId);

        String token = InvitationTokenUtils.generateToken();

        if(userRole.lowerOrEqual(request.getRole())){
            throw new ForbiddenException();
        }

        Invitation invitation = Invitation.builder()
                .organisation(orgRef)
                .email(request.getEmail())
                .role(request.getRole())
                .tokenHash(InvitationTokenUtils.hashToken(token))
                .rawToken(token)
                .status(InvitationStatus.PENDING)
                .invitedBy(userRef)
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
                .orElseThrow(InvitationNotFoundException::new);

        byte[] hashedToken = InvitationTokenUtils.hashToken(token);

        if(!MessageDigest.isEqual(invitation.getTokenHash(), hashedToken)){
            throw new ForbiddenException();
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
    public void acceptInvitation(UUID invitationId, String token, boolean isTransferAllowed){
        UUID userId = currentUser.getUserId();

        Invitation inv = invitationsRepository.findById(invitationId)
                .orElseThrow(InvitationNotFoundException::new);

        byte[] hashedToken = InvitationTokenUtils.hashToken(token);

        if(!MessageDigest.isEqual(inv.getTokenHash(), hashedToken)){ // different tokens hashes -> FORBIDDEN
            throw new ForbiddenException();
        }

        if(!inv.getStatus().isActive()){ // invitation status is not PENDING -> GONE
            throw new InvitationNotActiveException();
        }

        if(inv.getExpiresAt().isBefore(Instant.now())){ // invitation is expired -> GONE
            invitationExpiryService.markExpiredInNewTx(invitationId);
            throw new InvitationExpiredException();
        }

        UserEntity user = em.getReference(UserEntity.class, userId);

        if(emailsNotEquals(user.getEmail(), inv.getEmail())){ // different emails of authenticated user and in invitation -> FORBIDDEN
            throw new ForbiddenException();
        }

        Optional<Membership> membershipOptional = membershipsRepository.findById(userId);

        if(membershipOptional.isEmpty()){ // user has no organisation -> accept invitation
            membershipsService.addMembership(inv.getOrganisation().getId(), userId, inv.getRole());
            finalizeInvitation(inv);
            return;
        }

        Membership membership = membershipOptional.get();

        if(membership.getRole() == OrgRole.OWNER){
            throw new OwnerTransferNotAllowedException();
        }

        if(!isTransferAllowed){
            throw new OrganisationTransferNotAllowedException();
        }

        membership.setRole(inv.getRole());
        membership.setOrganisation(inv.getOrganisation());
        membershipsRepository.save(membership);

        finalizeInvitation(inv);
    }

    @Transactional
    public void declineInvitation(UUID invitationId, String token){
        UUID userId = currentUser.getUserId();

        Invitation inv = invitationsRepository.findById(invitationId)
                .orElseThrow(InvitationNotFoundException::new);

        byte[] hashedToken = InvitationTokenUtils.hashToken(token);

        if(!MessageDigest.isEqual(inv.getTokenHash(), hashedToken)){
            throw new ForbiddenException();
        }

        UserEntity user = em.getReference(UserEntity.class, userId);
        if(emailsNotEquals(inv.getEmail(), user.getEmail())){
            throw new ForbiddenException();
        }

        if(inv.getStatus() == InvitationStatus.DECLINED){
            return;
        }

        if(inv.getExpiresAt().isBefore(Instant.now())){
            invitationExpiryService.markExpiredInNewTx(invitationId);
            throw new InvitationExpiredException();
        }

        if(!inv.getStatus().isActive()){
            throw new InvitationNotActiveException();
        }

        inv.setDeclinedAt(Instant.now());
        inv.setStatus(InvitationStatus.DECLINED);
    }

    @Transactional
    public void revokeInvitation(UUID invitationId){
        UUID currentOrgId = currentUser.getOrganisationId();

        Invitation invitation = invitationsRepository.findByIdAndOrganisation_Id(invitationId, currentOrgId)
                .orElseThrow(InvitationNotFoundException::new);

        if(invitation.getExpiresAt().isBefore(Instant.now())){
            invitationExpiryService.markExpiredInNewTx(invitationId);
            throw new InvitationExpiredException();
        }

        if(!invitation.getStatus().isActive()){
            throw new InvitationNotActiveException();
        }

        invitation.setStatus(InvitationStatus.REVOKED);
        invitation.setRevokedAt(Instant.now());
    }


    private boolean emailsNotEquals(String email1, String email2){
        if(email1 == null || email2 == null) return true;
        return !email1.equalsIgnoreCase(email2);
    }

    private void finalizeInvitation(Invitation inv){
        inv.setStatus(InvitationStatus.ACCEPTED);
        inv.setAcceptedAt(Instant.now());

        Invitation invitation = invitationsRepository.save(inv);
        InvitationMailDTO invitationMailDTO = entityMapper.toMailDTO(invitation);

        applicationEventPublisher.publishEvent(new InvitationAcceptedEvent(invitationMailDTO));
    }
}
