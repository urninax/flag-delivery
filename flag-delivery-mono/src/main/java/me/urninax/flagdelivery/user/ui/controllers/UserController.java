package me.urninax.flagdelivery.user.ui.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.security.enums.AuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresAuthMethod;
import me.urninax.flagdelivery.user.services.UsersServiceImpl;
import me.urninax.flagdelivery.user.ui.models.requests.ChangePasswordRequest;
import me.urninax.flagdelivery.user.ui.models.requests.UpdatePersonalInfoRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@RequiresAuthMethod(AuthMethod.JWT)
public class UserController{
    private final UsersServiceImpl usersService;
    private final CurrentUser currentUser;

    @PatchMapping(value = "/update-personal-info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePersonalInfo(@RequestBody @Valid UpdatePersonalInfoRequest updateInfoRequest){
        UUID userId = currentUser.getUserId();
        usersService.updateUser(updateInfoRequest, userId);

        return ResponseEntity.accepted().build();
    }

    @PatchMapping(value = "/change-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest){
        UUID userId = currentUser.getUserId();
        usersService.changeUserPassword(changePasswordRequest, userId);

        return ResponseEntity.accepted().build();
    }
}
