import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('gc_token');
  const router = inject(Router);
  const outgoing = !token || req.url.includes('/api/auth/login')
    ? req
    : req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  return next(outgoing).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !req.url.includes('/api/auth/login')) {
        localStorage.removeItem('gc_token');
        sessionStorage.setItem('gc_session_ended', '1');
        router.navigateByUrl('/entrar');
      }
      return throwError(() => err);
    })
  );
};
