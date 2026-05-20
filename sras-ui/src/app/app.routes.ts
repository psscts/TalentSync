import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { managerGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'signup',
    loadComponent: () => import('./components/signup/signup.component').then(m => m.SignupComponent)
  },
  {
    path: 'forgot-password',
    loadComponent: () => import('./components/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent)
  },
  {
    path: 'reset-password',
    loadComponent: () => import('./components/reset-password/reset-password.component').then(m => m.ResetPasswordComponent)
  },
  {
    path: 'employees',
    loadComponent: () => import('./components/employee-form/employee-form.component').then(m => m.EmployeeFormComponent),
    canActivate: [authGuard]
  },
  {
    path: 'my-dashboard',
    loadComponent: () => import('./components/employee-dashboard/employee-dashboard.component').then(m => m.EmployeeDashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'ranking',
    loadComponent: () => import('./components/ranking-dashboard/ranking-dashboard.component').then(m => m.RankingDashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'projects',
    loadComponent: () => import('./components/project-requirement/project-requirement.component').then(m => m.ProjectRequirementComponent),
    canActivate: [managerGuard]
  },
  {
    path: 'matching',
    loadComponent: () => import('./components/matching-results/matching-results.component').then(m => m.MatchingResultsComponent),
    canActivate: [managerGuard]
  },
  {
    path: 'manager-dashboard',
    loadComponent: () => import('./components/manager-dashboard/manager-dashboard.component').then(m => m.ManagerDashboardComponent),
    canActivate: [managerGuard]
  },
  { path: '**', redirectTo: '' }
];
