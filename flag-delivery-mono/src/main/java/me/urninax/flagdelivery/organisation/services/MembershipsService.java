package me.urninax.flagdelivery.organisation.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.models.Organisation;
import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.repositories.MembershipsRepository;
import me.urninax.flagdelivery.organisation.repositories.OrganisationsRepository;
import me.urninax.flagdelivery.organisation.shared.MemberWithActivityDTO;
import me.urninax.flagdelivery.organisation.ui.models.requests.ChangeMembersRoleRequest;
import me.urninax.flagdelivery.organisation.ui.models.requests.MembersFilter;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.repositories.UsersRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipsService{
    private final MembershipsRepository membershipsRepository;
    private final OrganisationsRepository organisationsRepository;
    private final UsersRepository usersRepository;
    private final AccessTokenService accessTokenService;
    private final CurrentUser currentUser;

    @Transactional
    public void addMembership(UUID organisationId, UUID userId, OrgRole role){
        Organisation orgRef = organisationsRepository.getReferenceById(organisationId);
        UserEntity userRef = usersRepository.getReferenceById(userId);

        Membership membership = Membership.builder()
                .organisation(orgRef)
                .user(userRef)
                .role(role)
                .build();

        membershipsRepository.save(membership);
    }

    @Cacheable(value = "memberships", key = "#userId")
    public Membership findMembershipById(UUID userId){
        return membershipsRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("User has no organisation"));
    }

    @Transactional
    public void changeMembersRole(UUID memberId, ChangeMembersRoleRequest request){
        Membership targetMembership = membershipsRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member was not found"));

        if(currentUser.getUserId().equals(memberId)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot modify your own role");
        }

        if(targetMembership.getRole() == request.role()){
            return;
        }

        if(targetMembership.getRole() == OrgRole.ADMIN && currentUser.getOrgRole() != OrgRole.OWNER){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only organisation owner can change ADMIN's role");
        }

        if(targetMembership.getRole() == OrgRole.OWNER){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Owner role cannot be modified");
        }

        targetMembership.setRole(request.role());
        membershipsRepository.save(targetMembership);

        accessTokenService.downgradeMemberTokens(memberId, request.role());
    }

    public Page<MemberWithActivityDTO> getMembers(MembersFilter filter, Pageable pageable){
        Membership membership = membershipsRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User has no organisation"));

        LocalDate lastSeenAfter = filter.getLastSeenAfter();
        Instant threshold = lastSeenAfter != null
                ? lastSeenAfter.atStartOfDay(ZoneId.of("UTC")).toInstant()
                : null;

        return membershipsRepository.findMembers(
                membership.getOrganisation().getId(),
                threshold,
                filter.getRoles(),
                pageable
        );
    }
}
