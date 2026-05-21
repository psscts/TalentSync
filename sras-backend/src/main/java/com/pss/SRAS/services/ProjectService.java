package com.pss.SRAS.services;

import com.pss.SRAS.models.Project;
import com.pss.SRAS.models.ProjectRequirement;
import com.pss.SRAS.repositories.ProjectRepository;
import com.pss.SRAS.repositories.ProjectRequirementRepository;
import com.pss.SRAS.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectRequirementRepository requirementRepository;
    private final UserRepository userRepository;

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> getProjectsForManager(String managerEmail) {
        var user = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + managerEmail));
        return projectRepository.findByManagerOrUnowned(user.getId());
    }

    public Project getById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Project not found: " + id));
    }

    public Project getByIdForManager(Long id, String managerEmail) {
        Project project = getById(id);
        var user = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + managerEmail));
        // Allow access if unowned (null manager) or owned by this manager
        if (project.getProjectManager() != null && !project.getProjectManager().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You do not own this project");
        }
        return project;
    }

    @Transactional
    public Project create(Project project, String managerEmail) {
        var user = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + managerEmail));
        project.setProjectManager(user);
        if (project.getProjectRequirements() != null) {
            project.getProjectRequirements().forEach(r -> r.setProject(project));
        }
        return projectRepository.save(project);
    }

    @Transactional
    public Project update(Long id, Project updated, String managerEmail) {
        Project existing = getByIdForManager(id, managerEmail);
        existing.setProjectName(updated.getProjectName());
        existing.setDomain(updated.getDomain());
        existing.setLocationPreferences(updated.getLocationPreferences());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        userRepository.findByEmail(managerEmail).ifPresent(existing::setProjectManager);

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
    public void delete(Long id, String managerEmail) {
        getByIdForManager(id, managerEmail);
        projectRepository.deleteById(id);
    }

    public List<ProjectRequirement> getRequirements(Long projectId) {
        return requirementRepository.findByProjectId(projectId);
    }

    @Transactional
    public ProjectRequirement addRequirement(Long projectId, ProjectRequirement requirement, String managerEmail) {
        Project project = getByIdForManager(projectId, managerEmail);
        requirement.setProject(project);
        return requirementRepository.save(requirement);
    }

    @Transactional
    public ProjectRequirement updateRequirement(Long reqId, ProjectRequirement updated, String managerEmail) {
        ProjectRequirement existing = requirementRepository.findById(reqId)
                .orElseThrow(() -> new NoSuchElementException("Requirement not found: " + reqId));
        // verify ownership via the requirement's project
        getByIdForManager(existing.getProject().getId(), managerEmail);
        existing.setLocation(updated.getLocation());
        existing.setNumberOfPositions(updated.getNumberOfPositions());
        existing.setRole(updated.getRole());
        return requirementRepository.save(existing);
    }

    @Transactional
    public void deleteRequirement(Long reqId, String managerEmail) {
        ProjectRequirement existing = requirementRepository.findById(reqId)
                .orElseThrow(() -> new NoSuchElementException("Requirement not found: " + reqId));
        getByIdForManager(existing.getProject().getId(), managerEmail);
        requirementRepository.deleteById(reqId);
    }
}
