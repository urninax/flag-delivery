package me.urninax.flagdelivery.user.utils;

import me.urninax.flagdelivery.organisation.models.Organisation;
import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.shared.MembershipDTO;
import me.urninax.flagdelivery.user.shared.OrganisationDTO;
import me.urninax.flagdelivery.user.shared.UserDTO;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper{

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    UserEntity toEntity(SignupRequest signupRequest);

    UserDTO toDTO(UserEntity userEntity);
    MembershipDTO toDTO(Membership membership);
    OrganisationDTO toDTO(Organisation organisation);
}
