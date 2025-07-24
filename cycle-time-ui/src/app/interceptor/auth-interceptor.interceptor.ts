import {Inject, Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpErrorResponse} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {Router} from '@angular/router';
import {TokenService} from '../services/api-auth/services/token.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private tokenService: TokenService, private router: Router) {

  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let authReq = req;


    const token = this.tokenService.getToken();
    if (token) {
      authReq = req.clone({
        setHeaders: {
          Authorization: "Bearer" + " " + token,
        },
      });
    }

    return next.handle(authReq).pipe(
      catchError(error => this.handleAuthError(error))
    );
  }

  private handleAuthError(error: HttpErrorResponse): Observable<never> {
    if (error.status === 401) {
      // this.tokenService.clearToken();
      this.router.navigateByUrl('/login');
      return throwError(() => error);
    }
    return throwError(() => error);
  }
}
