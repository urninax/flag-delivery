package me.urninax.flagdelivery.organisation.ui.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.services.AccessTokenService;
import me.urninax.flagdelivery.organisation.services.OrganisationsService;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateAccessTokenRequest;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateOrganisationRequest;
import me.urninax.flagdelivery.user.security.UserPrincipal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/organisation")
@RequiredArgsConstructor
public class OrganisationsController{
    private final OrganisationsService organisationsService;
    private final AccessTokenService accessTokenService;

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

    @PostMapping("/access-tokens")
    public ResponseEntity<?> createAccessToken(@RequestBody @Valid CreateAccessTokenRequest request){
        UUID userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        String token = accessTokenService.issueToken(userId, request);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", token);

        return new ResponseEntity<>(httpHeaders, HttpStatus.OK);
    }
}
