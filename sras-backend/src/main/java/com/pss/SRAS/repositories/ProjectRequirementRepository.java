package com.pss.SRAS.repositories;

import com.pss.SRAS.models.ProjectRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRequirementRepository extends JpaRepository<ProjectRequirement, Long> {
    List<ProjectRequirement> findByProjectId(Long projectId);
}
