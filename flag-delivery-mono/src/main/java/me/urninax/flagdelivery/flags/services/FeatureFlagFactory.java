package me.urninax.flagdelivery.flags.services;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.models.FlagKind;
import me.urninax.flagdelivery.flags.shared.ResolvedVariations;
import me.urninax.flagdelivery.flags.ui.requests.CreateFeatureFlagRequest;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import me.urninax.flagdelivery.user.models.UserEntity;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FeatureFlagFactory{
    public FeatureFlag create(CreateFeatureFlagRequest request,
                              ResolvedVariations resolved,
                              UserEntity maintainer,
                              Project project) {

        FlagKind kind = FlagKind.from(resolved.variations().getFirst().getValue());

        FeatureFlag flag = FeatureFlag.builder()
                .name(cleanName(request.name()))
                .key(request.key())
                .description(request.description())
                .kind(kind)
                .maintainer(maintainer)
                .flagOn(Objects.requireNonNullElse(request.isFlagOn(), false))
                .temporary(Objects.requireNonNullElse(request.temporary(), true))
                .project(project)
                .build();

        flag.setVariations(new LinkedList<>());
        resolved.variations().forEach(flag::addVariation);

        if (request.tags() != null) {
            flag.setTags(new HashSet<>(request.tags()));
        }

        flag.setDefaultOnVariation(resolved.onVariation());
        flag.setDefaultOffVariation(resolved.offVariation());

        return flag;
    }

    private String cleanName(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ");
    }
}
