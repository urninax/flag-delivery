package me.urninax.flagdelivery.it.testcases.auth;

import me.urninax.flagdelivery.it.AbstractIntegrationTest;
import me.urninax.flagdelivery.user.ui.models.requests.SigninRequest;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import org.junit.jupiter.api.*;
import org.springframework.http.*;

import java.util.UUID;

public class SigninIT extends AbstractIntegrationTest {
    private SignupRequest signupRequest;

    @BeforeAll
    void prepare(){
        signupRequest = SignupRequest.builder()
                .firstName("CI First Name")
                .lastName("CI Last Name")
                .email("ci.email." + UUID.randomUUID() + "@example.com")
                .password("CITestPassword123!")
                .build();

        client.post().uri("/api/v1/auth/signup")
                .bodyValue(signupRequest)
                .exchange()
                .expectStatus().isCreated();
    }

    @Nested
    @DisplayName("POST /api/v1/auth/signin")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SigninCases{
        private SigninRequest signinRequest;

        @BeforeEach
        void prepareBeforeEach(){
            signinRequest = SigninRequest.builder()
                    .email(signupRequest.getEmail())
                    .password(signupRequest.getPassword())
                    .build();
        }

        @Test
        @DisplayName("With valid credentials -> 200 + Authorization header")
        public void signin_withValidCredentials_shouldReturn200AndAuthHeader(){
            client.post().uri("/api/v1/auth/signin")
                    .bodyValue(signinRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().exists("Authorization")
                    .expectHeader().valueMatches("Authorization", "^Bearer\\s.+");
        }

        @Test
        @DisplayName("With invalid credentials -> 401")
        public void signin_withInvalidCredentials_shouldReturn401(){
            signinRequest.setPassword("bad-password");

            client.post().uri("/api/v1/auth/signin")
                    .bodyValue(signinRequest)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("With empty body -> 401")
        public void signin_withEmptyBody_shouldReturn401(){
            client.post().uri("/api/v1/auth/signin")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }

    @Nested
    @DisplayName("JWT Tests")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class JwtCases{
        private String authToken;

        @BeforeEach
        void prepare(){
            authToken = helper.signinUser(signupRequest.getEmail(), signupRequest.getPassword());
        }

        @Test
        @DisplayName("With valid JWT -> 200")
        public void jwtAuth_withValidJwt_shouldReturn200(){
            client.get().uri("/api/v1/users/test")
                    .header("Authorization", authToken)
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("With invalid JWT -> 401")
        public void jwtAuth_withInvalidJwt_shouldReturn401(){
            client.get().uri("/api/v1/users/test")
                    .header("Authorization", "Bearer invalid")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("With no authorization header -> 403")
        public void jwtAuth_withNoAuthHeader_shouldReturn403(){
            client.get().uri("/api/v1/users/test")
                    .exchange()
                    .expectStatus().isForbidden();
        }

    }

}
