package me.urninax.flagdelivery.organisation.ui.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.services.OrganisationsService;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateOrganisationRequest;
import me.urninax.flagdelivery.user.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class OrganisationsController{
    private final OrganisationsService organisationsService;
    private final CurrentUser currentUser;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createOrganisation(@RequestBody @Valid CreateOrganisationRequest request){
        UUID userId = currentUser.getUserId();
        UUID newOrgId = organisationsService.createOrganisation(request, userId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newOrgId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteOrganisation(){
        UUID userId = currentUser.getUserId();
        organisationsService.deleteOrganisation(userId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
