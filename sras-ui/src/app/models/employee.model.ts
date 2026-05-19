export type ProficiencyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
export type ExperienceLevel = 'JUNIOR' | 'MID' | 'SENIOR' | 'LEAD';
export type AvailabilityStatus = 'AVAILABLE' | 'PARTIALLY_AVAILABLE' | 'UNAVAILABLE';

export interface Skill {
  id?: number;
  name: string;
  proficiencyLevel: ProficiencyLevel;
}

export interface Certification {
  id?: number;
  certificateId?: string;
  name: string;
  issuingOrganization?: string;
  score?: number;
}

export interface Employee {
  id?: number;
  employeeId: string;
  name: string;
  joiningDate?: string;
  experienceLevel?: ExperienceLevel;
  yearsOfExperience?: number;
  preferredLocation?: string;
  availabilityStatus?: AvailabilityStatus;
  previousRatings?: number;
  expectedSalary?: number;
  employeeScore?: number;
  skills?: Skill[];
  certifications?: Certification[];
}
