package me.urninax.flagdelivery.it.testcases.auth;

import me.urninax.flagdelivery.it.AbstractIntegrationTest;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

@DisplayName("POST /api/v1/auth/signup")
public class SignupIT extends AbstractIntegrationTest {
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
        client.post().uri("/api/v1/auth/signup")
                .bodyValue(signupRequest)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup with invalid email -> 400")
    void signup_withInvalidEmail_shouldReturn400(){
        signupRequest.setEmail("invalid-email");

        client.post().uri("/api/v1/auth/signup")
                .bodyValue(signupRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup with missing password -> 400")
    void signup_withMissingPassword_shouldReturn400(){
        signupRequest.setPassword(null);

        client.post().uri("/api/v1/auth/signup")
                .bodyValue(signupRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup with empty body -> 400")
    void signup_withEmptyBody_shouldReturn400(){
        client.post().uri("/api/v1/auth/signup")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup with existing email -> 409")
    void signup_withExistingEmail_shouldReturn409(){
        String uniqueEmail = "test_" + UUID.randomUUID() + "@example.com";
        signupRequest.setEmail(uniqueEmail);

        client.post().uri("/api/v1/auth/signup")
                .bodyValue(signupRequest)
                .exchange()
                .expectStatus().isCreated();

        client.post().uri("/api/v1/auth/signup")
                .bodyValue(signupRequest)
                .exchange()
                .expectStatus().isEqualTo(409);
    }
}
