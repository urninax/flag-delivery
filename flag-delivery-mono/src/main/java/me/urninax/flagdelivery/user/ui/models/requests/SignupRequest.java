package me.urninax.flagdelivery.user.ui.models.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SignupRequest{
    @JsonProperty("first_name")
    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, message = "First name must contain at least 2 characters")
    private String firstName;

    @JsonProperty("last_name")
    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 2, message = "Last name must contain at least 2 characters")
    private String lastName;

    @Email(message = "Value is not an email")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 64, message = "Password must contain at least 8 characters")
    private String password;
}
