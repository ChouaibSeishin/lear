import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { TokenService } from '../services/api-auth/services/token.service';

// Optional: role-based access map (cleaner)
const roleAccessMap: { [pathSegment: string]: string[] } = {
  'admin': ['ROLE_ADMIN'],
  'dashboard': ['ROLE_ADMIN', 'ROLE_USER', 'ROLE_AUDIT']
};

export const authorizationGuard: CanActivateFn = (route, state) => {
  const tokenService = inject(TokenService);
  const router = inject(Router);

  if (!tokenService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  const roles = tokenService.getUserRoles();

  // Get the first segment of the route (e.g. 'admin' from '/admin/settings')
  const firstSegment = state.url.split('/')[1];

  const allowedRoles = roleAccessMap[firstSegment];

  if (allowedRoles && roles.some(role => allowedRoles.includes(role))) {
    return true;
  }

  router.navigate(['/unauthorized']);
  return false;
};
