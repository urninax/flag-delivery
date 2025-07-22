package me.urninax.flagdelivery.user.ui.controllers;

import jakarta.validation.Valid;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.security.UserPrincipal;
import me.urninax.flagdelivery.user.services.UsersServiceImpl;
import me.urninax.flagdelivery.user.shared.UserDTO;
import me.urninax.flagdelivery.user.ui.models.requests.ChangePasswordRequest;
import me.urninax.flagdelivery.user.ui.models.requests.UpdatePersonalInfoRequest;
import me.urninax.flagdelivery.user.utils.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController{
    private final UsersServiceImpl usersService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UsersServiceImpl usersService, UserMapper userMapper){
        this.usersService = usersService;
        this.userMapper = userMapper;
    }

    @PatchMapping("/update-personal-info")
    public ResponseEntity<?> updatePersonalInfo(@RequestBody @Valid UpdatePersonalInfoRequest updateInfoRequest){
        UUID userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        usersService.updateUser(updateInfoRequest, userId);

        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest){
        UUID userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        usersService.changeUserPassword(changePasswordRequest, userId);

        return ResponseEntity.accepted().build();
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(){
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = usersService.getUserById(userPrincipal.getId());

        UserDTO userDTO = userMapper.toDTO(userEntity);

        return new ResponseEntity<>(userDTO, HttpStatus.OK);
//        return new ResponseEntity<>(String.format("User authorities: %s", SecurityContextHolder.getContext().getAuthentication().getAuthorities()), HttpStatus.OK);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testAdminRole(){
        return ResponseEntity.ok().build();
    }
}
