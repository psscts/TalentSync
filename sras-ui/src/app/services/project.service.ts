import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Project, ProjectRequirement } from '../models/project.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly apiUrl = `${environment.apiUrl}/projects`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Project[]> {
    return this.http.get<Project[]>(this.apiUrl);
  }

  getById(id: number): Observable<Project> {
    return this.http.get<Project>(`${this.apiUrl}/${id}`);
  }

  create(project: Project): Observable<Project> {
    return this.http.post<Project>(this.apiUrl, project);
  }

  update(id: number, project: Project): Observable<Project> {
    return this.http.put<Project>(`${this.apiUrl}/${id}`, project);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getRequirements(projectId: number): Observable<ProjectRequirement[]> {
    return this.http.get<ProjectRequirement[]>(`${this.apiUrl}/${projectId}/requirements`);
  }

  addRequirement(projectId: number, req: ProjectRequirement): Observable<ProjectRequirement> {
    return this.http.post<ProjectRequirement>(`${this.apiUrl}/${projectId}/requirements`, req);
  }

  updateRequirement(reqId: number, req: ProjectRequirement): Observable<ProjectRequirement> {
    return this.http.put<ProjectRequirement>(`${this.apiUrl}/requirements/${reqId}`, req);
  }

  deleteRequirement(reqId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/requirements/${reqId}`);
  }
}
