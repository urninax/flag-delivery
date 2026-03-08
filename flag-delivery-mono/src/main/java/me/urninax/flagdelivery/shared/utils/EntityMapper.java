package me.urninax.flagdelivery.shared.utils;

import me.urninax.flagdelivery.contexts.models.ContextKind;
import me.urninax.flagdelivery.contexts.shared.ContextKindDTO;
import me.urninax.flagdelivery.flags.models.EnvironmentFlagConfig;
import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.models.rule.RuleClause;
import me.urninax.flagdelivery.flags.shared.DefaultsDTO;
import me.urninax.flagdelivery.flags.shared.EnvironmentFlagConfigDTO;
import me.urninax.flagdelivery.flags.shared.FeatureFlagDTO;
import me.urninax.flagdelivery.flags.ui.requests.rule.ClauseRequest;
import me.urninax.flagdelivery.organisation.models.AccessToken;
import me.urninax.flagdelivery.organisation.models.invitation.Invitation;
import me.urninax.flagdelivery.organisation.shared.AccessTokenDTO;
import me.urninax.flagdelivery.organisation.shared.InvitationMailDTO;
import me.urninax.flagdelivery.organisation.shared.InvitationOrganisationDTO;
import me.urninax.flagdelivery.organisation.shared.InvitationPublicDTO;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import me.urninax.flagdelivery.projectsenvs.shared.environment.EnvironmentDTO;
import me.urninax.flagdelivery.projectsenvs.shared.project.ProjectDTO;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Map;
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
    @Mapping(target = "environments", ignore = true)
    @Mapping(source = "casingConvention", target = "namingConvention.casing")
    @Mapping(source = "prefix", target = "namingConvention.prefix")
    ProjectDTO toDTO(Project project);

    @Mapping(source = "tags", target = "tags")
    ProjectDTO toExpandedDTO(Project project);

    @Mapping(source = "tags", target = "tags")
    EnvironmentDTO toDTO(Environment environment);

    @Mapping(source = "tags", target = "tags")
    @Mapping(target = "defaults", expression = "java(mapDefaults(featureFlag))")
    @Mapping(target = "maintainerId", source = "maintainer.id")
    @Mapping(source = "flagConfigs", target = "environments")
    FeatureFlagDTO toDTO(FeatureFlag featureFlag);

    default DefaultsDTO mapDefaults(FeatureFlag flag) {
        if (flag == null) return null;
        return new DefaultsDTO(
                flag.getDefaultOnVariation() != null ? flag.getDefaultOnVariation().getId() : null,
                flag.getDefaultOffVariation() != null ? flag.getDefaultOffVariation().getId() : null
        );
    }

    @Mapping(source = "offVariation.id", target = "offVariationId")
    @Mapping(source = "fallthroughVariation.id", target = "fallthroughVariationId")
    EnvironmentFlagConfigDTO toDTO(EnvironmentFlagConfig flagConfig);

    ContextKindDTO toDTO(ContextKind kind);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rule", ignore = true)
    @Mapping(target = "contextKindKey", source = "contextKind")
    RuleClause toEntity(ClauseRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rule", ignore = true)
    @Mapping(target = "contextKindKey", source = "contextKind")
    void updateEntityFromRequest(ClauseRequest request, @MappingTarget RuleClause entity);

    default Map<String, EnvironmentFlagConfigDTO> mapFlagConfigs(List<EnvironmentFlagConfig> configs){
        return configs.stream().collect(Collectors.toMap(
                key -> key.getEnvironment().getKey(),
                this::toDTO
        ));
    }
}
