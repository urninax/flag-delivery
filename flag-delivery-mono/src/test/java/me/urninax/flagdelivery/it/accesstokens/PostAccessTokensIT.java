package me.urninax.flagdelivery.it.accesstokens;

import io.jsonwebtoken.Claims;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateAccessTokenRequest;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("POST /api/v1/organisation/access-tokens")
public class PostAccessTokensIT extends AbstractAccessTokensIT{
    private String jwt;

    private String organisationId;

    private CreateAccessTokenRequest request;

    @BeforeEach
    void setUp(){
        //create user
        SignupRequest signupRequest = createUser();
        String jwt = signinUser(signupRequest.getEmail(), signupRequest.getPassword());
        this.jwt = jwt;

        String organisationLocation = createOrganisationForUser(jwt);

        if(organisationLocation != null){
            organisationId = Arrays.stream(organisationLocation.split("/")).toList().getLast();
        }

        request = CreateAccessTokenRequest.builder().name("CI Token").role(OrgRole.READER).isService(true).build();
    }

    @Test
    @DisplayName("With valid request and JWT -> 201 and Authorization header with api- prefix")
    void createAccessToken_withValidRequestAndJWT_shouldReturn201AndAuthorizationHeader(){
        HttpHeaders authHeaders = authHeaders(jwt);

        ResponseEntity<?> response = sendCreateTokenRequest(request, authHeaders);
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Create access token with valid request should return 201");

        String issuedTokenHeader = response.getHeaders().getFirst("Authorization");
        assertNotNull(issuedTokenHeader, "Response should include Authorization header with issued API token");
        assertTrue(issuedTokenHeader.replace("Bearer ", "").startsWith("api-"), "Issued token should start with api-");
    }

    @Test
    @DisplayName("With invalid request (empty name) and JWT -> 400")
    void createAccessToken_withInvalidRequestAndJWT_shouldReturn400(){
        HttpHeaders authHeaders = authHeaders(jwt);

        request.setName("");

        ResponseEntity<?> response = sendCreateTokenRequest(request, authHeaders);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Create access token with invalid request should return 400");
    }

    @Test
    @DisplayName("With access token role higher than user's one -> 403")
    void createAccessToken_whenTokenRoleIsHigherThanUsers_shouldReturn403(){
        SignupRequest signupRequest = createUser();
        String secondUserAuthToken = signinUser(signupRequest.getEmail(), signupRequest.getPassword());
        String secondUserJwt = secondUserAuthToken.substring(7);
        Claims claims = jwtUtils.parse(secondUserJwt);

        String secondUserId = claims.getSubject();

        membershipsService.addMembership(UUID.fromString(organisationId), UUID.fromString(secondUserId), OrgRole.READER);

        HttpHeaders authHeaders = authHeaders(secondUserJwt);

        request.setRole(OrgRole.ADMIN);

        ResponseEntity<?> response = sendCreateTokenRequest(request, authHeaders);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Should have returned Forbidden, but got " + response.getStatusCode());
        assertNull(response.getHeaders().get("Authorization"));
    }


    @Test
    @DisplayName("When user has no organisation -> 403")
    void createAccessToken_whenUserHasNoOrganisation_shouldReturn403(){
        HttpHeaders authHeaders = authHeaders(jwt);

        HttpEntity<Void> deleteOrganisationEntity = new HttpEntity<>(authHeaders);
        ResponseEntity<Void> deleteOrganisationResponse = template.exchange("/api/v1/organisation", HttpMethod.DELETE, deleteOrganisationEntity, Void.class);
        assertEquals(HttpStatus.OK, deleteOrganisationResponse.getStatusCode(), "Delete organisation should have returned 200");

        ResponseEntity<?> createTokenResponse = sendCreateTokenRequest(request, authHeaders);

        assertEquals(HttpStatus.BAD_REQUEST, createTokenResponse.getStatusCode(), "HTTP Status for access token creation without organisation should be 400");
        assertNull(createTokenResponse.getHeaders().get("Authorization"));
    }
}
