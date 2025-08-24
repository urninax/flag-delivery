package me.urninax.flagdelivery.organisation.ui.models.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Data
public class CreateOrganisationRequest{
    @NotBlank(message = "Organisation name cannot be blank")
    @Size(min = 2, max = 100, message = "Organisation name must be between 2 and 100 characters long")
    private String name;
}
