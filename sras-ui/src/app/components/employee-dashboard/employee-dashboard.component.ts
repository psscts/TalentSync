import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDividerModule } from '@angular/material/divider';
import { ToastService } from '../../services/toast.service';
import { MatChipsModule } from '@angular/material/chips';
import { ProjectService } from '../../services/project.service';
import { EmployeeProjectDto } from '../../models/project.model';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
    selector: 'app-employee-dashboard',
    standalone: true,
    imports: [
        CommonModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatProgressBarModule,
        MatDividerModule,
        MatChipsModule
    ],
    templateUrl: './employee-dashboard.component.html',
    styleUrl: './employee-dashboard.component.scss'
})
export class EmployeeDashboardComponent implements OnInit {
    projects: EmployeeProjectDto[] = [];
    loading = false;

    constructor(
        private projectService: ProjectService,
        private toast: ToastService
    ) { }

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.loading = true;
        this.projectService.getMyProjects().subscribe({
            next: data => {
                this.projects = data;
                this.loading = false;
            },
            error: (err: HttpErrorResponse) => {
                this.loading = false;
                this.toast.error(this.resolveError(err));
            }
        });
    }

    private resolveError(err: HttpErrorResponse): string {
        const serverMsg: string | undefined =
            err.error?.message ?? err.error?.error ?? undefined;

        switch (err.status) {
            case 0:
                return 'Cannot reach the server. Please check that the backend is running on port 8081.';
            case 401:
                return 'Your session has expired or you are not logged in. Please log in again.';
            case 403:
                return 'You do not have permission to view project assignments.';
            case 404:
                return 'No assignment data found for your account (404). Your employee profile may not be linked to a user account yet.';
            case 500:
                return serverMsg
                    ? `Server error while loading assignments: ${serverMsg}`
                    : 'An internal server error occurred while loading your assignments. Please try again later.';
            default:
                return serverMsg
                    ? `Failed to load project assignments: ${serverMsg} (HTTP ${err.status})`
                    : `Failed to load project assignments (HTTP ${err.status}). Please try again.`;
        }
    }
}
