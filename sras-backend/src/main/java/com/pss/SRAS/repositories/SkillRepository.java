package com.pss.SRAS.repositories;

import com.pss.SRAS.models.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByEmployeeId(Long employeeId);
}
