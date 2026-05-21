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
import { MatTooltipModule } from '@angular/material/tooltip';
import { ToastService } from '../../services/toast.service';
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
        MatTooltipModule
    ],
    templateUrl: './manager-dashboard.component.html',
    styleUrl: './manager-dashboard.component.scss'
})
export class ManagerDashboardComponent implements OnInit {
    projects: ProjectDashboard[] = [];
    loading = false;

    empColumns = ['employeeDbId', 'employeeName', 'experienceLevel', 'assignmentStatus', 'assignedAt', 'actions'];

    constructor(
        private projectService: ProjectService,
        private toast: ToastService
    ) { }

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
                this.toast.error('Failed to load dashboard');
            }
        });
    }

    unassign(assignment: ProjectAssignment): void {
        this.projectService.unassignEmployee(assignment.id).subscribe({
            next: () => {
                this.toast.success(`${assignment.employeeName} unassigned successfully`);
                this.load();
            },
            error: err => {
                this.toast.error(err.error?.message ?? 'Unassign failed');
            }
        });
    }

    filledPercent(project: ProjectDashboard): number {
        const assigned = project.assignedEmployees?.length ?? 0;
        const total = project.totalPositions ?? 0;
        if (total <= 0) return assigned > 0 ? 100 : 0;
        return Math.min(100, Math.round((assigned / total) * 100));
    }

    staffingLabel(project: ProjectDashboard): string {
        const assigned = project.assignedEmployees?.length ?? 0;
        const total = project.totalPositions ?? 0;
        if (total <= 0) return `${assigned} assigned`;
        return `${assigned} / ${total} staffed`;
    }
}
