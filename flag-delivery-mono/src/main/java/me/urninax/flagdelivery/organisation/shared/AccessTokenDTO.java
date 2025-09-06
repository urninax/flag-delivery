package me.urninax.flagdelivery.organisation.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class AccessTokenDTO{
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("hint")
    private String tokenHint;

    @JsonProperty("name")
    private String name;

    @JsonProperty("role")
    private OrgRole role;

    @JsonProperty("issued_at")
    private Instant issuedAt;

    @JsonProperty("last_used")
    private Instant lastUsed;

    @JsonProperty("is_service")
    private boolean isService;

    @JsonProperty("member_id")
    private UUID memberId;
}
