package me.urninax.flagdelivery.organisation.ui.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.urninax.flagdelivery.organisation.models.invitation.InvitationStatus;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.services.InvitationsService;
import me.urninax.flagdelivery.organisation.shared.InvitationOrganisationDTO;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateInvitationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrganisationInvitationsController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
public class OrganisationInvitationsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InvitationsService invitationsService;


    @Nested
    @DisplayName("POST /api/v1/organisation/invitations")
    class InviteCases{
        private CreateInvitationRequest request;

        @BeforeEach
        void setup(){
            request = CreateInvitationRequest.builder()
                    .email("test@example.com")
                    .role(OrgRole.ADMIN)
                    .message("Test invitation message")
                    .build();
        }

        @Test
        @DisplayName("With valid request -> 201")
        void invite_withValidRequest_shouldReturn201() throws Exception {
            mockMvc.perform(post("/api/v1/organisation/invitations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(invitationsService, times(1)).createInvitation(request);
        }

        @Test
        @DisplayName("With invalid request -> 400")
        void invite_withInvalidRequest_shouldReturn400() throws Exception{
            request.setEmail("invalid email");

            mockMvc.perform(post("/api/v1/organisation/invitations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(invitationsService, times(0)).createInvitation(request);
        }

        @Test
        @DisplayName("With wrong content type -> 415")
        void invite_withWrongContentType_shouldReturn415() throws Exception{
            mockMvc.perform(post("/api/v1/organisation/invitations")
                            .contentType(MediaType.APPLICATION_XML)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/organisation/invitations")
    class ListInvitationsCases{

        @Test
        @DisplayName("With valid request -> 200")
        void listOrgInvitations_withValidRequest_shouldReturn200() throws Exception {
            InvitationOrganisationDTO responseDTO = InvitationOrganisationDTO.builder()
                    .email("test@example.com")
                    .invitedBy("Test user")
                    .role(OrgRole.ADMIN)
                    .status(InvitationStatus.PENDING)
                    .message("Test invitation message")
                    .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                    .createdAt(Instant.now().minus(1, ChronoUnit.DAYS))
                    .updatedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                    .build();

            List<InvitationOrganisationDTO> content = List.of(responseDTO);
            Page<InvitationOrganisationDTO> page = new PageImpl<>(content, PageRequest.of(0, 25), content.size());
            when(invitationsService.listOrganisationInvitations(any(), any())).thenReturn(page);

            mockMvc.perform(get("/api/v1/organisation/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(25))
                    .andExpect(jsonPath("$.total_elements").value(1))
                    .andExpect(jsonPath("$.total_pages").value(1))
                    .andExpect(jsonPath("$.content[0].email").value(responseDTO.getEmail()))
                    .andExpect(jsonPath("$.content[0].invited_by").value(responseDTO.getInvitedBy()))
                    .andExpect(jsonPath("$.content[0].role").value(responseDTO.getRole().name()))
                    .andExpect(jsonPath("$.content[0].status").value(responseDTO.getStatus().name()))
                    .andExpect(jsonPath("$.content[0].message").value(responseDTO.getMessage()))
                    .andExpect(jsonPath("$.content[0].expires_at").value(responseDTO.getExpiresAt().toString()))
                    .andExpect(jsonPath("$.content[0].created_at").value(responseDTO.getCreatedAt().toString()))
                    .andExpect(jsonPath("$.content[0].updated_at").value(responseDTO.getUpdatedAt().toString()));

            verify(invitationsService, times(1)).listOrganisationInvitations(any(), any());
        }

        @Test
        @DisplayName("With invalid request -> 400")
        void listOrgInvitations_withInvalidRequest_shouldReturn400() throws Exception {
            mockMvc.perform(get("/api/v1/organisation/invitations")
                            .param("status", "test")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/organisation/{uuid}/revoke")
    class RevokeInvitationCases{

        @Test
        @DisplayName("With valid request -> 204")
        void revokeInvitation_withValidRequest_shouldReturn204() throws Exception {
            UUID invitationId = UUID.randomUUID();

            mockMvc.perform(post("/api/v1/organisation/invitations/{uuid}/revoke", invitationId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(invitationsService, times(1)).revokeInvitation(invitationId);
        }

        @Test
        @DisplayName("With invalid UUID -> 400")
        void revokeInvitation_withInvalidUUID_shouldReturn400() throws Exception {
            mockMvc.perform(post("/api/v1/organisation/invitations/{uuid}/revoke", "not-a-uuid")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(invitationsService, times(0)).revokeInvitation(any());
        }
    }

}
