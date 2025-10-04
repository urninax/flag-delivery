package me.urninax.flagdelivery.organisation.ui.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.urninax.flagdelivery.organisation.services.OrganisationsService;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateOrganisationRequest;
import me.urninax.flagdelivery.organisation.utils.exceptions.organisation.AlreadyInOrganisationException;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrganisationsController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
public class OrganisationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateOrganisationRequest request;

    @MockitoBean
    private OrganisationsService organisationsService;

    @MockitoBean
    private CurrentUser currentUser;

    private UUID userId;

    @BeforeEach
    void setUp(){
        request = CreateOrganisationRequest.builder()
                .name("Valid name").build();

        userId = UUID.randomUUID();
        when(currentUser.getUserId()).thenReturn(userId);
    }

    @Test
    @DisplayName("POST /api/v1/organisation with valid request -> 201 and Location header")
    void createOrganisation_withValidRequest_shouldReturn201() throws Exception{
        UUID orgId = UUID.randomUUID();
        when(organisationsService.createOrganisation(any())).thenReturn(orgId);

        mockMvc.perform(post("/api/v1/organisation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/organisation/" + orgId)));

        verify(currentUser, times(1)).getUserId();
        verify(organisationsService, times(1)).createOrganisation(any());
    }

    @Test
    @DisplayName("POST /api/v1/organisation with invalid request (blank name) -> 400")
    void createOrganisation_withInvalidRequest_shouldReturn400() throws Exception{
        request.setName("");

        mockMvc.perform(post("/api/v1/organisation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Organisation name cannot be blank")));
    }

    @Test
    @DisplayName("POST /api/v1/organisation with empty body -> 400")
    void createOrganisation_withEmptyBody_shouldReturn400() throws Exception{
        mockMvc.perform(post("/api/v1/organisation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request"));
    }

    @Test
    @DisplayName("POST /api/v1/organisation when already in organisation -> 409")
    void createOrganisation_alreadyExists_shouldReturn409() throws Exception{
        doThrow(new AlreadyInOrganisationException()).when(organisationsService).createOrganisation(any());

        mockMvc.perform(post("/api/v1/organisation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User is already in organisation"));
    }

    @Test
    @DisplayName("POST /api/v1/organisation with HTTP method not POST -> 405")
    void createOrganisation_methodNotAllowed_shouldReturn405() throws Exception{
        mockMvc.perform(patch("/api/v1/organisation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("POST /api/v1/organisation with ContentType not application/json -> 415")
    void createOrganisation_withContentTypeNotJson_shouldReturn415() throws Exception{
        mockMvc.perform(post("/api/v1/organisation")
                        .contentType(MediaType.APPLICATION_XML)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());

    }

    @Test
    @DisplayName("POST /api/v1/organisation with Accept not application/json -> 406")
    void createOrganisation_withAcceptNotJson_shouldReturn406() throws Exception{
        mockMvc.perform(post("/api/v1/organisation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_XML)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotAcceptable());
    }
}
