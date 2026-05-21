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
            error: () => {
                this.loading = false;
                this.toast.error('Failed to load your project assignments');
            }
        });
    }
}
