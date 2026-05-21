import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Employee } from '../../models/employee.model';
import { EmployeeService } from '../../services/employee.service';
import { AuthService } from '../../services/auth.service';

type RankedEmployee = Employee & { rank: number };

@Component({
  selector: 'app-ranking-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatTooltipModule,
    MatProgressBarModule
  ],
  templateUrl: './ranking-dashboard.component.html',
  styleUrl: './ranking-dashboard.component.scss'
})
export class RankingDashboardComponent implements OnInit, AfterViewInit {
  displayedColumns = ['rank', 'name', 'experienceLevel', 'yearsOfExperience',
                      'availabilityStatus', 'preferredLocation', 'previousRatings', 'employeeScore'];
  dataSource = new MatTableDataSource<RankedEmployee>();
  isEmployee = false;
  profileIncomplete = false;
  myRank: number | null = null;
  myProfile: RankedEmployee | null = null;
  topThree: RankedEmployee[] = [];
  totalCount = 0;

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private employeeService: EmployeeService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const user = this.authService.currentUser();
    this.isEmployee = !!user && !this.authService.isManager();

    this.employeeService.getAll().subscribe(employees => {
      const ranked: RankedEmployee[] = employees
        .sort((a, b) => (b.employeeScore ?? 0) - (a.employeeScore ?? 0))
        .map((emp, idx) => ({ ...emp, rank: idx + 1 }));
      this.dataSource.data = ranked;
      this.topThree = ranked.slice(0, 3);
      this.totalCount = ranked.length;

      if (this.isEmployee && user) {
        const mine = ranked.find((e: any) => e.user?.id === user.userId);
        this.profileIncomplete = !mine || !mine.experienceLevel || mine.yearsOfExperience == null;
        if (mine) {
          this.myRank = mine.rank;
          this.myProfile = mine;
        }
      }
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
    if (score >= 70) return 'score-high';
    if (score >= 40) return 'score-mid';
    return 'score-low';
  }

  levelClass(level: string | undefined): string {
    return (level ?? '').toLowerCase();
  }

  availClass(status: string | undefined): string {
    switch (status) {
      case 'AVAILABLE': return 'avail';
      case 'PARTIALLY_AVAILABLE': return 'partial';
      default: return 'unavail';
    }
  }

  initials(name: string): string {
    return name.split(' ').map(p => p[0]).join('').substring(0, 2).toUpperCase();
  }

  medalIcon(rank: number): string {
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    return '🥉';
  }
}