package me.urninax.flagdelivery.organisation.services;

import jakarta.transaction.Transactional;
import me.urninax.flagdelivery.organisation.models.Organisation;
import me.urninax.flagdelivery.organisation.repositories.OrganisationsRepository;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateOrganisationRequest;
import me.urninax.flagdelivery.organisation.utils.exceptions.OrganisationAlreadyExistsException;
import me.urninax.flagdelivery.organisation.utils.projections.UserOrgProjection;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class OrganisationsService{
    private final OrganisationsRepository organisationsRepository;
    private final UsersRepository usersRepository;

    @Autowired
    public OrganisationsService(OrganisationsRepository organisationsRepository, UsersRepository usersRepository){
        this.organisationsRepository = organisationsRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional
    public UUID createOrganisation(CreateOrganisationRequest request, UUID userId){
        UserOrgProjection userOrgProjection = usersRepository.findProjectedById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User was not found"));

        if(userOrgProjection.getOrganisationId() != null){
            throw new OrganisationAlreadyExistsException("User is already in organisation");
        }

        UserEntity userRef = usersRepository.getReferenceById(userOrgProjection.getId());

        Organisation organisation = Organisation.builder()
                .name(request.getName())
                .owner(userRef)
                .members(new ArrayList<>())
                .build();

        organisation.addMember(userRef);

        Organisation created = organisationsRepository.save(organisation);

        return created.getId();
    }
}
