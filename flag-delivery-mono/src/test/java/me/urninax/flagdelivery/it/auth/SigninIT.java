package me.urninax.flagdelivery.it.auth;

import me.urninax.flagdelivery.user.ui.models.requests.SigninRequest;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SigninIT extends AbstractAuthIT{
    @Container
    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    private SignupRequest signupRequest;

    @BeforeAll
    void prepare(){
        signupRequest = SignupRequest.builder()
                .firstName("CI First Name")
                .lastName("CI Last Name")
                .email("ci.email." + UUID.randomUUID() + "@example.com")
                .password("CITestPassword123!")
                .build();

        ResponseEntity<?> response = sendCreateUserRequest(signupRequest, defaultHeaders());
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Status code should have been 201");
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
            ResponseEntity<?> response = sendSigninUserRequest(signinRequest, defaultHeaders());

            assertEquals(HttpStatus.OK, response.getStatusCode(),
                    "User sign in with valid credentials should have returned 200");

            String authHeader = response.getHeaders().getFirst("Authorization");
            assertNotNull(authHeader, "Authorization header should have not been null");
            assertTrue(authHeader.startsWith("Bearer "), "Authorization Header should start with Bearer");
        }

        @Test
        @DisplayName("With invalid credentials -> 401")
        public void signin_withInvalidCredentials_shouldReturn401(){
            signinRequest.setPassword("bad-password");

            ResponseEntity<?> response = sendSigninUserRequest(signinRequest, defaultHeaders());
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                    "Response status code should have been 401");
        }

        @Test
        @DisplayName("With empty body -> 401")
        public void signin_withEmptyBody_shouldReturn401(){
            ResponseEntity<?> response = sendSigninUserRequest(null, defaultHeaders());
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                    "Response status code should have been 401");
        }
    }

    @Nested
    @DisplayName("JWT Tests")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class JwtCases{
        private String jwt;

        @BeforeEach
        void prepare(){
            SigninRequest signinRequest = SigninRequest.builder()
                    .email(signupRequest.getEmail())
                    .password(signupRequest.getPassword())
                    .build();

            ResponseEntity<?> response = sendSigninUserRequest(signinRequest, defaultHeaders());
            assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status code should have been 200");
            jwt = response.getHeaders().getFirst("Authorization");
        }

        @Test
        @DisplayName("With valid JWT -> 200")
        public void jwtAuth_withValidJwt_shouldReturn200(){
            HttpEntity<HttpHeaders> entity = new HttpEntity<>(authHeaders(jwt));
            ResponseEntity<?> response = template.exchange("/api/v1/users/test", HttpMethod.GET, entity, Object.class);

            assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status code should have been 200");
        }

        @Test
        @DisplayName("With invalid JWT -> 401")
        public void jwtAuth_withInvalidJwt_shouldReturn401(){
            HttpEntity<HttpHeaders> entity = new HttpEntity<>(authHeaders("Bearer invalid"));
            ResponseEntity<?> response = template.exchange("/api/v1/users/test", HttpMethod.GET, entity, Object.class);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), "Response status code should have been 401");
        }

        @Test
        @DisplayName("With no authorization header -> 403")
        public void jwtAuth_withNoAuthHeader_shouldReturn403(){
            HttpEntity<HttpHeaders> entity = new HttpEntity<>(defaultHeaders());
            ResponseEntity<?> response = template.exchange("/api/v1/users/test", HttpMethod.GET, entity, Object.class);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Response status code should have been 403");
        }

    }

}
