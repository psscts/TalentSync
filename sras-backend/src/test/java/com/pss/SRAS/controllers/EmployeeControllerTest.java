package com.pss.SRAS.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pss.SRAS.exception.GlobalExceptionHandler;
import com.pss.SRAS.models.Employee;
import com.pss.SRAS.models.enums.AvailabilityStatus;
import com.pss.SRAS.models.enums.ExperienceLevel;
import com.pss.SRAS.services.EmployeeService;
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
 * MockMVC tests for EmployeeController.
 * Uses standalone setup — EmployeeService is mocked, no database required.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController controller;

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

    private Employee buildEmployee(Long id, String empId, String name) {
        Employee e = new Employee();
        e.setId(id);
        e.setEmployeeId(empId);
        e.setName(name);
        e.setExperienceLevel(ExperienceLevel.MID);
        e.setYearsOfExperience(3);
        e.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        e.setEmployeeScore(72.5);
        return e;
    }

    // ── GET /employees ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /employees - returns 200 with list of all employees")
    void getAll_returnsEmployeeList() throws Exception {
        List<Employee> employees = List.of(
                buildEmployee(1L, "EMP001", "Alice"),
                buildEmployee(2L, "EMP002", "Bob")
        );
        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].employeeId").value("EMP001"))
                .andExpect(jsonPath("$[1].employeeId").value("EMP002"));
    }

    @Test
    @DisplayName("GET /employees - returns 200 with empty list when no employees exist")
    void getAll_emptyList() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /employees/{id} ──────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /employees/{id} - found: returns 200 with employee data")
    void getById_found() throws Exception {
        Employee emp = buildEmployee(1L, "EMP001", "Alice");
        when(employeeService.getById(1L)).thenReturn(emp);

        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeId").value("EMP001"))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    @DisplayName("GET /employees/{id} - not found: returns 404")
    void getById_notFound() throws Exception {
        when(employeeService.getById(99L))
                .thenThrow(new NoSuchElementException("Employee not found: 99"));

        mockMvc.perform(get("/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found: 99"));
    }

    // ── GET /employees/eid/{employeeId} ──────────────────────────────────────────

    @Test
    @DisplayName("GET /employees/eid/{employeeId} - found: returns 200")
    void getByEmployeeId_found() throws Exception {
        Employee emp = buildEmployee(1L, "EMP001", "Alice");
        when(employeeService.getByEmployeeId("EMP001")).thenReturn(emp);

        mockMvc.perform(get("/employees/eid/EMP001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value("EMP001"));
    }

    @Test
    @DisplayName("GET /employees/eid/{employeeId} - not found: returns 404")
    void getByEmployeeId_notFound() throws Exception {
        when(employeeService.getByEmployeeId("UNKNOWN"))
                .thenThrow(new NoSuchElementException("Employee not found: UNKNOWN"));

        mockMvc.perform(get("/employees/eid/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    // ── POST /employees ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /employees - valid payload: returns 201 with created employee")
    void create_success() throws Exception {
        Employee req = buildEmployee(null, "EMP003", "Charlie");
        Employee saved = buildEmployee(3L, "EMP003", "Charlie");
        when(employeeService.create(any(Employee.class))).thenReturn(saved);

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/employees/3"))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.employeeId").value("EMP003"));
    }

    @Test
    @DisplayName("POST /employees - duplicate employeeId: returns 400")
    void create_duplicateId_returns400() throws Exception {
        Employee req = buildEmployee(null, "EMP001", "Duplicate");
        when(employeeService.create(any(Employee.class)))
                .thenThrow(new IllegalArgumentException("Employee ID already exists: EMP001"));

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Employee ID already exists: EMP001"));
    }

    // ── PUT /employees/{id} ───────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /employees/{id} - valid: returns 200 with updated employee")
    void update_success() throws Exception {
        Employee updated = buildEmployee(1L, "EMP001", "Alice Updated");
        when(employeeService.update(eq(1L), any(Employee.class))).thenReturn(updated);

        mockMvc.perform(put("/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Updated"));
    }

    @Test
    @DisplayName("PUT /employees/{id} - not found: returns 404")
    void update_notFound() throws Exception {
        Employee updated = buildEmployee(99L, "EMP099", "Ghost");
        when(employeeService.update(eq(99L), any(Employee.class)))
                .thenThrow(new NoSuchElementException("Employee not found: 99"));

        mockMvc.perform(put("/employees/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /employees/{id} ────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /employees/{id} - exists: returns 204 No Content")
    void delete_success() throws Exception {
        doNothing().when(employeeService).delete(1L);

        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());

        verify(employeeService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /employees/{id} - not found: returns 404")
    void delete_notFound() throws Exception {
        doThrow(new NoSuchElementException("Employee not found: 99"))
                .when(employeeService).delete(99L);

        mockMvc.perform(delete("/employees/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /employees/available ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /employees/available - returns 200 with only available employees")
    void getAvailable_returnsList() throws Exception {
        List<Employee> available = List.of(buildEmployee(1L, "EMP001", "Alice"));
        when(employeeService.getAvailableEmployees()).thenReturn(available);

        mockMvc.perform(get("/employees/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].availabilityStatus").value("AVAILABLE"));
    }

    @Test
    @DisplayName("GET /employees/available - no available employees: returns empty list")
    void getAvailable_empty() throws Exception {
        when(employeeService.getAvailableEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/employees/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
