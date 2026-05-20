import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormArray, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { ProjectService } from '../../services/project.service';
import { Project, ProjectRequirement, Role } from '../../models/project.model';

@Component({
  selector: 'app-project-requirement',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatIconModule,
    MatTableModule,
    MatExpansionModule,
    MatChipsModule,
    MatSnackBarModule,
    MatDividerModule
  ],
  templateUrl: './project-requirement.component.html',
  styleUrl: './project-requirement.component.scss'
})
export class ProjectRequirementComponent implements OnInit {
  projects: Project[] = [];
  editingId: number | null = null;
  loading = false;
  showProjectForm = false;

  form = this.fb.group({
    projectName: ['', Validators.required],
    domain: ['', Validators.required],
    locationPreferences: this.fb.array([]),
    startDate: [''],
    endDate: ['']
  });

  reqForm = this.fb.group({
    projectId: [null as number | null, Validators.required],
    location: ['', Validators.required],
    numberOfPositions: [1, [Validators.required, Validators.min(1)]],
    role: this.fb.group({
      name: ['', Validators.required],
      experienceLevel: ['JUNIOR', Validators.required],
      yearsOfExperience: [0, [Validators.required, Validators.min(0)]],
      expectedSalary: [0, [Validators.required, Validators.min(0)]],
      workMode: ['REMOTE', Validators.required],
      requiredSkills: this.fb.array([]),
      certificationsNeeded: this.fb.array([])
    })
  });

  projectColumns = ['projectName', 'domain', 'locations', 'actions'];

  constructor(
    private fb: FormBuilder,
    private projectService: ProjectService,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void { this.loadProjects(); }

  toggleProjectForm(): void {
    this.showProjectForm = !this.showProjectForm;
    if (!this.showProjectForm) this.resetProjectForm();
  }

  get locations() { return this.form.get('locationPreferences') as FormArray; }
  get reqSkills() { return this.reqForm.get('role.requiredSkills') as FormArray; }
  get reqCerts() { return this.reqForm.get('role.certificationsNeeded') as FormArray; }

  get selectedProjectLocations(): string[] {
    const projectId = this.reqForm.get('projectId')?.value;
    if (!projectId) return [];
    const project = this.projects.find(p => p.id === projectId);
    return project?.locationPreferences ?? [];
  }

  loadProjects(): void {
    this.projectService.getAll().subscribe(data => this.projects = data);
  }

  addLocation(): void { this.locations.push(this.fb.control('')); }
  removeLocation(i: number): void { this.locations.removeAt(i); }
  addReqSkill(): void { this.reqSkills.push(this.fb.control('')); }
  removeReqSkill(i: number): void { this.reqSkills.removeAt(i); }
  addReqCert(): void { this.reqCerts.push(this.fb.control('')); }
  removeReqCert(i: number): void { this.reqCerts.removeAt(i); }

  submitProject(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    const payload: Project = this.form.value as any;

    const op = this.editingId
      ? this.projectService.update(this.editingId, payload)
      : this.projectService.create(payload);

    op.subscribe({
      next: () => {
        this.loading = false;
        this.showProjectForm = false;
        this.snackBar.open(this.editingId ? 'Project updated' : 'Project created', 'Close', { duration: 2500 });
        this.resetProjectForm();
        this.loadProjects();
      },
      error: err => {
        this.loading = false;
        this.snackBar.open(err.error?.message ?? 'Error', 'Close', { duration: 3000 });
      }
    });
  }

  submitRequirement(): void {
    if (this.reqForm.invalid) {
      this.reqForm.markAllAsTouched();
      return;
    }
    const { projectId, location, numberOfPositions, role } = this.reqForm.value;
    const req: ProjectRequirement = { location: location ?? undefined, numberOfPositions: numberOfPositions ?? 1, role: role as Role };

    this.projectService.addRequirement(projectId!, req).subscribe({
      next: () => {
        this.snackBar.open('Requirement added', 'Close', { duration: 2500 });
        this.reqForm.reset({ numberOfPositions: 1 });
        this.reqSkills.clear();
        this.reqCerts.clear();
        this.loadProjects();
      },
      error: err => this.snackBar.open(err.error?.message ?? 'Error', 'Close', { duration: 3000 })
    });
  }

  editProject(p: Project): void {
    this.editingId = p.id!;
    this.showProjectForm = true;
    this.locations.clear();
    p.locationPreferences?.forEach(() => this.addLocation());
    this.form.patchValue({
      projectName: p.projectName,
      domain: p.domain ?? '',
      startDate: p.startDate ?? '',
      endDate: p.endDate ?? ''
    });
    p.locationPreferences?.forEach((loc, i) => {
      (this.locations.at(i) as any).setValue(loc);
    });
  }

  deleteProject(id: number): void {
    if (!confirm('Delete project?')) return;
    this.projectService.delete(id).subscribe({
      next: () => { this.snackBar.open('Deleted', 'Close', { duration: 2000 }); this.loadProjects(); }
    });
  }

  deleteRequirement(reqId: number): void {
    this.projectService.deleteRequirement(reqId).subscribe({
      next: () => { this.snackBar.open('Requirement removed', 'Close', { duration: 2000 }); this.loadProjects(); }
    });
  }

  resetProjectForm(): void {
    this.editingId = null;
    this.locations.clear();
    this.form.reset();
  }
}
