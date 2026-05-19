package com.pss.SRAS.models;

import com.pss.SRAS.models.enums.AvailabilityStatus;
import com.pss.SRAS.models.enums.ExperienceLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String employeeId;

    @Column(nullable = false, length = 100)
    private String name;

    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    private ExperienceLevel experienceLevel;

    private Integer yearsOfExperience;

    @Column(length = 100)
    private String preferredLocation;

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availabilityStatus;

    private Double previousRatings;

    private Double expectedSalary;

    private Double employeeScore;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Certification> certifications = new ArrayList<>();
}
