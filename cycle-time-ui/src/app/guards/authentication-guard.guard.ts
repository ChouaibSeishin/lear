import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {TokenService} from '../services/api-auth/services/token.service';

export const authenticationGuard: CanActivateFn = (route, state) => {
  const tokenService = inject(TokenService);
  const router = inject(Router);

  const token = tokenService.getToken();

  if (token && !tokenService.isTokenExpired(token)) {
    return true;
  } else {
    tokenService.clearToken();
    router.navigateByUrl('/login');
    return false;
  }
};
