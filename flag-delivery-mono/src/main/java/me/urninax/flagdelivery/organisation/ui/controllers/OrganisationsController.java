package me.urninax.flagdelivery.organisation.ui.controllers;

import jakarta.validation.Valid;
import me.urninax.flagdelivery.organisation.services.OrganisationsService;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateOrganisationRequest;
import me.urninax.flagdelivery.user.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organisations")
public class OrganisationsController{
    private final OrganisationsService organisationsService;

    @Autowired
    public OrganisationsController(OrganisationsService organisationsService){
        this.organisationsService = organisationsService;
    }

    @PostMapping()
    public ResponseEntity<?> createOrganisation(@RequestBody @Valid CreateOrganisationRequest request){
        UUID userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        UUID newOrgId = organisationsService.createOrganisation(request, userId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newOrgId)
                .toUri();

        return ResponseEntity.created(location).build();
    }
}
