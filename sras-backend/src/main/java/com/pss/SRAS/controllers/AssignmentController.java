package com.pss.SRAS.controllers;

import com.pss.SRAS.dto.AssignmentRequest;
import com.pss.SRAS.dto.AssignmentResponseDto;
import com.pss.SRAS.dto.EmployeeProjectDto;
import com.pss.SRAS.dto.ProjectDashboardDto;
import com.pss.SRAS.services.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    /** POST /assignments — assign an employee to a project */
    @PostMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<AssignmentResponseDto> assign(@RequestBody AssignmentRequest request, Authentication authentication) {
        return ResponseEntity.ok(assignmentService.assignEmployee(request.getProjectId(), request.getEmployeeId(), authentication.getName()));
    }

    /** DELETE /assignments/{id} — unassign, restores employee to AVAILABLE */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<Void> unassign(@PathVariable Long id) {
        assignmentService.unassignEmployee(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /assignments/project/{projectId} — all assignments for a project */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<List<AssignmentResponseDto>> getByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByProject(projectId));
    }

    /** GET /assignments/dashboard — manager dashboard */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<List<ProjectDashboardDto>> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(assignmentService.getDashboard(authentication.getName()));
    }

    /** GET /assignments/my-projects — employee's own assigned projects */
    @GetMapping("/my-projects")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EmployeeProjectDto>> getMyProjects(Authentication authentication) {
        return ResponseEntity.ok(assignmentService.getMyProjects(authentication.getName()));
    }
}

