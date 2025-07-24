import { Component, OnInit } from '@angular/core';
import { UserDto } from '../../services/api-auth/models/user-dto';
import { AuthenticationService } from '../../services/api-auth/services/authentication.service';
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrl: './users.component.css',
  standalone:false
})
export class UsersComponent implements OnInit {
  users: UserDto[] = [];
  filteredUsers: UserDto[] = [];
  currentPage: number = 1;
  pageSize: number = 5;
  searchQuery: string = '';
  sortColumn: keyof UserDto | '' = '';
  sortAsc: boolean = true;

  constructor(private userService: AuthenticationService,private router:Router,private route :ActivatedRoute) {}

  ngOnInit(): void {
    this.getUsers();
  }

  getUsers() {
    this.userService.loadUsers().subscribe({
      next: (response) => {
        this.users = response;
        this.applyFilters();
      },
      error: (err) => console.error(err),
    });
  }

  applyFilters() {
    this.filteredUsers = this.users
      .filter(user =>
        Object.values(user).some(val =>
          (val || '').toString().toLowerCase().includes(this.searchQuery.toLowerCase())
        )
      )
      .sort((a, b) => {
        if (!this.sortColumn) return 0;
        const aValue = (a[this.sortColumn] || '').toString().toLowerCase();
        const bValue = (b[this.sortColumn] || '').toString().toLowerCase();
        return this.sortAsc ? aValue.localeCompare(bValue) : bValue.localeCompare(aValue);
      });
  }

  changePage(page: number) {
    this.currentPage = page;
  }

  sortBy(column: keyof UserDto) {
    if (this.sortColumn === column) {
      this.sortAsc = !this.sortAsc;
    } else {
      this.sortColumn = column;
      this.sortAsc = true;
    }
    this.applyFilters();
  }

  get paginatedUsers(): UserDto[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredUsers.slice(start, start + this.pageSize);
  }

  totalPages(): number {
    return Math.ceil(this.filteredUsers.length / this.pageSize);
  }
  goToDetails(email: string | undefined): void {
    this.router.navigate(['/users/user-details', email], { relativeTo: this.route});
  }
}
