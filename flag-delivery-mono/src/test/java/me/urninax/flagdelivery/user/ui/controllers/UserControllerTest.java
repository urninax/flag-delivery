package me.urninax.flagdelivery.user.ui.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.user.services.UsersServiceImpl;
import me.urninax.flagdelivery.user.ui.models.requests.ChangePasswordRequest;
import me.urninax.flagdelivery.user.ui.models.requests.UpdatePersonalInfoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
public class UserControllerTest{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CurrentUser currentUser;

    @MockitoBean
    private UsersServiceImpl usersService;

    @Nested
    @DisplayName("PATCH /users/update-personal-info")
    class UpdatePersonalInfoCases{
        private UpdatePersonalInfoRequest request;

        @BeforeEach
        void setUp(){
            request = UpdatePersonalInfoRequest.builder()
                    .firstName("New first name")
                    .lastName("New last name")
                    .email("new.email@example.com")
                    .build();

            when(currentUser.getUserId()).thenReturn(UUID.randomUUID());
        }

        @Test
        @DisplayName("With valid request -> 202")
        void updatePersonalInfo_withValidRequest_shouldReturn202() throws Exception{
            mockMvc.perform(patch("/api/v1/users/update-personal-info")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted());

            verify(currentUser, times(1)).getUserId();
            verify(usersService, times(1)).updateUser(any(), any(UUID.class));
        }

        @Test
        @DisplayName("With invalid request -> 400")
        void updatePersonalInfo_withInvalidRequest_shouldReturn400() throws Exception{
            request.setEmail("not-an-email");

            mockMvc.perform(patch("/api/v1/users/update-personal-info")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Value is not an email"));
        }

        @Test
        @DisplayName("With empty request -> 400")
        void updatePersonalInfo_withEmptyRequest_shouldReturn400() throws Exception{
            mockMvc.perform(patch("/api/v1/users/update-personal-info")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("With HTTP method not PATCH -> 405")
        void updatePersonalInfo_withHTTPMethodNotPATCH_shouldReturn409() throws Exception{
            mockMvc.perform(post("/api/v1/users/update-personal-info")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("With ContentType not application/json -> 415")
        void updatePersonalInfo_withContentTypeNotJson_shouldReturn415() throws Exception{
            mockMvc.perform(patch("/api/v1/users/update-personal-info")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_XML)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("With Accept not application/json -> 406")
        void updatePersonalInfo_withAcceptNotJson_shouldReturn406() throws Exception{
            mockMvc.perform(patch("/api/v1/users/update-personal-info")
                    .accept(MediaType.APPLICATION_XML)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotAcceptable());
        }
    }

    @Nested
    @DisplayName("PATCH /users/change-password")
    class ChangePasswordCases{
        private ChangePasswordRequest request;

        @BeforeEach
        void setUp(){
            request = ChangePasswordRequest.builder()
                    .currentPassword("currentPassword1234")
                    .newPassword("newPassword1234")
                    .newPasswordConfirmation("newPassword1234")
                    .build();

            when(currentUser.getUserId()).thenReturn(UUID.randomUUID());
        }

        @Test
        @DisplayName("With valid request -> 202")
        void changePassword_withValidRequest_shouldReturn202() throws Exception{
            mockMvc.perform(patch("/api/v1/users/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted());
        }

        @Test
        @DisplayName("With invalid request -> 400")
        void changePassword_withInvalidRequest_shouldReturn400() throws Exception{
            request.setNewPassword("test");

            mockMvc.perform(patch("/api/v1/users/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("With empty request -> 400")
        void changePassword_withEmptyRequest_shouldReturn400() throws Exception{
            mockMvc.perform(patch("/api/v1/users/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("With HTTP method not PATCH -> 405")
        void changePassword_withHTTPMethodNotPATCH_shouldReturn405() throws Exception{
            mockMvc.perform(post("/api/v1/users/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("With ContentType not application/json -> 415")
        void changePassword_withContentTypeNotJson_shouldReturn415() throws Exception{
            mockMvc.perform(patch("/api/v1/users/change-password")
                            .contentType(MediaType.APPLICATION_XML)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("With Accept not application/json -> 406")
        void changePassword_withAcceptNotJson_shouldReturn406() throws Exception{
            mockMvc.perform(patch("/api/v1/users/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_XML)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotAcceptable());
        }
    }
}
