import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthResponse } from '../models/user.model';

const API_URL = 'http://localhost:8080/api/auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);

  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}/login`, { username, password }).pipe(
      tap((response) => {
        localStorage.setItem('token', response.token);
        localStorage.setItem('username', response.username);
        // strip "ROLE_" prefix so components compare against plain role names
        localStorage.setItem('role', response.role.replace('ROLE_', ''));
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getUsername(): string | null {
    return localStorage.getItem('username');
  }

  getRole(): string | null {
    return localStorage.getItem('role');
  }

  isLoggedIn(): boolean {
    return this.getToken() !== null;
  }
}
