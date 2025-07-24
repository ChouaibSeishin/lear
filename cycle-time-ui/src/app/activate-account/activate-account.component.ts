import {Component} from '@angular/core';
import {Router} from "@angular/router";
import {AuthenticationService} from "../services/api-auth/services/authentication.service";

@Component({
  selector: 'app-activate-account',
  standalone:false,
  templateUrl: './activate-account.component.html',
  styleUrl: './activate-account.component.css'
})
export class ActivateAccountComponent {
  message: string = "";
  isOkay: boolean = true;
  submitted: boolean = false;

  constructor(private router: Router, private authService: AuthenticationService) {
  }

  onCodeCompleted(token: string) {
    this.confirmAccount(token);

  }

  redirectToLogin() {
    this.router.navigate(['login']);
  }

  private confirmAccount(token: string) {
    this.authService.confirm({token}).subscribe({
      next: () => {
        this.message = "your account has been successfully activated.\n You can login now!";
        this.submitted = true;
        this.isOkay = true;

      }, error: () => {
        this.message = "Token has been expired or invalid.";
        this.submitted = true;
        this.isOkay = false;
      }
    });

  }
}
