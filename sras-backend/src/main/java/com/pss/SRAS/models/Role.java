package com.pss.SRAS.models;

import com.pss.SRAS.models.enums.ExperienceLevel;
import com.pss.SRAS.models.enums.WorkMode;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    private ExperienceLevel experienceLevel;

    private Integer yearsOfExperience;

    private Double expectedSalary;

    @Enumerated(EnumType.STRING)
    private WorkMode workMode;

    @ElementCollection
    @CollectionTable(name = "role_required_skills", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "skill_name")
    @Builder.Default
    private List<String> requiredSkills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "role_required_certifications", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "certification_name")
    @Builder.Default
    private List<String> certificationsNeeded = new ArrayList<>();
}
