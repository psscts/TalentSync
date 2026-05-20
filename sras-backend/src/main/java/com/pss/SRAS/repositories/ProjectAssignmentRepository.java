package com.pss.SRAS.repositories;

import com.pss.SRAS.models.ProjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Long> {
    List<ProjectAssignment> findByProjectId(Long projectId);
    List<ProjectAssignment> findByEmployeeId(Long employeeId);
    boolean existsByProjectIdAndEmployeeId(Long projectId, Long employeeId);
}
