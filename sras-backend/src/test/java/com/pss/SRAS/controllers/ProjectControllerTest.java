package com.pss.SRAS.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pss.SRAS.exception.GlobalExceptionHandler;
import com.pss.SRAS.models.Project;
import com.pss.SRAS.models.ProjectRequirement;
import com.pss.SRAS.models.Role;
import com.pss.SRAS.services.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMVC tests for ProjectController.
 * Uses standalone setup — ProjectService is mocked, no database required.
 * Security annotations (@PreAuthorize) are not enforced in standalone mode;
 * only controller logic and error handling are tested.
 */
@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController controller;

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

    // ── Helper ──────────────────────────────────────────────────────────────────

    private Project buildProject(Long id, String name) {
        Project p = new Project();
        p.setId(id);
        p.setProjectName(name);
        p.setDomain("Finance");
        p.setLocationPreferences(List.of("Bangalore", "Chennai"));
        return p;
    }

    private ProjectRequirement buildRequirement(Long id) {
        Role role = new Role();
        role.setId(1L);
        role.setName("Backend Developer");

        ProjectRequirement req = new ProjectRequirement();
        req.setId(id);
        req.setLocation("Bangalore");
        req.setNumberOfPositions(3);
        req.setRole(role);
        return req;
    }

    // ── GET /projects ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /projects - returns 200 with all projects")
    void getAll_returnsProjectList() throws Exception {
        when(projectService.getAllProjects()).thenReturn(
                List.of(buildProject(1L, "Alpha"), buildProject(2L, "Beta")));

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].projectName").value("Alpha"))
                .andExpect(jsonPath("$[1].projectName").value("Beta"));
    }

    @Test
    @DisplayName("GET /projects - empty: returns 200 with empty array")
    void getAll_empty() throws Exception {
        when(projectService.getAllProjects()).thenReturn(List.of());

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /projects/{id} ───────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /projects/{id} - found: returns 200 with project data")
    void getById_found() throws Exception {
        when(projectService.getById(1L)).thenReturn(buildProject(1L, "Alpha"));

        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.projectName").value("Alpha"))
                .andExpect(jsonPath("$.domain").value("Finance"));
    }

    @Test
    @DisplayName("GET /projects/{id} - not found: returns 404")
    void getById_notFound() throws Exception {
        when(projectService.getById(99L))
                .thenThrow(new NoSuchElementException("Project not found: 99"));

        mockMvc.perform(get("/projects/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project not found: 99"));
    }

    // ── POST /projects ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /projects - returns 201 with created project")
    void create_success() throws Exception {
        Project req = buildProject(null, "Gamma");
        Project saved = buildProject(3L, "Gamma");
        when(projectService.create(any(Project.class))).thenReturn(saved);

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/projects/3"))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.projectName").value("Gamma"));
    }

    // ── PUT /projects/{id} ────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /projects/{id} - returns 200 with updated project")
    void update_success() throws Exception {
        Project updated = buildProject(1L, "Alpha Revised");
        when(projectService.update(eq(1L), any(Project.class))).thenReturn(updated);

        mockMvc.perform(put("/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("Alpha Revised"));
    }

    @Test
    @DisplayName("PUT /projects/{id} - not found: returns 404")
    void update_notFound() throws Exception {
        when(projectService.update(eq(99L), any(Project.class)))
                .thenThrow(new NoSuchElementException("Project not found: 99"));

        mockMvc.perform(put("/projects/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildProject(99L, "Ghost"))))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /projects/{id} ─────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /projects/{id} - returns 204 No Content")
    void delete_success() throws Exception {
        doNothing().when(projectService).delete(1L);

        mockMvc.perform(delete("/projects/1"))
                .andExpect(status().isNoContent());

        verify(projectService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /projects/{id} - not found: returns 404")
    void delete_notFound() throws Exception {
        doThrow(new NoSuchElementException("Project not found: 99"))
                .when(projectService).delete(99L);

        mockMvc.perform(delete("/projects/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /projects/{id}/requirements ──────────────────────────────────────────

    @Test
    @DisplayName("GET /projects/{id}/requirements - returns 200 with requirement list")
    void getRequirements_returnsList() throws Exception {
        when(projectService.getRequirements(1L)).thenReturn(
                List.of(buildRequirement(10L), buildRequirement(11L)));

        mockMvc.perform(get("/projects/1/requirements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].role.name").value("Backend Developer"));
    }

    // ── POST /projects/{id}/requirements ─────────────────────────────────────────

    @Test
    @DisplayName("POST /projects/{id}/requirements - returns 200 with added requirement")
    void addRequirement_success() throws Exception {
        ProjectRequirement req = buildRequirement(null);
        ProjectRequirement saved = buildRequirement(10L);
        when(projectService.addRequirement(eq(1L), any(ProjectRequirement.class))).thenReturn(saved);

        mockMvc.perform(post("/projects/1/requirements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.numberOfPositions").value(3));
    }

    // ── PUT /projects/requirements/{reqId} ───────────────────────────────────────

    @Test
    @DisplayName("PUT /projects/requirements/{reqId} - returns 200 with updated requirement")
    void updateRequirement_success() throws Exception {
        ProjectRequirement updated = buildRequirement(10L);
        updated.setNumberOfPositions(5);
        when(projectService.updateRequirement(eq(10L), any(ProjectRequirement.class))).thenReturn(updated);

        mockMvc.perform(put("/projects/requirements/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfPositions").value(5));
    }

    // ── DELETE /projects/requirements/{reqId} ─────────────────────────────────────

    @Test
    @DisplayName("DELETE /projects/requirements/{reqId} - returns 204 No Content")
    void deleteRequirement_success() throws Exception {
        doNothing().when(projectService).deleteRequirement(10L);

        mockMvc.perform(delete("/projects/requirements/10"))
                .andExpect(status().isNoContent());

        verify(projectService).deleteRequirement(10L);
    }

    @Test
    @DisplayName("DELETE /projects/requirements/{reqId} - not found: returns 404")
    void deleteRequirement_notFound() throws Exception {
        doThrow(new NoSuchElementException("Requirement not found: 99"))
                .when(projectService).deleteRequirement(99L);

        mockMvc.perform(delete("/projects/requirements/99"))
                .andExpect(status().isNotFound());
    }
}
