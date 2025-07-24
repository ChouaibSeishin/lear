import {Component, input, OnInit} from '@angular/core';
import {SidebarService} from "../../../services/sidebar/sidebar.service";
import {Router} from "@angular/router";



@Component({
  selector: 'app-sidebar',
  standalone: false,
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements OnInit{
  sidebarServ:SidebarService;
  collapsed = false;
constructor(private sidebarService:SidebarService, private router:Router) {
  this.sidebarServ = sidebarService;
}


  ngOnInit(): void {
  screen.width <= 600 ? this.sidebarService.setCollapsed(true) : this.sidebarService.setCollapsed(false);

    this.sidebarService.collapsed$.subscribe(value => {
      this.collapsed = value;
    });

  }



  logout() {
    localStorage.removeItem('jwt');
    this.router.navigate(['/login']);
  }

  protected readonly screen = screen;
}
