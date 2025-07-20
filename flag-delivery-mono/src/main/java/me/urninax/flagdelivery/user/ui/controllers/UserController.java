package me.urninax.flagdelivery.user.ui.controllers;

import jakarta.validation.Valid;
import me.urninax.flagdelivery.user.services.UsersService;
import me.urninax.flagdelivery.user.ui.models.requests.SigninRequest;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController{
    private final UsersService usersService;

    @Autowired
    public UserController(UsersService usersService){
        this.usersService = usersService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid SignupRequest signupRequest){
        usersService.createUser(signupRequest);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
