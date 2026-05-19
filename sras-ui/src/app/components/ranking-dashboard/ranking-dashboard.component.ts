import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { Employee } from '../../models/employee.model';
import { EmployeeService } from '../../services/employee.service';

@Component({
  selector: 'app-ranking-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule
  ],
  templateUrl: './ranking-dashboard.component.html',
  styleUrl: './ranking-dashboard.component.scss'
})
export class RankingDashboardComponent implements OnInit, AfterViewInit {
  displayedColumns = ['rank', 'employeeId', 'name', 'experienceLevel', 'yearsOfExperience',
                      'availabilityStatus', 'preferredLocation', 'previousRatings', 'employeeScore'];
  dataSource = new MatTableDataSource<Employee & { rank: number }>();

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private employeeService: EmployeeService) {}

  ngOnInit(): void {
    this.employeeService.getAll().subscribe(employees => {
      const ranked = employees
        .sort((a, b) => (b.employeeScore ?? 0) - (a.employeeScore ?? 0))
        .map((emp, idx) => ({ ...emp, rank: idx + 1 }));
      this.dataSource.data = ranked;
    });
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  applyFilter(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.dataSource.filter = value.trim().toLowerCase();
  }

  filterByLevel(level: string): void {
    this.dataSource.filter = level.toLowerCase();
  }

  clearFilter(): void {
    this.dataSource.filter = '';
  }

  scoreClass(score: number): string {
    if (score >= 70) return 'high';
    if (score >= 40) return 'medium';
    return 'low';
  }
}
