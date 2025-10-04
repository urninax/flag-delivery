package me.urninax.flagdelivery.organisation.ui.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.services.OrganisationsService;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateOrganisationRequest;
import me.urninax.flagdelivery.shared.security.enums.AuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresAuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organisation")
@RequiredArgsConstructor
@RequiresAuthMethod(AuthMethod.JWT)
public class OrganisationsController{
    private final OrganisationsService organisationsService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createOrganisation(@RequestBody @Valid CreateOrganisationRequest request){
        UUID newOrgId = organisationsService.createOrganisation(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newOrgId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiresRole(OrgRole.OWNER)
    public ResponseEntity<?> deleteOrganisation(){
        organisationsService.deleteOrganisation();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
