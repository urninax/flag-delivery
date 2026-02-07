package me.urninax.flagdelivery.flags.repositories;

import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.ui.requests.ListAllFlagsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface FlagsRepositoryCustom{
    Page<FeatureFlag> findAllWithFilter(UUID projectId, ListAllFlagsRequest request, Pageable pageable);
}
