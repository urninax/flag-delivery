package me.urninax.flagdelivery.flags.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.models.EnvironmentFlagConfig;
import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.models.Prerequisite;
import me.urninax.flagdelivery.flags.models.rule.Rule;
import me.urninax.flagdelivery.flags.repositories.FeatureFlagPersistenceManager;
import me.urninax.flagdelivery.flags.repositories.FlagsRepository;
import me.urninax.flagdelivery.flags.services.patch.*;
import me.urninax.flagdelivery.flags.shared.FeatureFlagDTO;
import me.urninax.flagdelivery.flags.shared.ResolvedVariations;
import me.urninax.flagdelivery.flags.ui.requests.CreateFeatureFlagRequest;
import me.urninax.flagdelivery.flags.ui.requests.ListAllFlagsRequest;
import me.urninax.flagdelivery.flags.ui.requests.PatchFeatureFlag;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.*;
import me.urninax.flagdelivery.flags.utils.FlagConfigEnvironmentProjection;
import me.urninax.flagdelivery.flags.utils.exceptions.FlagNotFoundException;
import me.urninax.flagdelivery.flags.utils.exceptions.rule.RuleNotFoundException;
import me.urninax.flagdelivery.organisation.services.MembershipsService;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import me.urninax.flagdelivery.projectsenvs.repositories.environment.EnvironmentsRepository;
import me.urninax.flagdelivery.projectsenvs.services.ProjectsService;
import me.urninax.flagdelivery.shared.exceptions.BadRequestException;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import me.urninax.flagdelivery.user.models.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlagsService{
    private final FlagsRepository flagsRepository;
    private final EnvironmentsRepository environmentsRepository;
    private final CurrentUser currentUser;
    private final ProjectsService projectsService;
    private final FlagVariationsService flagVariationsService;
    private final PrerequisitesService prerequisitesService;
    private final FeatureFlagFactory featureFlagFactory;
    private final FeatureFlagPersistenceManager flagPersistenceManager;
    private final MembershipsService membershipsService;
    private final EntityManager em;
    private final EntityMapper entityMapper;
    private final FlagConfigService flagConfigService;
    private final RulesInstructionHandler rulesInstructionHandler;
    private final LifecycleInstructionHandler lifecycleInstructionHandler;
    private final TargetsInstructionHandler targetsInstructionHandler;
    private final SettingsInstructionHandler settingsInstructionHandler;
    private final VariationsInstructionHandler variationsInstructionHandler;
    private final ClausesInstructionHandler clausesInstructionHandler;
    private final PrerequisitesInstructionHandler prerequisitesInstructionHandler;

    @Transactional
    public FeatureFlagDTO createFlag(String projectKey, CreateFeatureFlagRequest request){
        UUID orgId = currentUser.getOrganisationId();
        UUID projectId = projectsService.findIdByKeyAndOrg(projectKey, orgId);
        Project project = em.getReference(Project.class, projectId);

        ResolvedVariations resolved = flagVariationsService.resolveAndValidateVariations(request);
        UserEntity maintainer = membershipsService.resolveMaintainer(request.maintainerId(), orgId);
        Set<Prerequisite> prerequisites = prerequisitesService.resolvePrerequisites(request.initialPrerequisites(),
                request.key(), projectId);

        FeatureFlag flag = featureFlagFactory.create(request, resolved, maintainer, project);

        FeatureFlag created = flagPersistenceManager.saveSafely(flag);

        initializeEnvironmentConfigs(created, projectId, prerequisites);

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

        Page<FeatureFlag> flagPage = flagsRepository.findPageWithFilter(projectId, request, pageable);

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

        UUID flagId = flagsRepository.findIdForUpdate(projectId, flagKey)
                .orElseThrow(FlagNotFoundException::new);

        FeatureFlag flag = flagsRepository.findDeepById(flagId)
                .orElseThrow(FlagNotFoundException::new);

        EnvironmentFlagConfig config = null;

        if(request.environmentKey() != null){
            config = flag.getFlagConfigs().stream()
                    .filter(c -> c.getEnvironment().getKey().equals(request.environmentKey()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Environment key not found"));
        }

        for(BaseInstruction instruction : request.instructions()){
            if(!em.contains(flag) || (config != null && !em.contains(config))){
                throw new BadRequestException("Instruction " + instruction.getClass().getSimpleName() +
                        " failed: The flag or environment was deleted by a previous instruction in the chain.");
            }

            switch(instruction){
                case ClauseInstruction c -> {
                    if(config == null) throw new BadRequestException("Environment config is missing");

                    Rule rule = config.getRules().stream()
                                    .filter(r -> r.getId().equals(c.getRuleId()))
                                    .findFirst()
                                    .orElseThrow(RuleNotFoundException::new);

                    clausesInstructionHandler.handle(rule, c);
                }
                case RuleInstruction r -> {
                    if(config == null) throw new BadRequestException("Environment config is missing");

                    rulesInstructionHandler.handle(config, flag, r);
                }
                case LifecycleInstruction l -> lifecycleInstructionHandler.handle(flag, l);
//                case TargetInstruction t -> targetsService.handle();
                case PrerequisiteInstruction p -> {
                    if(config == null) throw new BadRequestException("Environment config is missing");

                    prerequisitesInstructionHandler.handle(flag, config, p);
                }
                case SettingInstruction s -> settingsInstructionHandler.handle(flag, s);
                case VariationInstruction v -> variationsInstructionHandler.handle(flag, config, v);

                default -> throw new BadRequestException("Unsupported instruction type");
            }

            em.flush();
        }

    }

    public void deleteFlag(String projectKey, String flagKey){
        UUID orgId = currentUser.getOrganisationId();
        UUID projectId = projectsService.findIdByKeyAndOrg(projectKey, orgId);

        flagPersistenceManager.deleteSafely(projectId, flagKey);
    }

    // Helper Methods

    private void initializeEnvironmentConfigs(FeatureFlag flag, UUID projectId, Set<Prerequisite> prerequisites) {
        Set<FlagConfigEnvironmentProjection> envs = environmentsRepository.findAllByProject_Id(projectId);
        List<EnvironmentFlagConfig> configs = flagConfigService.createFlagConfigForEnvs(flag, envs, prerequisites);
        flag.setFlagConfigs(configs);
    }
}
