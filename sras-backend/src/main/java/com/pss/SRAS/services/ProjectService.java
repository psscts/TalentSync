package com.pss.SRAS.services;

import com.pss.SRAS.models.Project;
import com.pss.SRAS.models.ProjectRequirement;
import com.pss.SRAS.repositories.ProjectRepository;
import com.pss.SRAS.repositories.ProjectRequirementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectRequirementRepository requirementRepository;

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project getById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Project not found: " + id));
    }

    @Transactional
    public Project create(Project project) {
        if (project.getProjectRequirements() != null) {
            project.getProjectRequirements().forEach(r -> r.setProject(project));
        }
        return projectRepository.save(project);
    }

    @Transactional
    public Project update(Long id, Project updated) {
        Project existing = getById(id);
        existing.setProjectName(updated.getProjectName());
        existing.setDomain(updated.getDomain());
        existing.setLocationPreferences(updated.getLocationPreferences());
        existing.setProjectManager(updated.getProjectManager());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());

        if (updated.getProjectRequirements() != null) {
            existing.getProjectRequirements().clear();
            updated.getProjectRequirements().forEach(r -> {
                r.setProject(existing);
                existing.getProjectRequirements().add(r);
            });
        }
        return projectRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new NoSuchElementException("Project not found: " + id);
        }
        projectRepository.deleteById(id);
    }

    // ─── Requirements ─────────────────────────────────────────────────────────

    public List<ProjectRequirement> getRequirements(Long projectId) {
        return requirementRepository.findByProjectId(projectId);
    }

    @Transactional
    public ProjectRequirement addRequirement(Long projectId, ProjectRequirement requirement) {
        Project project = getById(projectId);
        requirement.setProject(project);
        return requirementRepository.save(requirement);
    }

    @Transactional
    public ProjectRequirement updateRequirement(Long reqId, ProjectRequirement updated) {
        ProjectRequirement existing = requirementRepository.findById(reqId)
                .orElseThrow(() -> new NoSuchElementException("Requirement not found: " + reqId));
        existing.setLocation(updated.getLocation());
        existing.setNumberOfPositions(updated.getNumberOfPositions());
        existing.setRole(updated.getRole());
        return requirementRepository.save(existing);
    }

    @Transactional
    public void deleteRequirement(Long reqId) {
        if (!requirementRepository.existsById(reqId)) {
            throw new NoSuchElementException("Requirement not found: " + reqId);
        }
        requirementRepository.deleteById(reqId);
    }
}
