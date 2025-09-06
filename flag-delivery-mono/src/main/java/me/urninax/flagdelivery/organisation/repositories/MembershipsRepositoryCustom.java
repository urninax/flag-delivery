package me.urninax.flagdelivery.organisation.repositories;

import jakarta.annotation.Nullable;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.shared.MemberWithActivityDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MembershipsRepositoryCustom{
    Page<MemberWithActivityDTO> findMembers(
            UUID orgId,
            @Nullable Instant from,
            @Nullable List<OrgRole> roles,
            Pageable pageable);
}
