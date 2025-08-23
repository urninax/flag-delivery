package me.urninax.flagdelivery.user.utils;

import me.urninax.flagdelivery.organisation.models.AccessToken;
import me.urninax.flagdelivery.organisation.models.invitation.Invitation;
import me.urninax.flagdelivery.organisation.shared.AccessTokenDTO;
import me.urninax.flagdelivery.organisation.shared.InvitationDTO;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntityMapper{

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    UserEntity toEntity(SignupRequest signupRequest);

    @Mapping(source = "owner.id", target = "memberId")
    AccessTokenDTO toDTO(AccessToken accessToken);

    @Mapping(target = "invitedBy", expression = "java(invitedBy.getFirstName() + \" \" + invitedBy.getLastName())")
    @Mapping(source = "organisation.name", target = "organisationName")

    InvitationDTO toDTO(Invitation invitation);
}
