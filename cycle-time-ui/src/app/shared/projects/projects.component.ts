import { Component, OnInit} from '@angular/core';
import {ProjectResponse} from "../../services/api-project/models/project-response";
import {ProjectControllerService} from "../../services/api-project/services/project-controller.service";
import {ToastrService} from "ngx-toastr";
import {TokenService} from "../../services/api-auth/services/token.service";
import {ProjectRequest} from "../../services/api-project/models/project-request";
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {filter} from "rxjs/operators";
import {ProductionLineResponse} from "../../services/api-machine/models/production-line-response";
import {ProductionLineControllerService} from "../../services/api-machine/services/production-line-controller.service";

@Component({
  selector: 'app-projects',
  standalone: false,
  templateUrl: './projects.component.html',
  styleUrl: './projects.component.css'
})
export class ProjectsComponent implements OnInit  {
  showTable = true;
  lines:ProductionLineResponse[]=[];
  tokenService:TokenService;
  newProject: ProjectRequest = { name: '', description: '',productionLineIds:[] };
  projects:ProjectResponse[]=[];
  filteredProjects: ProjectResponse[] = [];
  currentPage: number = 1;
  pageSize: number = 5;
  searchQuery: string = '';
  sortColumn: keyof ProjectResponse | '' = '';
  sortAsc: boolean = true;
  private bootstrap: any;
  constructor(private projectService:ProjectControllerService,
              private lineService:ProductionLineControllerService,
              private token:TokenService,
              private toaster:ToastrService,
              private router:Router,
              private route: ActivatedRoute) {
    this.tokenService = token;
  }

  ngOnInit(): void {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {

        this.showTable = !(this.route.firstChild?.snapshot.paramMap.has('id'));
      });
    this.onPageReload();
    this.getProjects();
    this.getLines();
  }
  onPageReload(): void {

    if (window.performance.navigation.type === window.performance.navigation.TYPE_NAVIGATE || window.performance.navigation.type === window.performance.navigation.TYPE_RELOAD) {
      this.showTable = !this.route.firstChild?.snapshot.paramMap.has('id');

    }
    else
      this.showTable=true;
  }
 getProjects(){
    this.projectService.getAllProjects().subscribe({
      next:(response)=>{
        this.projects=response;
        this.applyFilters();
        console.log(response);
      },
      error:err => {
        console.log(err);
      }
    })
 }

  applyFilters() {
    this.filteredProjects = this.projects
      .filter(project =>
        Object.values(project).some(val =>
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

  sortBy(column: keyof ProjectResponse) {
    if (this.sortColumn === column) {
      this.sortAsc = !this.sortAsc;
    } else {
      this.sortColumn = column;
      this.sortAsc = true;
    }
    this.applyFilters();
  }

  get paginatedProjects(): ProjectResponse[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredProjects.slice(start, start + this.pageSize);
  }

  totalPages(): number {
    return Math.ceil(this.filteredProjects.length / this.pageSize);
  }


  deleteProject(id: number) {
    this.projectService.deleteProject({ id: id }).subscribe({
      next: () => {
        this.toaster.success("Project " + id + " deleted successfully");
        this.refreshProjects(); // <-- refresh the list
      },
      error: err => {
        this.toaster.error("Error deleting project " + id);
      }
    });
  }
 getLines(){
    this.lineService.getAllProductionLines().subscribe({
      next:(response)=>{
        this.lines = response;
      },
      error:err=>{
        console.log(err);
      }
    })

}

  submitProject() {
    if (!this.newProject.name) return;

    this.projectService.createProject({body:this.newProject}).subscribe({
      next:()=>{
        this.newProject = {};
        this.refreshProjects();
        this.toaster.success("Project added successfully");
        const modal = this.bootstrap.Modal.getInstance(document.getElementById('addProjectModal')!);
        modal?.hide();
      },
      error:err => {
        this.toaster.error("Error adding Project ");
      }
    });

  }
  refreshProjects() {
   this.getProjects();
  }
  goToDetails(projectId: number ) {
    this.router.navigate(['project-details', projectId], {relativeTo: this.route}) ;


  }


}
