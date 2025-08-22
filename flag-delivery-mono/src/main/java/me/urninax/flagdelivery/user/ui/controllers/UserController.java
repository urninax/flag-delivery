package me.urninax.flagdelivery.user.ui.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.user.security.CurrentUser;
import me.urninax.flagdelivery.user.services.UsersServiceImpl;
import me.urninax.flagdelivery.user.ui.models.requests.ChangePasswordRequest;
import me.urninax.flagdelivery.user.ui.models.requests.UpdatePersonalInfoRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
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

//    @GetMapping("/test")
//    public ResponseEntity<?> test(@RequestParam String token){
//        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        UserEntity userEntity = usersService.getUserById(userPrincipal.getId());
//
//        UserDTO userDTO = userMapper.toDTO(userEntity);
//        return new ResponseEntity<>(userDTO, HttpStatus.OK);
//        return new ResponseEntity<>(String.format("User authorities: %s", SecurityContextHolder.getContext().getAuthentication().getAuthorities()), HttpStatus.OK);
//        String hashedToken = AccessTokenUtils.hashSha256(token);
//        AccessToken accessToken = accessTokenRepository.findByHashedToken(hashedToken)
//                .orElseThrow(() -> new BadCredentialsException("Invalid access token"));
//
//        return new ResponseEntity<>(accessToken.getOwner().getId(), HttpStatus.OK);
//    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testAdminRole(){
        return ResponseEntity.ok().build();
    }
}
