package me.urninax.flagdelivery.shared.utils;

import me.urninax.flagdelivery.organisation.models.AccessToken;
import me.urninax.flagdelivery.organisation.models.invitation.Invitation;
import me.urninax.flagdelivery.organisation.shared.AccessTokenDTO;
import me.urninax.flagdelivery.organisation.shared.InvitationMailDTO;
import me.urninax.flagdelivery.organisation.shared.InvitationOrganisationDTO;
import me.urninax.flagdelivery.organisation.shared.InvitationPublicDTO;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import me.urninax.flagdelivery.projectsenvs.models.environment.EnvironmentTag;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import me.urninax.flagdelivery.projectsenvs.models.project.ProjectTag;
import me.urninax.flagdelivery.projectsenvs.shared.environment.EnvironmentDTO;
import me.urninax.flagdelivery.projectsenvs.shared.project.ProjectDTO;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EntityMapper{

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    UserEntity toEntity(SignupRequest signupRequest);

    @Mapping(source = "owner.id", target = "memberId")
    @Mapping(source = "id", target = "id")
    AccessTokenDTO toDTO(AccessToken accessToken);

    @Mapping(target = "invitedBy", expression = "java(invitation.getInvitedBy().getFirstName() + \" \" + invitation.getInvitedBy().getLastName())")
    @Mapping(source = "organisation.name", target = "organisationName")
    InvitationPublicDTO toPublicDTO(Invitation invitation);

    @Mapping(target = "invitedBy", expression = "java(invitation.getInvitedBy().getFirstName() + \" \" + invitation.getInvitedBy().getLastName())")
    InvitationOrganisationDTO toOrganisationDTO(Invitation invitation);

    @Mapping(target = "organisationName", source = "invitation.organisation.name")
    @Mapping(source = "invitation.id", target = "invitationId")
    @Mapping(target = "token", ignore = true)
    InvitationMailDTO toMailDTO(Invitation invitation);

    @Mapping(source = "tags", target = "tags")
    ProjectDTO toDTO(Project project);

    @Mapping(source = "tags", target = "tags")
    EnvironmentDTO toDTO(Environment environment);

    default Set<String> mapProjectTags(Set<ProjectTag> tags) {
        if (tags == null) {
            return Collections.emptySet();
        }
        return tags.stream()
                .map(pt -> pt.getId().getTag())
                .collect(Collectors.toSet());
    }

    default Set<String> mapEnvironmentTags(Set<EnvironmentTag> tags){
        if (tags == null) {
            return Collections.emptySet();
        }
        return tags.stream()
                .map(et -> et.getId().getTag())
                .collect(Collectors.toSet());
    }
}
