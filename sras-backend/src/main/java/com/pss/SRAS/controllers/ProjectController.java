package com.pss.SRAS.controllers;

import com.pss.SRAS.models.Project;
import com.pss.SRAS.models.ProjectRequirement;
import com.pss.SRAS.services.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<Project>> getAll(Authentication authentication) {
        // Managers see their own  projects; other roles see all
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROJECT_MANAGER"))) {
            return ResponseEntity.ok(projectService.getProjectsForManager(authentication.getName()));
        }
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<Project> create(@RequestBody Project project, Authentication authentication) {
        Project saved = projectService.create(project, authentication.getName());
        return ResponseEntity.created(URI.create("/projects/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<Project> update(@PathVariable Long id, @RequestBody Project project, Authentication authentication) {
        return ResponseEntity.ok(projectService.update(id, project, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        projectService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // ─── Requirements ─────────────────────────────────────────────────────────

    @GetMapping("/{id}/requirements")
    public ResponseEntity<List<ProjectRequirement>> getRequirements(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getRequirements(id));
    }

    @PostMapping("/{id}/requirements")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ProjectRequirement> addRequirement(
            @PathVariable Long id,
            @RequestBody ProjectRequirement requirement,
            Authentication authentication) {
        return ResponseEntity.ok(projectService.addRequirement(id, requirement, authentication.getName()));
    }

    @PutMapping("/requirements/{reqId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ProjectRequirement> updateRequirement(
            @PathVariable Long reqId,
            @RequestBody ProjectRequirement requirement,
            Authentication authentication) {
        return ResponseEntity.ok(projectService.updateRequirement(reqId, requirement, authentication.getName()));
    }

    @DeleteMapping("/requirements/{reqId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<Void> deleteRequirement(@PathVariable Long reqId, Authentication authentication) {
        projectService.deleteRequirement(reqId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
