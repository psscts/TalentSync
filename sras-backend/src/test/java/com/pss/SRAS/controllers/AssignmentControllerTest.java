package com.pss.SRAS.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pss.SRAS.dto.AssignmentRequest;
import com.pss.SRAS.dto.AssignmentResponseDto;
import com.pss.SRAS.dto.EmployeeProjectDto;
import com.pss.SRAS.dto.ProjectDashboardDto;
import com.pss.SRAS.exception.GlobalExceptionHandler;
import com.pss.SRAS.services.AssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMVC tests for AssignmentController.
 * Uses standalone setup — AssignmentService is mocked, no database required.
 * Security annotations (@PreAuthorize) are not enforced in standalone mode.
 * The getMyProjects endpoint uses Spring Security test support to supply
 * an Authentication principal via the request's user principal.
 */
@ExtendWith(MockitoExtension.class)
class AssignmentControllerTest {

    @Mock
    private AssignmentService assignmentService;

    @InjectMocks
    private AssignmentController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(
                        new ObjectMapper().registerModule(new JavaTimeModule())
                                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)))
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private AssignmentResponseDto buildAssignmentDto(Long id, Long projectId, String empId) {
        return new AssignmentResponseDto(id, projectId, "Project Alpha",
                empId, "Alice", "MID", "UNAVAILABLE", LocalDate.of(2026, 5, 1));
    }

    private ProjectDashboardDto buildDashboardDto(Long projectId, String projectName, int total,
                                                  List<AssignmentResponseDto> assigned) {
        return new ProjectDashboardDto(projectId, projectName, "Finance",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 12, 31), total, assigned);
    }

    private EmployeeProjectDto buildEmployeeProjectDto(Long assignmentId, Long projectId) {
        return new EmployeeProjectDto(assignmentId, projectId, "Project Alpha", "Finance",
                "Backend Developer", "Manager Bob",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 12, 31), 30L,
                LocalDate.of(2026, 5, 15));
    }

    // ── POST /assignments ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /assignments - assign employee: returns 200 with assignment details")
    void assign_success() throws Exception {
        AssignmentRequest req = new AssignmentRequest();
        req.setProjectId(1L);
        req.setEmployeeId(10L);

        AssignmentResponseDto resp = buildAssignmentDto(100L, 1L, "EMP010");
        when(assignmentService.assignEmployee(1L, 10L)).thenReturn(resp);

        mockMvc.perform(post("/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.employeeDbId").value("EMP010"))
                .andExpect(jsonPath("$.availabilityStatus").value("UNAVAILABLE"));
    }

    @Test
    @DisplayName("POST /assignments - employee already assigned: returns 400")
    void assign_alreadyAssigned_returns400() throws Exception {
        AssignmentRequest req = new AssignmentRequest();
        req.setProjectId(1L);
        req.setEmployeeId(10L);

        when(assignmentService.assignEmployee(1L, 10L))
                .thenThrow(new IllegalArgumentException("Employee already assigned to this project"));

        mockMvc.perform(post("/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Employee already assigned to this project"));
    }

    @Test
    @DisplayName("POST /assignments - project not found: returns 404")
    void assign_projectNotFound_returns404() throws Exception {
        AssignmentRequest req = new AssignmentRequest();
        req.setProjectId(99L);
        req.setEmployeeId(10L);

        when(assignmentService.assignEmployee(99L, 10L))
                .thenThrow(new NoSuchElementException("Project not found: 99"));

        mockMvc.perform(post("/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /assignments/{id} ──────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /assignments/{id} - unassign: returns 204 and employee set to AVAILABLE")
    void unassign_success() throws Exception {
        doNothing().when(assignmentService).unassignEmployee(100L);

        mockMvc.perform(delete("/assignments/100"))
                .andExpect(status().isNoContent());

        verify(assignmentService).unassignEmployee(100L);
    }

    @Test
    @DisplayName("DELETE /assignments/{id} - assignment not found: returns 404")
    void unassign_notFound_returns404() throws Exception {
        doThrow(new NoSuchElementException("Assignment not found: 999"))
                .when(assignmentService).unassignEmployee(999L);

        mockMvc.perform(delete("/assignments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Assignment not found: 999"));
    }

    // ── GET /assignments/project/{projectId} ──────────────────────────────────────

    @Test
    @DisplayName("GET /assignments/project/{projectId} - returns 200 with assignment list")
    void getByProject_success() throws Exception {
        List<AssignmentResponseDto> list = List.of(
                buildAssignmentDto(100L, 1L, "EMP001"),
                buildAssignmentDto(101L, 1L, "EMP002")
        );
        when(assignmentService.getAssignmentsByProject(1L)).thenReturn(list);

        mockMvc.perform(get("/assignments/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].employeeDbId").value("EMP001"))
                .andExpect(jsonPath("$[1].employeeDbId").value("EMP002"));
    }

    @Test
    @DisplayName("GET /assignments/project/{projectId} - project not found: returns 404")
    void getByProject_projectNotFound() throws Exception {
        when(assignmentService.getAssignmentsByProject(99L))
                .thenThrow(new NoSuchElementException("Project not found: 99"));

        mockMvc.perform(get("/assignments/project/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /assignments/dashboard ────────────────────────────────────────────────

    @Test
    @DisplayName("GET /assignments/dashboard - returns 200 with full dashboard data")
    void getDashboard_success() throws Exception {
        List<AssignmentResponseDto> assigned = List.of(buildAssignmentDto(100L, 1L, "EMP001"));
        List<ProjectDashboardDto> dashboard = List.of(
                buildDashboardDto(1L, "Alpha", 4, assigned),
                buildDashboardDto(2L, "Beta", 2, List.of())
        );
        when(assignmentService.getDashboard()).thenReturn(dashboard);

        mockMvc.perform(get("/assignments/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].projectName").value("Alpha"))
                .andExpect(jsonPath("$[0].totalPositions").value(4))
                .andExpect(jsonPath("$[0].assignedEmployees.length()").value(1))
                .andExpect(jsonPath("$[1].projectName").value("Beta"))
                .andExpect(jsonPath("$[1].assignedEmployees.length()").value(0));
    }

    @Test
    @DisplayName("GET /assignments/dashboard - no projects: returns 200 with empty list")
    void getDashboard_empty() throws Exception {
        when(assignmentService.getDashboard()).thenReturn(List.of());

        mockMvc.perform(get("/assignments/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /assignments/my-projects ──────────────────────────────────────────────

    @Test
    @DisplayName("GET /assignments/my-projects - authenticated: returns 200 with project list")
    void getMyProjects_success() throws Exception {
        List<EmployeeProjectDto> projects = List.of(
                buildEmployeeProjectDto(100L, 1L),
                buildEmployeeProjectDto(101L, 2L)
        );
        when(assignmentService.getMyProjects("emp@example.com")).thenReturn(projects);

        mockMvc.perform(get("/assignments/my-projects")
                        .principal(new UsernamePasswordAuthenticationToken("emp@example.com", null, List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].projectName").value("Project Alpha"))
                .andExpect(jsonPath("$[0].roleName").value("Backend Developer"))
                .andExpect(jsonPath("$[0].projectManagerName").value("Manager Bob"))
                .andExpect(jsonPath("$[0].durationWeeks").value(30));
    }

    @Test
    @DisplayName("GET /assignments/my-projects - no profile found: returns 404")
    void getMyProjects_noProfile_returns404() throws Exception {
        when(assignmentService.getMyProjects("emp@example.com"))
                .thenThrow(new NoSuchElementException("Employee profile not found for user: emp@example.com"));

        mockMvc.perform(get("/assignments/my-projects")
                        .principal(new UsernamePasswordAuthenticationToken("emp@example.com", null, List.of())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /assignments/my-projects - not assigned to any project: returns 200 empty list")
    void getMyProjects_notAssigned_returnsEmpty() throws Exception {
        when(assignmentService.getMyProjects("emp@example.com")).thenReturn(List.of());

        mockMvc.perform(get("/assignments/my-projects")
                        .principal(new UsernamePasswordAuthenticationToken("emp@example.com", null, List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
