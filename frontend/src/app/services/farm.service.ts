import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Farm, FarmRequest } from '../models/farm.model';

const API_URL = 'http://localhost:8080/api/farms';

@Injectable({ providedIn: 'root' })
export class FarmService {
  private http = inject(HttpClient);

  list(): Observable<Farm[]> {
    return this.http.get<Farm[]>(API_URL);
  }

  create(request: FarmRequest): Observable<Farm> {
    return this.http.post<Farm>(API_URL, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${id}`);
  }
}
