import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FarmDistance, Movement } from '../models/movement.model';

const API_URL = 'http://localhost:8080/api/movements';

@Injectable({ providedIn: 'root' })
export class MovementService {
  private http = inject(HttpClient);

  list(): Observable<Movement[]> {
    return this.http.get<Movement[]>(API_URL);
  }

  traversal(farmId: number, hops: number, species?: string): Observable<FarmDistance[]> {
    const params: Record<string, string | number> = { farmId, hops };
    if (species) {
      params['species'] = species;
    }
    return this.http.get<FarmDistance[]>(`${API_URL}/traversal`, { params });
  }
}
