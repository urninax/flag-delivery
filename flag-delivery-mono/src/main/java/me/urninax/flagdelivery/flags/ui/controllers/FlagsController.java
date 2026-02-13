package me.urninax.flagdelivery.flags.ui.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.services.FlagsService;
import me.urninax.flagdelivery.flags.shared.FeatureFlagDTO;
import me.urninax.flagdelivery.flags.ui.requests.CreateFeatureFlagRequest;
import me.urninax.flagdelivery.flags.ui.requests.ListAllFlagsRequest;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.ui.models.responses.PageResponse;
import me.urninax.flagdelivery.shared.security.enums.AuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresAuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectKey}/flags")
@RequiredArgsConstructor
@RequiresAuthMethod(AuthMethod.ACCESS_TOKEN)
public class FlagsController{
    private final FlagsService flagsService;

    @PostMapping
    @RequiresRole(OrgRole.WRITER)
    public ResponseEntity<?> createFlag(@PathVariable String projectKey,
                                        @Valid @RequestBody CreateFeatureFlagRequest request){
        FeatureFlagDTO flagDTO = flagsService.createFlag(projectKey, request);
        return new ResponseEntity<>(flagDTO, HttpStatus.OK);
    }

    @GetMapping
    @RequiresRole(OrgRole.READER)
    public ResponseEntity<?> listFlags(@PathVariable String projectKey,
                                       @PageableDefault Pageable pageable,
                                       @RequestParam(name = "filter", required = false) ListAllFlagsRequest request){
        Page<FeatureFlagDTO> flagDtoPage = flagsService.getPaginatedFlags(projectKey, pageable, request);
        return new ResponseEntity<>(new PageResponse<>(flagDtoPage), HttpStatus.OK);
    }

    @GetMapping("/{flagKey}")
    @RequiresRole(OrgRole.READER)
    public ResponseEntity<?> getFlag(@PathVariable String projectKey,
                                     @PathVariable String flagKey){
        FeatureFlagDTO flagDTO = flagsService.getFlag(projectKey, flagKey);
        return new ResponseEntity<>(flagDTO, HttpStatus.OK);
    }

    @PatchMapping("/{flagKey}")
    @RequiresRole(OrgRole.WRITER)
    public ResponseEntity<?> editFlag(@PathVariable String projectKey,
                                      @PathVariable String flagKey){
        return null;
    }

    @DeleteMapping("/{flagKey}")
    @RequiresRole(OrgRole.WRITER)
    public ResponseEntity<?> deleteFlag(@PathVariable String projectKey,
                                        @PathVariable String flagKey){
        flagsService.deleteFlag(projectKey, flagKey);
        return ResponseEntity.ok().build();
    }


}
