package me.urninax.flagdelivery.flags.services;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.models.FeatureFlagTag;
import me.urninax.flagdelivery.flags.models.FlagKind;
import me.urninax.flagdelivery.flags.models.FlagVariation;
import me.urninax.flagdelivery.flags.repositories.FlagsRepository;
import me.urninax.flagdelivery.flags.shared.FeatureFlagDTO;
import me.urninax.flagdelivery.flags.ui.requests.CreateFeatureFlagRequest;
import me.urninax.flagdelivery.flags.ui.requests.VariationRequest;
import me.urninax.flagdelivery.organisation.repositories.MembershipsRepository;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import me.urninax.flagdelivery.projectsenvs.services.ProjectsService;
import me.urninax.flagdelivery.shared.exceptions.ConflictException;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import me.urninax.flagdelivery.shared.utils.PersistenceExceptionUtils;
import me.urninax.flagdelivery.user.models.UserEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlagsService{
    private final FlagsRepository flagsRepository;
    private final MembershipsRepository membershipsRepository;
    private final CurrentUser currentUser;
    private final ProjectsService projectsService;
    private final FlagVariationsService flagVariationsService;
    private final EntityManager em;
    private final EntityMapper entityMapper;

    @Transactional
    public FeatureFlagDTO createFlag(String projectKey, CreateFeatureFlagRequest request){
        UUID orgId = currentUser.getOrganisationId();
        UUID projectId = projectsService.findIdByKeyAndOrg(projectKey, orgId);

        List<VariationRequest> variationRequests = request.variations();

        // map request to FlagVariation objects or get default true/false variations of empty
        List<FlagVariation> variations = variationRequests != null && !variationRequests.isEmpty()
                ? flagVariationsService.transformVariations(variationRequests)
                : flagVariationsService.defaultVariations();

        // map default variations indexes or get default
        int onIdx  = Objects.requireNonNullElse(request.defaults().onVariation(), 0);
        int offIdx = Objects.requireNonNullElse(request.defaults().offVariation(), variations.size() - 1);

        if(onIdx >= variations.size() || offIdx >= variations.size()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Default variation index is out of bounds.");
        }

        // map each variation to FlagKind and collect to set to find out if all variations have the same kind
        Set<FlagKind> variationsKinds = variations.stream()
                .map(v -> detectType(v.getValue()))
                .collect(Collectors.toSet());

        if(variationsKinds.size() > 1){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Variations types are different.");
        }

        // compare set of variations with list. size should not change if unique
        if(new HashSet<>(variations).size() < variations.size()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Variations are not unique.");
        }

        // find out maintainer. maintainer from request if exists in the organisation, take requester id otherwise
        UUID candidateMaintainerId = request.maintainerId();
        UUID organisationId = currentUser.getOrganisationId();

        boolean isValidMaintainer = candidateMaintainerId != null
                && membershipsRepository.existsByUserIdAndOrganisation_Id(candidateMaintainerId, organisationId);

        UUID maintainerId = isValidMaintainer
                ? candidateMaintainerId
                : currentUser.getUserId();

        UserEntity maintainer = em.getReference(UserEntity.class, maintainerId);
        Project project = em.getReference(Project.class, projectId);

        // not null check for boolean primitive values
        boolean flagOn = request.isFlagOn() != null ? request.isFlagOn() : false;
        boolean temporary = request.temporary() != null ? request.temporary() : true;

        FeatureFlag flag = FeatureFlag.builder()
                .name(request.name().trim().replaceAll("\\s+", " "))
                .key(request.key())
                .description(request.description())
                .kind(variationsKinds.iterator().next())
                .variations(new HashSet<>(variations))
                .defaultOnVariation(variations.get(onIdx))
                .defaultOffVariation(variations.get(offIdx))
                .maintainer(maintainer)
                .flagOn(flagOn)
                .temporary(temporary)
                .project(project)
                .build();

        // map FeatureFlagTags if exist in request
        if(request.tags() != null && !request.tags().isEmpty()){
            Set<FeatureFlagTag> tags = request.tags().stream()
                    .map(tag -> FeatureFlagTag.of(flag, tag))
                    .collect(Collectors.toSet());

            flag.setTags(tags);
        }

        //todo: create flag configs for each environment is the project
        try{
            return entityMapper.toDTO(flagsRepository.save(flag));

        }catch(DataIntegrityViolationException exc){
            if(PersistenceExceptionUtils.isUniqueException(exc)){
                throw new ConflictException("Project key already in use");
            }
            throw exc;
        }
    }


    private FlagKind detectType(JsonNode value){
        if(value.isBoolean()) return FlagKind.BOOLEAN;
        if(value.isNumber()) return FlagKind.NUMBER;
        if(value.isTextual()) return FlagKind.STRING;
        if(value.isObject() || value.isArray()) return FlagKind.JSON;

        return FlagKind.STRING;
    }
}
