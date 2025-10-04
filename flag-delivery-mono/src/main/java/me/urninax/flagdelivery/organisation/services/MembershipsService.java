package me.urninax.flagdelivery.organisation.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.events.invitation.MemberRoleChangedEvent;
import me.urninax.flagdelivery.organisation.models.Organisation;
import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.repositories.MembershipsRepository;
import me.urninax.flagdelivery.organisation.shared.MemberWithActivityDTO;
import me.urninax.flagdelivery.organisation.ui.models.requests.ChangeMembersRoleRequest;
import me.urninax.flagdelivery.organisation.ui.models.requests.MembersFilter;
import me.urninax.flagdelivery.organisation.utils.exceptions.ForbiddenException;
import me.urninax.flagdelivery.organisation.utils.exceptions.membership.AdminRoleChangeForbiddenException;
import me.urninax.flagdelivery.organisation.utils.exceptions.membership.OwnerRoleModificationException;
import me.urninax.flagdelivery.organisation.utils.exceptions.membership.SelfRoleModificationException;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.user.models.UserEntity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipsService{
    private final MembershipsRepository membershipsRepository;
    private final CurrentUser currentUser;
    private final EntityManager em;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void addMembership(UUID organisationId, UUID userId, OrgRole role){
        UserEntity userRef = em.getReference(UserEntity.class, userId);
        Organisation orgRef = em.getReference(Organisation.class, organisationId);

        Membership membership = Membership.builder()
                .organisation(orgRef)
                .user(userRef)
                .role(role)
                .build();

        membershipsRepository.save(membership);
    }

    public Membership findByIdAndOrg(UUID userId, UUID orgId){
        return membershipsRepository.findByUserIdAndOrganisation_Id(userId, orgId)
                .orElseThrow(ForbiddenException::new);
    }

    @Transactional
    public void changeMembersRole(UUID memberId, ChangeMembersRoleRequest request){
        UUID requesterOrgId = currentUser.getOrganisationId();
        Membership targetMembership = findByIdAndOrg(memberId, requesterOrgId);

        if(currentUser.getUserId().equals(memberId)){
            throw new SelfRoleModificationException();
        }

        if(targetMembership.getRole() == request.role()){
            return;
        }

        if(targetMembership.getRole() == OrgRole.ADMIN && currentUser.getOrgRole() != OrgRole.OWNER){
            throw new AdminRoleChangeForbiddenException();
        }

        if(targetMembership.getRole() == OrgRole.OWNER){
            throw new OwnerRoleModificationException();
        }

        targetMembership.setRole(request.role());
        membershipsRepository.save(targetMembership);

        applicationEventPublisher.publishEvent(new MemberRoleChangedEvent(memberId, request.role()));
    }

    public Page<MemberWithActivityDTO> getMembers(MembersFilter filter, Pageable pageable){
        LocalDate lastSeenAfter = filter.getLastSeenAfter();
        Instant threshold = lastSeenAfter != null
                ? lastSeenAfter.atStartOfDay(ZoneId.of("UTC")).toInstant()
                : null;

        return membershipsRepository.findMembers(
                currentUser.getOrganisationId(),
                threshold,
                filter.getRoles(),
                pageable
        );
    }
}
