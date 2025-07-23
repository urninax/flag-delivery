package me.urninax.flagdelivery.organisation.ui.models.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;

public record CreateAccessTokenRequest(
        @NotBlank(message = "Name cannot be blank")
        @Size(min = 2, max = 256, message = "Name must not exceed 256 characters")
        String name,

        @NotNull(message = "Role must be present")
        OrgRole role,

        @NotNull(message = "\"is_service\" field must be present")
        @JsonProperty("is_service")
        boolean isService
){}
