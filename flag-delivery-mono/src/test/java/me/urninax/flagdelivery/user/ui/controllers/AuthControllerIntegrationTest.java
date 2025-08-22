package me.urninax.flagdelivery.user.ui.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.urninax.flagdelivery.user.repositories.UsersRepository;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class AuthControllerIntegrationTest{

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private SignupRequest signupRequest;

    @Autowired
    private UsersRepository usersRepository;

    @BeforeEach
    void setUp(){
        signupRequest = SignupRequest.builder()
                .email("testEmail@email.com")
                .firstName("Test first name")
                .lastName("Test last name")
                .password("TestPassword123")
                .build();
    }

    @AfterEach
    void tearDown(){
        usersRepository.deleteAll();
    }


    @Test
    @DisplayName("POST /api/v1/auth/signup with valid request -> 201")
    @Order(1)
    void signup_withValidRequest_shouldReturn201() throws JsonProcessingException{
        HttpEntity<String> entity = new HttpEntity<>(
                objectMapper.writeValueAsString(signupRequest), defaultHeaders());

        ResponseEntity<?> response =
                testRestTemplate.postForEntity("/api/v1/auth/signup", entity, Object.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                () -> "Incorrect status code returned");
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup with invalid email -> 400")
    @Order(2)
    void signup_withInvalidEmail_shouldReturn400() throws JsonProcessingException{
        signupRequest.setEmail("invalid-email");

        HttpEntity<String> entity = new HttpEntity<>(
                objectMapper.writeValueAsString(signupRequest), defaultHeaders());

        ResponseEntity<?> response = testRestTemplate.exchange(
                "/api/v1/auth/signup",
                HttpMethod.POST,
                entity,
                Object.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Invalid response status");
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup with missing password -> 400")
    @Order(3)
    void signup_withMissingPassword_shouldReturn400() throws JsonProcessingException{
        signupRequest.setPassword(null);

        HttpEntity<String> entity = new HttpEntity<>(
                objectMapper.writeValueAsString(signupRequest), defaultHeaders());

        ResponseEntity<?> response = testRestTemplate.exchange(
                "/api/v1/auth/signup",
                HttpMethod.POST,
                entity,
                Object.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Invalid response status");
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup with empty body -> 400")
    @Order(4)
    void signup_withEmptyBody_shouldReturn400(){
        HttpEntity<String> entity = new HttpEntity<>(null, defaultHeaders());

        ResponseEntity<?> response = testRestTemplate.exchange(
                "/api/v1/auth/signup",
                HttpMethod.POST,
                entity,
                Object.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Invalid response status");
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup with existing email -> 409")
    @Order(5)
    void signup_withExistingEmail_shouldReturn409() throws JsonProcessingException{
        String uniqueEmail = "test_" + UUID.randomUUID() + "@example.com";
        signupRequest.setEmail(uniqueEmail);

        HttpEntity<String> entity = new HttpEntity<>(
                objectMapper.writeValueAsString(signupRequest), defaultHeaders());

        ResponseEntity<?> firstResponse =
                testRestTemplate.postForEntity("/api/v1/auth/signup", entity, Object.class);

        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode(), "Initial signup failed.");

        ResponseEntity<?> response =
                testRestTemplate.postForEntity("/api/v1/auth/signup", entity, Object.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode(),
                () -> "Incorrect status code returned");
    }

    private HttpHeaders defaultHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
