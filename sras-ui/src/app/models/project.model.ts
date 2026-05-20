export type WorkMode = 'REMOTE' | 'HYBRID' | 'ONSITE';
export type ExperienceLevel = 'JUNIOR' | 'MID' | 'SENIOR' | 'LEAD';

export interface Role {
  id?: number;
  name: string;
  experienceLevel?: ExperienceLevel;
  yearsOfExperience?: number;
  expectedSalary?: number;
  workMode?: WorkMode;
  requiredSkills?: string[];
  certificationsNeeded?: string[];
}

export interface ProjectRequirement {
  id?: number;
  location?: string;
  numberOfPositions?: number;
  role?: Role;
}

export interface Project {
  id?: number;
  projectName: string;
  domain?: string;
  locationPreferences?: string[];
  projectRequirements?: ProjectRequirement[];
  startDate?: string;
  endDate?: string;
}

export interface ProjectAssignment {
  id: number;
  projectId: number;
  projectName: string;
  employeeDbId: string;
  employeeName: string;
  experienceLevel?: string;
  availabilityStatus?: string;
  assignedAt: string;
}

export interface ProjectDashboard {
  projectId: number;
  projectName: string;
  domain?: string;
  startDate?: string;
  endDate?: string;
  totalPositions: number;
  assignedEmployees: ProjectAssignment[];
}

export interface EmployeeProjectDto {
  assignmentId: number;
  projectId: number;
  projectName: string;
  domain?: string;
  roleName?: string;
  projectManagerName?: string;
  startDate?: string;
  endDate?: string;
  durationWeeks?: number;
  assignedAt: string;
}
