import {Injectable} from '@angular/core';
import  {jwtDecode} from 'jwt-decode';
import {JwtHelperService} from "@auth0/angular-jwt";

@Injectable({
  providedIn: 'root',
})
export class TokenService {
  private jwtHelper = new JwtHelperService();
  private readonly tokenKey = 'jwt';


  isAuthenticated(): boolean {
    const token = this.getToken();
    return !!token && !this.isTokenExpired(token);

  }


  setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  isTokenExpired(token: string): boolean {
    const decodedToken = jwtDecode(token);
    const expirationDate = new Date(0);
    expirationDate.setUTCSeconds(decodedToken.exp as number);
   return expirationDate < new Date()
  }

  clearToken(): void {
    localStorage.removeItem(this.tokenKey);
  }

  getToken(): string {
    const token = localStorage.getItem(this.tokenKey)
    if (token == null || this.isTokenExpired(token!) ) {
      return '';
    }
    return token
  }

  hasRole(role: string): boolean {
    const roles = this.getUserRoles();
    return roles.includes(role);
  }


  getUserRoles(): string[] {
    const token = this.getToken();
    if (!token) return [];

    const decodedToken = this.jwtHelper.decodeToken(token);
    const authorities = decodedToken.roles[0]?.['role'];

    if (Array.isArray(authorities)) {
      return authorities;
    }

    if (typeof authorities === 'string') {
      return authorities.split(',');
    }

    return [];
  }


}
