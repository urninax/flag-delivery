package me.urninax.flagdelivery.organisation.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.models.Organisation;
import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.repositories.MembershipsRepository;
import me.urninax.flagdelivery.organisation.repositories.OrganisationsRepository;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateOrganisationRequest;
import me.urninax.flagdelivery.organisation.utils.exceptions.OrganisationAlreadyExistsException;
import me.urninax.flagdelivery.organisation.utils.projections.UserOrgProjection;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.repositories.UsersRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganisationsService{
    private final OrganisationsRepository organisationsRepository;
    private final MembershipsService membershipsService;
    private final MembershipsRepository membershipsRepository;
    private final UsersRepository usersRepository;

    @Transactional
    public UUID createOrganisation(CreateOrganisationRequest request, UUID userId){
        UserOrgProjection userOrgProjection = usersRepository.findProjectedById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User was not found"));

        if(userOrgProjection.getOrganisationId() != null){
            throw new OrganisationAlreadyExistsException();
        }

        UserEntity userRef = usersRepository.getReferenceById(userOrgProjection.getId());

        Organisation organisation = Organisation.builder()
                .name(request.getName())
                .owner(userRef)
                .memberships(new ArrayList<>())
                .build();

        Organisation created = organisationsRepository.save(organisation);

        membershipsService.addMembership(
                created.getId(),
                userId,
                OrgRole.ADMIN,
                true
        );

        return created.getId();
    }

    @Transactional
    public void deleteOrganisation(UUID userId){
        Membership membership = membershipsRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("No role in any organisation"));

        if(!membership.isOwner()){
            throw new AccessDeniedException("User is not an owner of organisation");
        }

        organisationsRepository.deleteById(membership.getOrganisation().getId());
    }
}
