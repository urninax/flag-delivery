package me.urninax.flagdelivery.it.accesstokens;

import me.urninax.flagdelivery.organisation.services.MembershipsService;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateAccessTokenRequest;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateOrganisationRequest;
import me.urninax.flagdelivery.user.ui.models.requests.SigninRequest;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import me.urninax.flagdelivery.user.utils.JwtUtils;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public abstract class AbstractAccessTokensIT{
    @Autowired
    protected TestRestTemplate template;

    @Autowired
    protected JwtUtils jwtUtils;

    @Autowired
    protected MembershipsService membershipsService;
    protected HttpHeaders defaultHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        return headers;
    }

    protected HttpHeaders authHeaders(String bearer){
        HttpHeaders headers = defaultHeaders();
        headers.set("Authorization", bearer);
        return headers;
    }

    protected SignupRequest createUser(){
        SignupRequest signupRequest = SignupRequest.builder().firstName("CI First Name").lastName("CI Last Name").email("ci." + UUID.randomUUID() + "@example.com").password("CIPassword123!").build();

        HttpEntity<SignupRequest> signupRequestEntity = new HttpEntity<>(signupRequest, defaultHeaders());
        ResponseEntity<?> signupResponse = template.postForEntity("/api/v1/auth/signup", signupRequestEntity, Object.class);
        assertEquals(HttpStatus.CREATED, signupResponse.getStatusCode(), "Signup should succeed for IT setup");

        return signupRequest;
    }

    protected String signinUser(String email, String password){
        SigninRequest signinRequest = SigninRequest.builder()
                .email(email)
                .password(password)
                .build();

        HttpEntity<SigninRequest> signinRequestEntity = new HttpEntity<>(signinRequest, defaultHeaders());
        ResponseEntity<Void> signinResponse = template.postForEntity("/api/v1/auth/signin", signinRequestEntity, Void.class);
        assertEquals(HttpStatus.OK, signinResponse.getStatusCode(), "Signin should return 200");

        String authHeader = signinResponse.getHeaders().getFirst("Authorization");
        assertNotNull(authHeader, "Signin should return Authorization token");
        assertTrue(authHeader.startsWith("Bearer "), "Authorization token should be Bearer token");

        return authHeader;
    }

    protected String createOrganisationForUser(String jwt){
        CreateOrganisationRequest createOrganisationRequest = CreateOrganisationRequest.builder().name("CI Organisation name " + UUID.randomUUID()).build();
        HttpHeaders headersWithJwt = defaultHeaders();
        headersWithJwt.set("Authorization", jwt);

        HttpEntity<CreateOrganisationRequest> createOrganisationRequestEntity = new HttpEntity<>(createOrganisationRequest, headersWithJwt);
        ResponseEntity<Void> createOrganisationResponse = template.postForEntity("/api/v1/organisation", createOrganisationRequestEntity, Void.class);
        assertEquals(HttpStatus.CREATED, createOrganisationResponse.getStatusCode(), "Create organisation should return 201");

        return createOrganisationResponse.getHeaders().getFirst("Location");
    }

    protected ResponseEntity<?> sendCreateTokenRequest(CreateAccessTokenRequest request, HttpHeaders headers){
        HttpEntity<CreateAccessTokenRequest> httpEntity = new HttpEntity<>(request, headers);
        return template.postForEntity("/api/v1/organisation/access-tokens", httpEntity, Object.class);
    }
}
