package me.urninax.flagdelivery.user.ui.controllers;

import me.urninax.flagdelivery.shared.security.enums.AuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresAuthMethod;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("test")
@RestController
@RequestMapping("/api/v1/users")
public class TestUserController {

    @RequiresAuthMethod(AuthMethod.JWT)
    @GetMapping("/test")
    public ResponseEntity<?> securedEndpoint(){
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
