import {Component, OnInit} from '@angular/core';
import {UserDto} from "../../services/api-auth/models/user-dto";
import {UserControllerService} from "../../services/api-auth/services/user-controller.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {RegistrationRequest} from "../../services/api-auth/models/registration-request";
import {TokenService} from "../../services/api-auth/services/token.service";

@Component({
  selector: 'app-user-details',
  standalone: false,
  templateUrl: './user-details.component.html',
  styleUrl: './user-details.component.css'
})
export class UserDetailsComponent implements OnInit {
  user: UserDto = {};
  roles: RegistrationRequest['roleName'][] = ['ADMIN', 'USER', 'AUDIT'];


  tokenService: TokenService;

  constructor(
    private userService: UserControllerService,
    private route: ActivatedRoute,
    private router:Router,
    private toaster: ToastrService,
    private tokService:TokenService

  ) {
    this.tokenService= tokService;
  }

  ngOnInit(): void {
    const userEmail = this.route.snapshot.paramMap.get('email');

    if (userEmail) {
      this.getUserByEmail(userEmail);

    }

  }

  getUserByEmail(email: string): void {
    this.userService.getUserByEmail({email:email}).subscribe({
      next: (data) => {
        this.user = data
          console.log(this.user);

      },
      error: (err) => console.error('Error fetching user:', err),
    });
  }

  updateUser(): void {
    if(!this.tokService.hasRole('ROLE_ADMIN')){
      this.router.navigate(['/unauthorized']);
    }
    else {
      this.userService.updateUser({body:this.user} ).subscribe({
        next: () =>{
          this.toaster.success('User updated successfully')
        },

        error: (err) => {
          this.toaster.error('Error updating user:'+err);
          console.log(JSON.stringify(err));
        }
      });
    }

  }

  // goBack(): void {
  //   this.location.back();
  // }
}
