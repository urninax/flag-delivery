package me.urninax.flagdelivery.organisation.ui.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.services.MembershipsService;
import me.urninax.flagdelivery.organisation.shared.MemberWithActivityDTO;
import me.urninax.flagdelivery.organisation.ui.models.requests.ChangeMembersRoleRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrganisationMembershipsController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
public class OrganisationMembershipsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MembershipsService membershipsService;

    private final UUID memberId = UUID.randomUUID();

    @Nested
    @DisplayName("GET /api/v1/organisation/members")
    class GetMembersCases{
        @Test
        @DisplayName("With valid request -> 200")
        void getMembers_withValidRequest_shouldReturn200AndPageResponse() throws Exception {
            MemberWithActivityDTO dto = MemberWithActivityDTO.builder()
                    .id(UUID.randomUUID())
                    .name("TestName")
                    .email("test@example.com")
                    .role(OrgRole.ADMIN)
                    .lastSeen(Instant.now().minus(20, ChronoUnit.MINUTES))
                    .build();

            List<MemberWithActivityDTO> content = List.of(dto);
            Page<MemberWithActivityDTO> page = new PageImpl<>(content, PageRequest.of(0, 25), content.size());

            when(membershipsService.getMembers(any(), any())).thenReturn(page);

            mockMvc.perform(get("/api/v1/organisation/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(25))
                    .andExpect(jsonPath("$.total_elements").value(1))
                    .andExpect(jsonPath("$.total_pages").value(1))
                    .andExpect(jsonPath("$.content[0].id").value(dto.getId().toString()))
                    .andExpect(jsonPath("$.content[0].name").value(dto.getName()))
                    .andExpect(jsonPath("$.content[0].role").value(dto.getRole().name()))
                    .andExpect(jsonPath("$.content[0].last_seen").value(dto.getLastSeen().toString()));

            verify(membershipsService, times(1)).getMembers(any(), any());
        }

        @Test
        @DisplayName("With invalid request -> 400")
        void getMembers_withInvalidRequest_shouldReturn400() throws Exception {
            mockMvc.perform(get("/api/v1/organisation/members")
                            .param("roles", "not-a-role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(membershipsService, times(0)).getMembers(any(), any());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/organisation/members/{uuid}")
    class ChangeRoleCases {

        @Test
        @DisplayName("With valid request -> 200 OK")
        void changeRole_withValidRequest_shouldReturn200() throws Exception {
            ChangeMembersRoleRequest request = new ChangeMembersRoleRequest(OrgRole.ADMIN);

            mockMvc.perform(patch("/api/v1/organisation/members/{uuid}", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(membershipsService, times(1)).changeMembersRole(eq(memberId), any(ChangeMembersRoleRequest.class));
        }

        @Test
        @DisplayName("With invalid role (null) -> 400 Bad Request")
        void changeRole_withInvalidRole_shouldReturn400() throws Exception {
            ChangeMembersRoleRequest invalidRequest = new ChangeMembersRoleRequest(null);

            mockMvc.perform(patch("/api/v1/organisation/members/{uuid}", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("With malformed JSON -> 400 Bad Request")
        void changeRole_withMalformedJson_shouldReturn400() throws Exception {
            mockMvc.perform(patch("/api/v1/organisation/members/{uuid}", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content("{\"invalid_json\": \"test\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("With invalid UUID -> 400 Bad Request")
        void changeRole_withInvalidUuid_shouldReturn400() throws Exception {
            mockMvc.perform(patch("/api/v1/organisation/members/{uuid}", "not-a-uuid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new ChangeMembersRoleRequest(OrgRole.ADMIN))))
                    .andExpect(status().isBadRequest());
        }
    }

}
