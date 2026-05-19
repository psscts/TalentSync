import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, LoginRequest, SignupRequest, ForgotPasswordRequest, ResetPasswordRequest } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  currentUser = signal<AuthResponse | null>(this.loadFromStorage());

  constructor(private http: HttpClient, private router: Router) {}

  signup(request: SignupRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/signup`, request).pipe(
      tap(response => this.saveSession(response))
    );
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => this.saveSession(response))
    );
  }

  forgotPassword(request: ForgotPasswordRequest): Observable<{ message: string; token: string }> {
    return this.http.post<{ message: string; token: string }>(`${this.apiUrl}/forgot-password`, request);
  }

  resetPassword(request: ResetPasswordRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/reset-password`, request);
  }

  logout(): void {
    localStorage.removeItem('sras_token');
    localStorage.removeItem('sras_user');
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('sras_token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isManager(): boolean {
    return this.currentUser()?.role === 'PROJECT_MANAGER';
  }

  private saveSession(response: AuthResponse): void {
    localStorage.setItem('sras_token', response.token);
    localStorage.setItem('sras_user', JSON.stringify(response));
    this.currentUser.set(response);
  }

  private loadFromStorage(): AuthResponse | null {
    const raw = localStorage.getItem('sras_user');
    return raw ? JSON.parse(raw) : null;
  }
}
