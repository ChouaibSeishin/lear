import {Component, OnInit} from '@angular/core';
import {UserDto} from "../../services/api-auth/models/user-dto";
import {UserControllerService} from "../../services/api-auth/services/user-controller.service";
import {AuthenticationService} from "../../services/api-auth/services/authentication.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-profile',
  standalone: false,
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})

export class ProfileComponent implements OnInit {
  user: UserDto = {};
  passwordData = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  };
  errorMessage = '';
  successMessage = '';

  constructor(
    private userService: UserControllerService,
    private authService:AuthenticationService,
    private modalService: NgbModal
  ) {}

  ngOnInit(): void {

      this.authService.loadUser().subscribe({
        next: (data) => (this.user = data),
        error: (err) => console.error('Error fetching user:', err),
      });
  }

  openPasswordModal(content: any): void {
    this.passwordData = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    };
    this.errorMessage = '';
    this.successMessage = '';
    this.modalService.open(content);
  }

  updatePassword(modalRef: any): void {
    const { currentPassword, newPassword, confirmPassword } = this.passwordData;
    if (newPassword !== confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    const payload = { currentPassword, newPassword };

    this.userService.updatePassword(payload).subscribe({
      next: () => {

        this.successMessage = 'Password updated successfully.';
        setTimeout(() => {
          modalRef.close();
        }, 3000);

      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error updating password.';
      },
    });
  }
}

