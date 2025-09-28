package me.urninax.flagdelivery.flags.ui.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.services.FlagsService;
import me.urninax.flagdelivery.flags.shared.FeatureFlagDTO;
import me.urninax.flagdelivery.flags.ui.requests.CreateFeatureFlagRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectKey}/flags")
@RequiredArgsConstructor
public class FlagsController{
    private final FlagsService flagsService;

    @PostMapping
    public ResponseEntity<?> createFlag(@PathVariable("projectKey") String projectKey,
                                        @Valid CreateFeatureFlagRequest request){
        FeatureFlagDTO flagDTO = flagsService.createFlag(projectKey, request);
        return new ResponseEntity<>(flagDTO, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<?> listFlags(){
        return null;
    }

    @GetMapping("/{flagKey}")
    public ResponseEntity<?> getFlag(){
        return null;
    }
}
