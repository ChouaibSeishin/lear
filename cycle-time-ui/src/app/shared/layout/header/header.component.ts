import {Component, NgIterable, OnInit} from '@angular/core';
import {AuthenticationService} from "../../../services/api-auth/services/authentication.service";
import {UserDto} from "../../../services/api-auth/models/user-dto";
import {Breadcrumb, BreadcrumbService} from "../../../services/breadcrumbs/breadcrumbs.service";
import {SidebarService} from "../../../services/sidebar/sidebar.service";
import {TokenService} from "../../../services/api-auth/services/token.service";
import {Observable} from "rxjs";
import {UserLogControllerService} from "../../../services/api-auth/services/user-log-controller.service";
import {UserLogResponse} from "../../../services/api-auth/models/user-log-response";


@Component({
  selector: 'app-header',
  standalone: false,
  templateUrl: './header.component.html',
  styleUrl: 'header.component.css'
})
export class HeaderComponent implements OnInit{
  breadcrumbs: Breadcrumb[] = [];
  userInitial = '?';
  userDto:UserDto={};
  tokenService: TokenService;
  logs:UserLogResponse[]=[];

 constructor(private userService:AuthenticationService,
             private breadcrumbService: BreadcrumbService,
             private sidebarService: SidebarService,
             private token:TokenService,
             private logsService:UserLogControllerService
             )

 {

this.tokenService = token;
 }

  ngOnInit(): void {

    this.loadUser();

    this.breadcrumbService.breadcrumbs$.subscribe(crumbs => {
      this.breadcrumbs = crumbs;
    });
    this.getLogs();
  }

  toggleSidebar() {
    this.sidebarService.toggle();
  }
  loadUser(): void {

    this.userService.loadUser().subscribe({
      next:(response)=>{
        this.userDto = response
        this.userInitial = this.userDto.firstName!.charAt(0).toUpperCase();
      },
      error:err => {
        console.log(err);
      }
    })

  }
  getLogs(){
   this.logsService.getAllLogs().subscribe({
     next:(res)=>{
       this.logs = res.filter((log)=> log.seen==false);


     },
     error:err => {
       console.log(err);
     }
   })
  }

    protected readonly window = window;

  markAsSeen(id: number | undefined) {
    this.logsService.updateSeen({id:id!,seen:true}).subscribe({
      next:()=>{
        this.getLogs();
      }
    })


  }
}
