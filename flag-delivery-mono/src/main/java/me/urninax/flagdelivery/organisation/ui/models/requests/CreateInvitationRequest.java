package me.urninax.flagdelivery.organisation.ui.models.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;

@Builder
@Data
public class CreateInvitationRequest{
    @Email(message = "Email field should be an email")
    @NotBlank(message = "Email should not be blank")
    private String email;

    @NotNull(message = "Role must be present")
    private OrgRole role;

    @Size(max = 300)
    private String message;
}
