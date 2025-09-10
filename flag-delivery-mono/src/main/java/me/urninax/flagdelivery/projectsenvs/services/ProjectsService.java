package me.urninax.flagdelivery.projectsenvs.services;

import jakarta.transaction.Transactional;
import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.organisation.repositories.MembershipsRepository;
import me.urninax.flagdelivery.projectsenvs.models.project.CasingConvention;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import me.urninax.flagdelivery.projectsenvs.repositories.ProjectsRepository;
import me.urninax.flagdelivery.projectsenvs.shared.project.ProjectDTO;
import me.urninax.flagdelivery.projectsenvs.ui.models.requests.CreateProjectRequest;
import me.urninax.flagdelivery.shared.exceptions.ConflictException;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import me.urninax.flagdelivery.shared.utils.PersistenceExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class ProjectsService{
    private final ProjectsRepository projectsRepository;
    private final EntityMapper entityMapper;
    private final CurrentUser currentUser;
    private final MembershipsRepository membershipsRepository;
    private final Clock clock;

    @Autowired
    public ProjectsService(ProjectsRepository projectsRepository, EntityMapper entityMapper, CurrentUser currentUser, MembershipsRepository membershipsRepository, Clock clock){
        this.projectsRepository = projectsRepository;
        this.entityMapper = entityMapper;
        this.currentUser = currentUser;
        this.membershipsRepository = membershipsRepository;
        this.clock = clock;
    }

    @Transactional
    public ProjectDTO createProject(CreateProjectRequest request){
        UUID userId = currentUser.getUserId();
        Membership membership = membershipsRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User has no organisation"));

        Project project = Project.builder()
                .name(request.name().trim().replaceAll("\\s+", " "))
                .key(request.key())
                .tags(request.tags())
                .organisationId(membership.getOrganisation().getId())
                .createdBy(userId)
                .createdAt(Instant.now(clock))
                .updatedAt(Instant.now(clock))
                .build();

        if(request.namingConvention() != null){
            CasingConvention convention = request.namingConvention().casingConvention();
            String prefix = request.namingConvention().prefix();

            if(convention != null){
                if(prefix != null && !convention.matches(prefix)){
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            String.format("Use %s: %s", convention, convention.description())
                    );
                }
            }

            project.setCasingConvention(request.namingConvention().casingConvention());
            project.setPrefix(request.namingConvention().prefix());
        }

        //todo: create default environments if not specified (publish event maybe)
        try{
            Project createdProject = projectsRepository.saveAndFlush(project);
            return entityMapper.toDTO(createdProject);
        }catch(DataIntegrityViolationException exc){
            if(PersistenceExceptionUtils.isUniqueException(exc)){
                throw new ConflictException("Project key already in use");
            }
            throw exc;
        }
    }
}
