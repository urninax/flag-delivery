package me.urninax.flagdelivery.organisation.services;

import jakarta.transaction.Transactional;
import me.urninax.flagdelivery.organisation.models.Organisation;
import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.repositories.AccessTokenRepository;
import me.urninax.flagdelivery.organisation.repositories.MembershipsRepository;
import me.urninax.flagdelivery.organisation.repositories.OrganisationsRepository;
import me.urninax.flagdelivery.organisation.ui.models.requests.ChangeMembersRoleRequest;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class MembershipsService{
    private final MembershipsRepository membershipsRepository;
    private final OrganisationsRepository organisationsRepository;
    private final UsersRepository usersRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final CurrentUser currentUser;

    @Autowired
    public MembershipsService(MembershipsRepository membershipsRepository, OrganisationsRepository organisationsRepository, UsersRepository usersRepository, AccessTokenRepository accessTokenRepository, CurrentUser currentUser){
        this.membershipsRepository = membershipsRepository;
        this.organisationsRepository = organisationsRepository;
        this.usersRepository = usersRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.currentUser = currentUser;
    }

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

        accessTokenRepository.downgradeUserTokens(memberId, request.role());
    }
}
