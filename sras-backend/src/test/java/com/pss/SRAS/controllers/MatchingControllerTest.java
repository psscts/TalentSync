package com.pss.SRAS.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pss.SRAS.dto.MatchingResultDto;
import com.pss.SRAS.exception.GlobalExceptionHandler;
import com.pss.SRAS.services.MatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMVC tests for MatchingController.
 * Uses standalone setup — MatchingService is mocked, no database required.
 * Security annotations (@PreAuthorize) are not enforced in standalone mode.
 */
@ExtendWith(MockitoExtension.class)
class MatchingControllerTest {

    @Mock
    private MatchingService matchingService;

    @InjectMocks
    private MatchingController controller;

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

    private MatchingResultDto buildResult(Long dbId, String empId, String name, double matchScore) {
        return new MatchingResultDto(dbId, empId, name, matchScore, 75.0, "AVAILABLE", "Bangalore", 4, "SENIOR");
    }

    // ── GET /matching/{projectId} ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /matching/{projectId} - default k=10: returns 200 with ranked list")
    void getTopK_defaultK() throws Exception {
        List<MatchingResultDto> results = List.of(
                buildResult(1L, "EMP001", "Alice", 92.0),
                buildResult(2L, "EMP002", "Bob", 78.5)
        );
        when(matchingService.getTopKForProject(1L, 10)).thenReturn(results);

        mockMvc.perform(get("/matching/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].employeeDbId").value("EMP001"))
                .andExpect(jsonPath("$[0].matchingScore").value(92.0))
                .andExpect(jsonPath("$[1].employeeDbId").value("EMP002"))
                .andExpect(jsonPath("$[1].matchingScore").value(78.5));
    }

    @Test
    @DisplayName("GET /matching/{projectId}?k=3 - returns top-3 results")
    void getTopK_customK() throws Exception {
        List<MatchingResultDto> top3 = List.of(
                buildResult(1L, "EMP001", "Alice", 95.0),
                buildResult(2L, "EMP002", "Bob", 85.0),
                buildResult(3L, "EMP003", "Carol", 75.0)
        );
        when(matchingService.getTopKForProject(2L, 3)).thenReturn(top3);

        mockMvc.perform(get("/matching/2").param("k", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].matchingScore").value(95.0));
    }

    @Test
    @DisplayName("GET /matching/{projectId} - no requirements defined: returns 404")
    void getTopK_noRequirements_returns404() throws Exception {
        when(matchingService.getTopKForProject(5L, 10))
                .thenThrow(new NoSuchElementException("No requirements found for project: 5"));

        mockMvc.perform(get("/matching/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No requirements found for project: 5"));
    }

    @Test
    @DisplayName("GET /matching/{projectId} - project does not exist: returns 404")
    void getTopK_projectNotFound() throws Exception {
        when(matchingService.getTopKForProject(99L, 10))
                .thenThrow(new NoSuchElementException("Project not found: 99"));

        mockMvc.perform(get("/matching/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /matching/{projectId} - no available employees: returns 200 empty list")
    void getTopK_noAvailableEmployees_returnsEmptyList() throws Exception {
        when(matchingService.getTopKForProject(1L, 10)).thenReturn(List.of());

        mockMvc.perform(get("/matching/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /matching/{projectId} - response fields are complete and correct")
    void getTopK_responseFieldsCorrect() throws Exception {
        MatchingResultDto result = new MatchingResultDto(
                10L, "EMP010", "Diana", 88.5, 80.0, "AVAILABLE", "Hyderabad", 6, "LEAD"
        );
        when(matchingService.getTopKForProject(3L, 10)).thenReturn(List.of(result));

        mockMvc.perform(get("/matching/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeId").value(10))
                .andExpect(jsonPath("$[0].employeeDbId").value("EMP010"))
                .andExpect(jsonPath("$[0].name").value("Diana"))
                .andExpect(jsonPath("$[0].matchingScore").value(88.5))
                .andExpect(jsonPath("$[0].employeeScore").value(80.0))
                .andExpect(jsonPath("$[0].availabilityStatus").value("AVAILABLE"))
                .andExpect(jsonPath("$[0].preferredLocation").value("Hyderabad"))
                .andExpect(jsonPath("$[0].yearsOfExperience").value(6))
                .andExpect(jsonPath("$[0].experienceLevel").value("LEAD"));
    }
}
