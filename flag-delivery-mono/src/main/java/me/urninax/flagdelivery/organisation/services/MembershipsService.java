package me.urninax.flagdelivery.organisation.services;

import jakarta.transaction.Transactional;
import me.urninax.flagdelivery.organisation.models.Organisation;
import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.repositories.MembershipsRepository;
import me.urninax.flagdelivery.organisation.repositories.OrganisationsRepository;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MembershipsService{
    private final MembershipsRepository membershipsRepository;
    private final OrganisationsRepository organisationsRepository;
    private final UsersRepository usersRepository;

    @Autowired
    public MembershipsService(MembershipsRepository membershipsRepository, OrganisationsRepository organisationsRepository, UsersRepository usersRepository){
        this.membershipsRepository = membershipsRepository;
        this.organisationsRepository = organisationsRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional
    public void addMembership(UUID organisationId, UUID userId, OrgRole role, boolean isOwner){
        Organisation orgRef = organisationsRepository.getReferenceById(organisationId);
        UserEntity userRef = usersRepository.getReferenceById(userId);

        Membership membership = Membership.builder()
                .organisation(orgRef)
                .user(userRef)
                .owner(isOwner)
                .role(role)
                .build();

        membershipsRepository.save(membership);
    }
}
