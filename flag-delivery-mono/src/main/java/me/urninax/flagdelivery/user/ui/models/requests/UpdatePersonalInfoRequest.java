package me.urninax.flagdelivery.user.ui.models.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdatePersonalInfoRequest{
    @JsonProperty("first_name")
    @Size(min = 2, max = 50, message = "First name must contain between 2 and 50 characters")
    private String firstName;

    @JsonProperty("last_name")
    @Size(min = 2, max = 50, message = "Last name must contain between 2 and 50 characters")
    private String lastName;

    @Email(message = "Value is not an email")
    private String email;
}
