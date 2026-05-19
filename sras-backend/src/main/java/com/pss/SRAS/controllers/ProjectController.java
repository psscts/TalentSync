package com.pss.SRAS.controllers;

import com.pss.SRAS.models.Project;
import com.pss.SRAS.models.ProjectRequirement;
import com.pss.SRAS.services.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<Project>> getAll() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<Project> create(@RequestBody Project project) {
        Project saved = projectService.create(project);
        return ResponseEntity.created(URI.create("/projects/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<Project> update(@PathVariable Long id, @RequestBody Project project) {
        return ResponseEntity.ok(projectService.update(id, project));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
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
            @RequestBody ProjectRequirement requirement) {
        return ResponseEntity.ok(projectService.addRequirement(id, requirement));
    }

    @PutMapping("/requirements/{reqId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ProjectRequirement> updateRequirement(
            @PathVariable Long reqId,
            @RequestBody ProjectRequirement requirement) {
        return ResponseEntity.ok(projectService.updateRequirement(reqId, requirement));
    }

    @DeleteMapping("/requirements/{reqId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<Void> deleteRequirement(@PathVariable Long reqId) {
        projectService.deleteRequirement(reqId);
        return ResponseEntity.noContent().build();
    }
}
