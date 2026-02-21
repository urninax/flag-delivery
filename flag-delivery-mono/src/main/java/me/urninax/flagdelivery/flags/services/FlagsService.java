package me.urninax.flagdelivery.flags.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.models.*;
import me.urninax.flagdelivery.flags.models.rule.Rule;
import me.urninax.flagdelivery.flags.repositories.FlagsRepository;
import me.urninax.flagdelivery.flags.repositories.RulesRepository;
import me.urninax.flagdelivery.flags.services.patch.*;
import me.urninax.flagdelivery.flags.shared.FeatureFlagDTO;
import me.urninax.flagdelivery.flags.shared.ResolvedVariations;
import me.urninax.flagdelivery.flags.ui.requests.CreateFeatureFlagRequest;
import me.urninax.flagdelivery.flags.ui.requests.ListAllFlagsRequest;
import me.urninax.flagdelivery.flags.ui.requests.PatchFeatureFlag;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.BaseInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.ClauseInstruction;
import me.urninax.flagdelivery.flags.utils.FlagConfigEnvironmentProjection;
import me.urninax.flagdelivery.flags.utils.exceptions.FlagAlreadyExistsException;
import me.urninax.flagdelivery.flags.utils.exceptions.FlagNotFoundException;
import me.urninax.flagdelivery.flags.utils.exceptions.rule.RuleNotFoundException;
import me.urninax.flagdelivery.organisation.repositories.MembershipsRepository;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import me.urninax.flagdelivery.projectsenvs.repositories.EnvironmentsRepository;
import me.urninax.flagdelivery.projectsenvs.services.ProjectsService;
import me.urninax.flagdelivery.shared.exceptions.BadRequestException;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import me.urninax.flagdelivery.shared.utils.PersistenceExceptionUtils;
import me.urninax.flagdelivery.user.models.UserEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlagsService{
    private final FlagsRepository flagsRepository;
    private final MembershipsRepository membershipsRepository;
    private final EnvironmentsRepository environmentsRepository;
    private final CurrentUser currentUser;
    private final ProjectsService projectsService;
    private final FlagVariationsService flagVariationsService;
    private final EntityManager em;
    private final EntityMapper entityMapper;
    private final FlagConfigService flagConfigService;
    private final RulesService rulesService;
    private final LifecycleService lifecycleService;
    private final TargetsService targetsService;
    private final PrerequisitesService prerequisitesService;
    private final SettingsService settingsService;
    private final VariationsService variationsService;
    private final ClausesService clausesService;
    private final RulesRepository rulesRepository;

    @Transactional
    public FeatureFlagDTO createFlag(String projectKey, CreateFeatureFlagRequest request){
        UUID orgId = currentUser.getOrganisationId();
        UUID projectId = projectsService.findIdByKeyAndOrg(projectKey, orgId);


        ResolvedVariations resolvedVariations = flagVariationsService.resolveAndValidateVariations(request);
        UserEntity maintainer = resolveMaintainer(request.maintainerId(), orgId);
        Project project = em.getReference(Project.class, projectId);

        FeatureFlag flag = buildFeatureFlag(request, resolvedVariations, maintainer, project);
        attachTags(flag, request.tags());

        FeatureFlag created = safelySaveFlag(flag);
        initializeEnvironmentConfigs(created, projectId);

        return entityMapper.toDTO(created);
    }

    public FeatureFlagDTO getFlag(String projectKey, String flagKey){
        UUID orgId = currentUser.getOrganisationId();
        UUID projectId = projectsService.findIdByKeyAndOrg(projectKey, orgId);

        FeatureFlag flag = flagsRepository.findByProjectIdAndKey(projectId, flagKey)
                .orElseThrow(FlagNotFoundException::new);

        return entityMapper.toDTO(flag);
    }

    public Page<FeatureFlagDTO> getPaginatedFlags(String projectKey, Pageable pageable, ListAllFlagsRequest request){
        UUID orgId = currentUser.getOrganisationId();
        UUID projectId = projectsService.findIdByKeyAndOrg(projectKey, orgId);

        Page<FeatureFlag> flagPage = flagsRepository.findAllWithFilter(projectId, request, pageable);

        return flagPage.map(entityMapper::toDTO);
    }

    @Transactional
    public void patchFlag(PatchFeatureFlag request, String projectKey, String flagKey){
        UUID orgId = currentUser.getOrganisationId();
        UUID projectId = projectsService.findIdByKeyAndOrg(projectKey, orgId);

        if(request.instructions().stream().anyMatch(BaseInstruction::requiresEnvironmentKey)){
            if (request.environmentKey() == null) {
                throw new BadRequestException("Environment key is required for some instructions");
            }
        }

        for(BaseInstruction instruction : request.instructions()){
            switch(instruction){
                case ClauseInstruction c -> {
                    Rule rule = rulesRepository.findRuleWithSecurityCheck(c.getRuleId(), request.environmentKey(), projectId)
                                    .orElseThrow(RuleNotFoundException::new);
                    clausesService.handle(rule, c);
                }
//                case RuleInstruction r -> rulesService.handle();
//                case LifecycleInstruction l -> lifecycleService.handle();
//                case TargetInstruction t -> targetsService.handle();
//                case PrerequisiteInstruction p -> prerequisitesService.handle();
//                case SettingInstruction s -> settingsService.handle();
//                case VariationInstruction v -> variationsService.handle();
                default -> throw new IllegalStateException("Unexpected value: " + instruction); // todo: change
            }
        }

    }

    public void deleteFlag(String projectKey, String flagKey){
        UUID orgId = currentUser.getOrganisationId();
        UUID projectId = projectsService.findIdByKeyAndOrg(projectKey, orgId);

        if(!flagsRepository.deleteByProjectIdAndKey(projectId, flagKey)){
            throw new FlagNotFoundException();
        }
    }

    // Helper Methods

    private UserEntity resolveMaintainer(UUID candidateId, UUID orgId){
        // find out maintainer. maintainer from request if exists in the organisation, take requester id otherwise
        boolean isValidMaintainer = candidateId != null
                && membershipsRepository.existsByUserIdAndOrganisation_Id(candidateId, orgId);

        UUID maintainerId = isValidMaintainer
                ? candidateId
                : currentUser.getUserId();

        return em.getReference(UserEntity.class, maintainerId);
    }

    private FeatureFlag buildFeatureFlag(CreateFeatureFlagRequest request,
                                         ResolvedVariations resolvedVariations,
                                         UserEntity maintainer,
                                         Project project){
        List<FlagVariation> variations = resolvedVariations.variations();

        FlagKind kind = flagVariationsService.detectType(variations.getFirst().getValue());

        FeatureFlag flag = FeatureFlag.builder()
                .name(request.name().trim().replaceAll("\\s+", " "))
                .key(request.key())
                .description(request.description())
                .kind(kind)
                .defaultOnVariationIdx(resolvedVariations.onIdx())
                .defaultOffVariationIdx(resolvedVariations.offIdx())
                .maintainer(maintainer)
                .flagOn(Objects.requireNonNullElse(request.isFlagOn(), false))
                .temporary(Objects.requireNonNullElse(request.temporary(), true))
                .project(project)
                .build();

        flag.setVariations(new LinkedList<>());
        variations.forEach(flag::addVariation);

        return flag;
    }

    private void attachTags(FeatureFlag flag, Set<String> tagNames) {
        if (tagNames != null && !tagNames.isEmpty()) {
            Set<FeatureFlagTag> tags = tagNames.stream()
                    .map(tag -> FeatureFlagTag.of(flag, tag))
                    .collect(Collectors.toSet());
            flag.setTags(tags);
        }
    }

    private FeatureFlag safelySaveFlag(FeatureFlag flag) {
        try {
            return flagsRepository.saveAndFlush(flag);
        } catch (DataIntegrityViolationException exc) {
            if (PersistenceExceptionUtils.isUniqueException(exc)) {
                throw new FlagAlreadyExistsException();
            }
            throw exc;
        }
    }

    private void initializeEnvironmentConfigs(FeatureFlag flag, UUID projectId) {
        Set<FlagConfigEnvironmentProjection> envs = environmentsRepository.findAllByProject_Id(projectId);
        List<EnvironmentFlagConfig> configs = flagConfigService.createFlagConfigForEnvs(flag, envs);
        flag.setFlagConfigs(configs);
    }
}
