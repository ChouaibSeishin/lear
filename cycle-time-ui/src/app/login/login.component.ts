import {Component, OnInit} from '@angular/core';
import {AuthenticationRequest} from "../services/api-auth/models/authentication-request";
import {Authenticate$Params} from "../services/api-auth/fn/authentication/authenticate";
import {FormBuilder, FormControl, FormGroup} from "@angular/forms";
import {AuthenticationService} from "../services/api-auth/services/authentication.service";
import {Router} from "@angular/router";
import {TokenService} from "../services/api-auth/services/token.service";

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrl: './login.component.css',
    standalone: false
})
export class LoginComponent implements OnInit{
  errorMsg: Array<String> = [];
  authRequestInit:AuthenticationRequest={email:"",password:""};
  authRequest:Authenticate$Params={body:this.authRequestInit};
  formLogin:any;
  hide: boolean=true;
  token:any;
  ngOnInit(): void {

    this.formLogin = new FormGroup({
      email: new FormControl(),
      password:new FormControl()
    });
  }
  constructor(private authService:AuthenticationService,
              private formBuilder:FormBuilder,
              private router:Router,
              private tokenService:TokenService
  ) {
    this.authService=authService;
    this.formBuilder = formBuilder;
    this.router=router;
    this.tokenService =tokenService;
  }

  handleAuthentication(){
    this.errorMsg = [];
    this.authRequestInit.email=this.formLogin.get("email").value;
    this.authRequestInit.password=this.formLogin.get("password").value;

    this.authService.authenticate(this.authRequest)
      .subscribe({
        next:(response)=>{
           this.token = response.token;
          this.tokenService.setToken(this.token);
          this.router.navigateByUrl('/');
        },

        error:err => {
          // console.log("-----"+err.error.validationErrors);
          this.errorMsg.push(err.error.businessErrorDescription || err.error.validationErrors);

          // this.errorMsg.push(err.error.validationErrors);
        }
      })


  }

}
