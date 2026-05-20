import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProjectService } from '../../services/project.service';
import { ProjectDashboard, ProjectAssignment } from '../../models/project.model';

@Component({
  selector: 'app-manager-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatChipsModule,
    MatExpansionModule,
    MatBadgeModule,
    MatProgressBarModule,
    MatSnackBarModule,
    MatTooltipModule
  ],
  templateUrl: './manager-dashboard.component.html',
  styleUrl: './manager-dashboard.component.scss'
})
export class ManagerDashboardComponent implements OnInit {
  projects: ProjectDashboard[] = [];
  loading = false;

  empColumns = ['employeeDbId', 'employeeName', 'experienceLevel', 'availabilityStatus', 'assignedAt', 'actions'];

  constructor(
    private projectService: ProjectService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.projectService.getDashboard().subscribe({
      next: data => {
        this.projects = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load dashboard', 'Close', { duration: 3000 });
      }
    });
  }

  unassign(assignment: ProjectAssignment): void {
    this.projectService.unassignEmployee(assignment.id).subscribe({
      next: () => {
        this.snackBar.open(`${assignment.employeeName} unassigned successfully`, 'Close', { duration: 3000 });
        this.load();
      },
      error: err => {
        this.snackBar.open(err.error?.message ?? 'Unassign failed', 'Close', { duration: 3000 });
      }
    });
  }

  filledPercent(project: ProjectDashboard): number {
    if (!project.totalPositions) return 0;
    return Math.min(100, Math.round((project.assignedEmployees.length / project.totalPositions) * 100));
  }
}
