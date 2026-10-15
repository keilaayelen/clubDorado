import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { finalize, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private http: HttpClient) {}

  login(username: string, password: string) {
    return this.http.post<{ token: string }>('/api/auth/login', { username, password }).pipe(
      tap(response => localStorage.setItem('gc_token', response.token))
    );
  }

  logout() {
    return this.http.post('/api/auth/logout', {}).pipe(
      finalize(() => localStorage.removeItem('gc_token'))
    );
  }

  isLoggedIn() {
    return !!localStorage.getItem('gc_token');
  }
}
