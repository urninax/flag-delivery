package me.urninax.flagdelivery.user.ui.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.urninax.flagdelivery.user.services.UsersServiceImpl;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = AuthController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
public class AuthControllerTest{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsersServiceImpl usersService;

    private SignupRequest signupRequest;
    @BeforeEach
    void setUp(){
        signupRequest = SignupRequest.builder()
                .email("testEmail@email.com")
                .firstName("Maks")
                .lastName("Kapa")
                .password("SpringPassword123")
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup with valid request -> 201")
    void signup_withValidRequest_shouldReturn201() throws Exception{
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        verify(usersService, times(1)).createUser(any());
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup with invalid request -> 400")
    void signup_withBadRequest_shouldReturn400() throws Exception{
        signupRequest.setEmail("not-an-email");

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpectAll(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup when user already exists -> 409")
    void signup_userAlreadyExists_shouldReturn409() throws Exception{
        doThrow(new DataIntegrityViolationException("unique_email"))
                .when(usersService).createUser(any());

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists."));
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup with empty body -> 400")
    void signup_withEmptyBody_shouldReturn400() throws Exception{
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request"));
    }


}
