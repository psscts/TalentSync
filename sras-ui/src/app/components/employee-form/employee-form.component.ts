import { Component, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormArray, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { EmployeeService } from '../../services/employee.service';
import { AuthService } from '../../services/auth.service';
import { Employee } from '../../models/employee.model';

@Component({
  selector: 'app-employee-form',
  standalone: true,
  imports: [
    DecimalPipe,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatIconModule,
    MatSnackBarModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  templateUrl: './employee-form.component.html',
  styleUrl: './employee-form.component.scss'
})
export class EmployeeFormComponent implements OnInit {
  existingEmployee: Employee | null = null;
  loading = false;
  saving = false;
  viewMode = false;  // true = read-only view; false = edit/create form

  form = this.fb.group({
    employeeId: ['', Validators.required],
    name: ['', Validators.required],
    joiningDate: ['', Validators.required],
    experienceLevel: ['JUNIOR', Validators.required],
    yearsOfExperience: [0, [Validators.required, Validators.min(0)]],
    preferredLocation: ['', Validators.required],
    availabilityStatus: ['AVAILABLE', Validators.required],
    previousRatings: [1, Validators.required],
    expectedSalary: [0, [Validators.required, Validators.min(0)]],
    skills: this.fb.array([]),
    certifications: this.fb.array([])
  });

  constructor(
    private fb: FormBuilder,
    private employeeService: EmployeeService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const userId = this.authService.currentUser()?.userId;
    if (!userId) return;
    this.loading = true;
    // Try to load an existing profile linked to this user
    this.employeeService.getAll().subscribe({
      next: employees => {
        this.loading = false;
        // Match on the employee whose user.id equals the logged-in userId
        const mine = employees.find((e: any) => e.user?.id === userId);
        if (mine) {
          this.existingEmployee = mine;
          this.patchForm(mine);
          this.viewMode = true;  // existing profile → show in view mode first
        }
      },
      error: () => { this.loading = false; }
    });
  }

  get skills() { return this.form.get('skills') as FormArray; }
  get certifications() { return this.form.get('certifications') as FormArray; }

  startEdit(): void { this.viewMode = false; }
  cancelEdit(): void { this.viewMode = true; }

  addSkill(): void {
    this.skills.push(this.fb.group({ name: ['', Validators.required], proficiencyLevel: ['INTERMEDIATE'] }));
  }
  removeSkill(i: number): void { this.skills.removeAt(i); }

  addCert(): void {
    this.certifications.push(this.fb.group({
      certificateId: [''], name: ['', Validators.required],
      issuingOrganization: [''], score: [null]
    }));
  }
  removeCert(i: number): void { this.certifications.removeAt(i); }

  patchForm(emp: Employee): void {
    this.skills.clear();
    this.certifications.clear();
    emp.skills?.forEach(() => this.addSkill());
    emp.certifications?.forEach(() => this.addCert());
    this.form.patchValue(emp as any);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;

    // Attach the userId so backend can link Employee ↔ User
    const payload: any = {
      ...this.form.value,
      user: { id: this.authService.currentUser()?.userId }
    };

    const op = this.existingEmployee
      ? this.employeeService.update(this.existingEmployee.id!, payload)
      : this.employeeService.create(payload);

    op.subscribe({
      next: saved => {
        this.saving = false;
        this.existingEmployee = saved;
        this.patchForm(saved);
        this.viewMode = true;  // go back to view mode after saving
        this.snackBar.open(
          'Profile saved!',
          'Close', { duration: 2500 }
        );
      },
      error: err => {
        this.saving = false;
        this.snackBar.open(err.error?.message ?? 'Error saving profile', 'Close', { duration: 3000 });
      }
    });
  }
}
