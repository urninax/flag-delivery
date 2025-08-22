package me.urninax.flagdelivery.user.ui.models.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChangePasswordRequest{
    @JsonProperty("current_password")
    @NotBlank(message = "Current password cannot be blank")
    @Size(min = 8, max = 64, message = "Password must contain at least 8 characters")
    private String currentPassword;

    @JsonProperty("new_password")
    @NotBlank(message = "New password cannot be blank")
    @Size(min = 8, max = 64, message = "Password must contain at least 8 characters")
    private String newPassword;

    @JsonProperty("new_password_confirmation")
    @NotBlank(message = "New password confirmation cannot be blank")
    @Size(min = 8, max = 64, message = "Password must contain at least 8 characters")
    private String newPasswordConfirmation;
}
