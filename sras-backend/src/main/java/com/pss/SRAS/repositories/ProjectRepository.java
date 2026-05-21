package com.pss.SRAS.repositories;

import com.pss.SRAS.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByProjectManagerId(Long managerId);

    @Query("SELECT p FROM Project p LEFT JOIN p.projectManager m WHERE m IS NULL OR m.id = :managerId")
    List<Project> findByManagerOrUnowned(@Param("managerId") Long managerId);
}
