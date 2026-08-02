import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BiosecurityPlan, PlanRequest } from '../models/plan.model';

const API_URL = 'http://localhost:8080/api/plans';

@Injectable({ providedIn: 'root' })
export class PlanService {
  private http = inject(HttpClient);

  list(): Observable<BiosecurityPlan[]> {
    return this.http.get<BiosecurityPlan[]>(API_URL);
  }

  create(request: PlanRequest): Observable<BiosecurityPlan> {
    return this.http.post<BiosecurityPlan>(API_URL, request);
  }

  update(id: number, request: PlanRequest): Observable<BiosecurityPlan> {
    return this.http.put<BiosecurityPlan>(`${API_URL}/${id}`, request);
  }

  submit(id: number): Observable<BiosecurityPlan> {
    return this.http.post<BiosecurityPlan>(`${API_URL}/${id}/submit`, null);
  }

  approve(id: number): Observable<BiosecurityPlan> {
    return this.http.post<BiosecurityPlan>(`${API_URL}/${id}/approve`, null);
  }

  reject(id: number): Observable<BiosecurityPlan> {
    return this.http.post<BiosecurityPlan>(`${API_URL}/${id}/reject`, null);
  }
}
