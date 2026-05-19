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
}
