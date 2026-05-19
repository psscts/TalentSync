import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { MatchingResult } from '../models/matching.model';

@Injectable({ providedIn: 'root' })
export class MatchingService {
  private readonly apiUrl = `${environment.apiUrl}/matching`;

  constructor(private http: HttpClient) {}

  getTopK(projectId: number, k: number = 10): Observable<MatchingResult[]> {
    const params = new HttpParams().set('k', k.toString());
    return this.http.get<MatchingResult[]>(`${this.apiUrl}/${projectId}`, { params });
  }
}
