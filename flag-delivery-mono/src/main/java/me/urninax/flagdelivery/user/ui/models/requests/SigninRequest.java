package me.urninax.flagdelivery.user.ui.models.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Valid
public class SigninRequest{
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Value is not an email")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}
