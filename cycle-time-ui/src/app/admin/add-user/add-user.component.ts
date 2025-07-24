import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import {AuthenticationService} from "../../services/api-auth/services/authentication.service";
import {RegistrationRequest} from "../../services/api-auth/models/registration-request";
import {Register$Params} from "../../services/api-auth/fn/authentication/register";
import {ToastrService} from "ngx-toastr";


@Component({
  selector: 'app-add-user',
  templateUrl: './add-user.component.html',
  styleUrl: 'add-user.component.scss',
  standalone:false,
})
export class AddUserComponent {
  addUserForm: FormGroup;
  isSubmitting = false;
  hide:boolean=true;
  roles: RegistrationRequest['roleName'][] = ['ADMIN', 'USER', 'AUDIT'];

  constructor(
    private fb: FormBuilder,
    private userService: AuthenticationService,
    private router: Router,
    private toastr: ToastrService
  ) {
    this.addUserForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      roleName:['',Validators.required],
      code:['',Validators.required]
    });
  }

  get f() {
    return this.addUserForm.controls;
  }

  onSubmit(): void {
    if (this.addUserForm.invalid) return;

    this.isSubmitting = true;
    const registrationRequest: RegistrationRequest = this.addUserForm.value;
    const registrationParams:Register$Params = {body:registrationRequest};

    this.userService.register(registrationParams).subscribe({
      next: () => {
        this.toastr.success('User added. They will receive an email with the activation code.', 'Success');
        this.router.navigate(['/users']);
      },
      error: (err) => {
        this.toastr.error('Failed to add user');

        console.error(err);
        this.isSubmitting = false;
      }
    });
  }
}
