package me.urninax.flagdelivery.user.ui.controllers;

import jakarta.validation.Valid;
import me.urninax.flagdelivery.user.services.UsersService;
import me.urninax.flagdelivery.user.services.UsersServiceImpl;
import me.urninax.flagdelivery.user.ui.models.requests.ChangePasswordRequest;
import me.urninax.flagdelivery.user.ui.models.requests.SigninRequest;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import me.urninax.flagdelivery.user.ui.models.requests.UpdatePersonalInfoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController{
    private final UsersServiceImpl usersService;

    @Autowired
    public UserController(UsersServiceImpl usersService){
        this.usersService = usersService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid SignupRequest signupRequest){
        usersService.createUser(signupRequest);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PatchMapping("/update-personal-info")
    public ResponseEntity<?> updatePersonalInfo(@RequestBody @Valid UpdatePersonalInfoRequest updateInfoRequest){
        String userEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        usersService.updateUser(updateInfoRequest, userEmail);

        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest){
        String userEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        usersService.changeUserPassword(changePasswordRequest, userEmail);

        return ResponseEntity.accepted().build();
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(){
        return new ResponseEntity<>(String.format("User authorities: %s", SecurityContextHolder.getContext().getAuthentication().getAuthorities()), HttpStatus.OK);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testAdminRole(){
        return ResponseEntity.ok().build();
    }
}
