package me.urninax.flagdelivery.organisation.ui.controllers.integration;

import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateAccessTokenRequest;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateOrganisationRequest;
import me.urninax.flagdelivery.user.ui.models.requests.SigninRequest;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@Testcontainers
public class AccessTokensControllerIT{
    @Autowired TestRestTemplate template;
    private String jwt;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @BeforeEach
    void setUp(){
        //create user
        SignupRequest signupRequest = SignupRequest.builder()
                .firstName("CI First Name")
                .lastName("CI Last Name")
                .email("ci."+ UUID.randomUUID() +"@example.com")
                .password("CIPassword123!")
                .build();

        HttpEntity<SignupRequest> signupRequestEntity = new HttpEntity<>(signupRequest, defaultHeaders());
        ResponseEntity<?> signupResponse = template.postForEntity("/api/v1/auth/signup", signupRequestEntity, Object.class);
        assertEquals(HttpStatus.CREATED, signupResponse.getStatusCode(), "Signup should succeed for IT setup");

        SigninRequest signinRequest = SigninRequest.builder()
                .email(signupRequest.getEmail())
                .password(signupRequest.getPassword())
                .build();

        HttpEntity<SigninRequest> signinRequestEntity = new HttpEntity<>(signinRequest, defaultHeaders());
        ResponseEntity<Void> signinResponse = template.postForEntity("/api/v1/auth/signin", signinRequestEntity, Void.class);
        assertEquals(HttpStatus.OK, signinResponse.getStatusCode(), "Signin should return 200");

        String authHeader = signinResponse.getHeaders().getFirst("Authorization");
        assertNotNull(authHeader, "Signin should return Authorization token");
        assertTrue(authHeader.startsWith("Bearer "), "Authorization token should be Bearer token");
        jwt = authHeader;

        CreateOrganisationRequest createOrganisationRequest = CreateOrganisationRequest.builder()
                .name("CI Organisation name " + UUID.randomUUID())
                .build();
        HttpHeaders headersWithJwt = defaultHeaders();
        headersWithJwt.set("Authorization", jwt);

        HttpEntity<CreateOrganisationRequest> createOrganisationRequestEntity = new HttpEntity<>(createOrganisationRequest, headersWithJwt);
        ResponseEntity<Void> createOrganisationResponse = template.postForEntity("/api/v1/organisation", createOrganisationRequestEntity, Void.class);
        assertEquals(HttpStatus.CREATED, createOrganisationResponse.getStatusCode(), "Create organisation should return 201");
    }

    @Nested
    @DisplayName("POST /api/v1/organisation/access-tokens")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CreateAccessTokensCases{
        CreateAccessTokenRequest request;

        @BeforeEach
        void setUp(){
            request = CreateAccessTokenRequest.builder()
                    .name("CI Token")
                    .role(OrgRole.READER)
                    .isService(true)
                    .build();
        }

        @Test
        @DisplayName("With valid request and JWT -> 201 and Authorization header with api- prefix")
        void createAccessToken_withValidRequestAndJWT_shouldReturn201AndAuthorizationHeader(){
            HttpHeaders headersWithJwt = defaultHeaders();
            headersWithJwt.set("Authorization", jwt);

            HttpEntity<CreateAccessTokenRequest> httpEntity = new HttpEntity<>(request, headersWithJwt);
            ResponseEntity<Void> response = template.postForEntity("/api/v1/organisation/access-tokens", httpEntity, Void.class);
            assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Create access token with valid request should return 201");

            String issuedTokenHeader = response.getHeaders().getFirst("Authorization");
            assertNotNull(issuedTokenHeader, "Response should include Authorization header with issued API token");
            assertTrue(issuedTokenHeader.startsWith("api-"), "Issued token should start with api-");
        }

        @Test
        @DisplayName("With invalid request (empty name) and JWT -> 400")
        void createAccessToken_withInvalidRequestAndJWT_shouldReturn400(){
            HttpHeaders headersWithJWT = defaultHeaders();
            headersWithJWT.set("Authorization", jwt);

            request.setName("");

            HttpEntity<CreateAccessTokenRequest> httpEntity = new HttpEntity<>(request, headersWithJWT);
            ResponseEntity<?> response = template.postForEntity("/api/v1/organisation/access-tokens", httpEntity, Object.class);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Create access token with invalid request should return 400");
        }

        @Test
        @Disabled
        @DisplayName("With access token role higher than user's one -> 403")
        void createAccessToken_whenTokenRoleIsHigherThanUsers_shouldReturn403(){
            HttpHeaders headersWithJWT = defaultHeaders();
            headersWithJWT.set("Authorization", jwt);

            request.setRole(OrgRole.ADMIN);

            HttpEntity<CreateAccessTokenRequest> entity = new HttpEntity<>(request, headersWithJWT);
            ResponseEntity<?> response = template.postForEntity("/api/v1/organisation/access-tokens", entity, Void.class);
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Should have returned Forbidden, but got " + response.getStatusCode());
            assertNull(response.getHeaders().get("Authorization"));
        }


        @Test
        @DisplayName("When user has no organisation -> 403")
        void createAccessToken_whenUserHasNoOrganisation_shouldReturn403(){
            HttpHeaders headersWithJWT = defaultHeaders();
            headersWithJWT.set("Authorization", jwt);

            HttpEntity<Void> deleteOrganisationEntity = new HttpEntity<>(headersWithJWT);
            ResponseEntity<Void> deleteOrganisationResponse = template.exchange(
                    "/api/v1/organisation",
                    HttpMethod.DELETE,
                    deleteOrganisationEntity,
                    Void.class
            );
            assertEquals(HttpStatus.OK, deleteOrganisationResponse.getStatusCode(), "Delete organisation should have returned 200");

            HttpEntity<CreateAccessTokenRequest> createTokenEntity = new HttpEntity<>(request, headersWithJWT);
            ResponseEntity<Void> createTokenResponse = template.postForEntity("/api/v1/organisation/access-tokens", createTokenEntity, Void.class);

            assertEquals(HttpStatus.FORBIDDEN, createTokenResponse.getStatusCode(), "HTTP Status for access token creation without organisation should be 403");
            assertNull(createTokenResponse.getHeaders().get("Authorization"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/organisation/access-tokens")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Disabled
    class GetAccessTokensCases{
        @Test
        @DisplayName("With valid request -> 200 and Access tokens page")
        void getAccessTokens_withValidRequest_shouldReturn200AndTokensPage(){


        }
    }

    private HttpHeaders defaultHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        return headers;
    }
}
