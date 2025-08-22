package me.urninax.flagdelivery.user.ui.controllers;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class AuthControllerIntegrationTest{

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Container
    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    private SignupRequest signupRequest;


    @BeforeEach
    void setUp(){
        signupRequest = SignupRequest.builder()
                .email("testEmail@email.com")
                .firstName("Test first name")
                .lastName("Test last name")
                .password("TestPassword123")
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup with valid request -> 201")
    void signup_withValidRequest_shouldReturn201(){
        ResponseEntity<?> response = sendCreateUserRequest(signupRequest, defaultHeaders());

        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                () -> "Incorrect status code returned");
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup with invalid email -> 400")
    void signup_withInvalidEmail_shouldReturn400(){
        signupRequest.setEmail("invalid-email");

        ResponseEntity<?> response = sendCreateUserRequest(signupRequest, defaultHeaders());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Invalid response status");
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup with missing password -> 400")
    void signup_withMissingPassword_shouldReturn400(){
        signupRequest.setPassword(null);

        ResponseEntity<?> response = sendCreateUserRequest(signupRequest, defaultHeaders());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Invalid response status");
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup with empty body -> 400")
    void signup_withEmptyBody_shouldReturn400(){
        ResponseEntity<?> response = sendCreateUserRequest(null, defaultHeaders());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Invalid response status");
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup with existing email -> 409")
    void signup_withExistingEmail_shouldReturn409(){
        String uniqueEmail = "test_" + UUID.randomUUID() + "@example.com";
        signupRequest.setEmail(uniqueEmail);

        ResponseEntity<?> firstResponse = sendCreateUserRequest(signupRequest, defaultHeaders());

        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode(), "Initial signup failed.");

        ResponseEntity<?> response = sendCreateUserRequest(signupRequest, defaultHeaders());

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode(),
                () -> "Incorrect status code returned");
    }

    private HttpHeaders defaultHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private ResponseEntity<?> sendCreateUserRequest(SignupRequest request, HttpHeaders headers){
        HttpEntity<SignupRequest> entity = new HttpEntity<>(request, headers);
        return testRestTemplate.postForEntity("/api/v1/auth/signup", entity, Object.class);
    }
}
