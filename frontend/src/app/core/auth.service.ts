import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface LoginResponse {
  token: string;
}

const TOKEN_KEY = 'acme_token';
const API = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${API}/auth/login`, { email, password })
      .pipe(tap((res) => localStorage.setItem(TOKEN_KEY, res.token)));
  }

  token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.token();
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
  }
}
