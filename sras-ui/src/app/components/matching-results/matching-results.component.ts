import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { forkJoin } from 'rxjs';
import { ToastService } from '../../services/toast.service';
import { MatchingService } from '../../services/matching.service';
import { ProjectService } from '../../services/project.service';
import { MatchingResult } from '../../models/matching.model';
import { Project } from '../../models/project.model';

type MatchingRow = MatchingResult & { rank: number; assigned: boolean };

@Component({
  selector: 'app-matching-results',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    MatTooltipModule
  ],
  templateUrl: './matching-results.component.html',
  styleUrl: './matching-results.component.scss'
})
export class MatchingResultsComponent implements OnInit, AfterViewInit {
  projects: Project[] = [];
  dataSource = new MatTableDataSource<MatchingRow>();
  loading = false;
  searched = false;
  totalPositions = 0;
  assignedCount = 0;

  get projectFull(): boolean {
    return this.totalPositions > 0 && this.assignedCount >= this.totalPositions;
  }

  displayedColumns = ['rank', 'employeeDbId', 'name', 'experienceLevel',
    'yearsOfExperience', 'availabilityStatus', 'preferredLocation',
    'employeeScore', 'matchingScore', 'actions'];

  form = this.fb.group({
    projectId: [null as number | null, Validators.required],
    k: [10, [Validators.required, Validators.min(1), Validators.max(100)]]
  });

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private fb: FormBuilder,
    private matchingService: MatchingService,
    private projectService: ProjectService,
    private toast: ToastService
  ) { }

  ngOnInit(): void {
    this.projectService.getAll().subscribe(p => this.projects = p);
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  search(): void {
    if (this.form.invalid) return;
    const { projectId, k } = this.form.value;
    this.loading = true;
    this.searched = false;

    forkJoin({
      matches: this.matchingService.getTopK(projectId!, k!),
      requirements: this.projectService.getRequirements(projectId!),
      assignments: this.projectService.getAssignmentsByProject(projectId!)
    }).subscribe({
      next: ({ matches, requirements, assignments }) => {
        this.loading = false;
        this.searched = true;
        this.totalPositions = requirements.reduce((s, r) => s + (r.numberOfPositions ?? 0), 0);
        this.assignedCount = assignments.length;
        const assignedIds = new Set(assignments.map((a: any) => a.employeeDbId));
        this.dataSource.data = matches.map((r, i) => ({
          ...r, rank: i + 1, assigned: assignedIds.has(r.employeeDbId)
        }));
      },
      error: err => {
        this.loading = false;
        this.toast.error(err.error?.message ?? 'Matching failed');
      }
    });
  }

  assignEmployee(row: MatchingRow): void {
    const projectId = this.form.get('projectId')?.value;
    if (!projectId) return;
    this.projectService.assignEmployee(projectId, row.employeeId).subscribe({
      next: () => {
        row.assigned = true;
        row.availabilityStatus = 'UNAVAILABLE';
        this.assignedCount++;
        this.toast.success(`${row.name} assigned successfully`);
      },
      error: err => {
        this.toast.error(err.error?.message ?? 'Assignment failed');
      }
    });
  }

  scoreClass(score: number): string {
    if (score >= 70) return 'high';
    if (score >= 40) return 'medium';
    return 'low';
  }

  selectedProjectName(): string {
    const id = this.form.get('projectId')?.value;
    return this.projects.find(p => p.id === id)?.projectName ?? '';
  }
}
