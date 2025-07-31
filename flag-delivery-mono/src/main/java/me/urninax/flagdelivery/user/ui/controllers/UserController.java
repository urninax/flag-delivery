package me.urninax.flagdelivery.user.ui.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.models.AccessToken;
import me.urninax.flagdelivery.organisation.repositories.AccessTokenRepository;
import me.urninax.flagdelivery.user.security.CurrentUser;
import me.urninax.flagdelivery.user.services.UsersServiceImpl;
import me.urninax.flagdelivery.user.ui.models.requests.ChangePasswordRequest;
import me.urninax.flagdelivery.user.ui.models.requests.UpdatePersonalInfoRequest;
import me.urninax.flagdelivery.user.utils.AccessTokenUtils;
import me.urninax.flagdelivery.user.utils.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController{
    private final UsersServiceImpl usersService;
    private final UserMapper userMapper;
    private final AccessTokenRepository accessTokenRepository;
    private final CurrentUser currentUser;

    @PatchMapping("/update-personal-info")
    public ResponseEntity<?> updatePersonalInfo(@RequestBody @Valid UpdatePersonalInfoRequest updateInfoRequest){
        UUID userId = currentUser.getUserId();
        usersService.updateUser(updateInfoRequest, userId);

        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest){
        UUID userId = currentUser.getUserId();
        usersService.changeUserPassword(changePasswordRequest, userId);

        return ResponseEntity.accepted().build();
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(@RequestParam String token){
//        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        UserEntity userEntity = usersService.getUserById(userPrincipal.getId());

//        UserDTO userDTO = userMapper.toDTO(userEntity);
//        return new ResponseEntity<>(userDTO, HttpStatus.OK);
//        return new ResponseEntity<>(String.format("User authorities: %s", SecurityContextHolder.getContext().getAuthentication().getAuthorities()), HttpStatus.OK);
        String hashedToken = AccessTokenUtils.hashSha256(token);
        AccessToken accessToken = accessTokenRepository.findByHashedToken(hashedToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid access token"));

        return new ResponseEntity<>(accessToken.getOwner().getId(), HttpStatus.OK);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testAdminRole(){
        return ResponseEntity.ok().build();
    }
}
