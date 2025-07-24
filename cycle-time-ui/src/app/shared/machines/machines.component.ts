import { Component, OnInit } from '@angular/core';

import {MachineResponse} from "../../services/api-machine/models/machine-response";
import {MachineRequest} from "../../services/api-machine/models/machine-request";
import {MachineControllerService} from "../../services/api-machine/services/machine-controller.service";
import {TokenService} from "../../services/api-auth/services/token.service";
import {ToastrService} from "ngx-toastr";
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {filter} from "rxjs/operators";
import {ProductionLineControllerService} from "../../services/api-machine/services/production-line-controller.service";
import {ProductionLineResponse} from "../../services/api-machine/models/production-line-response";


@Component({
  selector: 'app-machines',
  templateUrl: './machines.component.html',
  standalone:false,
  styleUrl: './machines.component.css'
})
export class MachinesComponent implements OnInit {
  tokenService:TokenService;

  machines: MachineResponse[] = [];
  filteredMachines: MachineResponse[] = [];
  paginatedMachines: MachineResponse[] = [];
  newMachine: MachineRequest = {};
  searchQuery: string = '';
  showTable: boolean = true;
  currentPage: number = 1;
  itemsPerPage: number = 5;
  productionLines:Array<ProductionLineResponse>=[];

  private bootstrap: any;


  constructor(
    private machineService: MachineControllerService,
    private prodLineService:ProductionLineControllerService,
    private token:TokenService,
    private toaster:ToastrService,
    private router:Router,
    private route: ActivatedRoute

  )
  {
    this.tokenService = token;
  }

  ngOnInit(): void {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.showTable = !(this.route.firstChild?.snapshot.paramMap.has('id'));
      });
    this.getMachines();
    this.getProductionLines();
  }

  getProductionLines(){
  this.prodLineService.getAllProductionLines().subscribe({
    next:(resp)=>{
      this.productionLines = resp;
    },
    error:err => {
      console.log(err);
    }
  })
  }

  getMachines(): void {
    this.machineService.getAllMachines().subscribe((data: MachineResponse[]) => {
      this.machines = data;
      this.applyFilters();
    });
  }

  applyFilters(): void {
    const query = this.searchQuery.trim().toLowerCase();
    this.filteredMachines = this.machines.filter(machine =>
      machine.name?.toLowerCase().includes(query) ||
      machine.description?.toLowerCase().includes(query)
    );
    this.paginate();
  }

  paginate(): void {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    this.paginatedMachines = this.filteredMachines.slice(start, end);
  }

  changePage(page: number): void {
    this.currentPage = page;
    this.paginate();
  }

  totalPages(): number {
    return Math.ceil(this.filteredMachines.length / this.itemsPerPage);
  }

  sortBy(key: keyof MachineResponse): void {
    this.filteredMachines.sort((a, b) => {
      const valueA = a[key] ?? '';
      const valueB = b[key] ?? '';
      return valueA < valueB ? -1 : valueA > valueB ? 1 : 0;
    });
    this.paginate();
  }

  goToDetails(id: number | undefined): void {
    this.router.navigate(['machine-details', id], { relativeTo: this.route});
  }

  deleteMachine(id: number): void {
    if (confirm('Are you sure you want to delete this machine?')) {
      this.machineService.deleteMachine({id:id}).subscribe(() => {
        this.machines = this.machines.filter(m => m.id !== id);
        this.applyFilters();
      });
    }
  }

  submitMachine(): void {
    if (!this.newMachine.name || !this.newMachine.description) return;

    this.machineService.createMachine({body:this.newMachine}).subscribe({
      next :(created)=>{
      this.machines.push(created);
      this.toaster.success("Machine added successfully");
      this.applyFilters();
      this.newMachine = {}; // Reset form

        const modal = this.bootstrap.Modal.getInstance(document.getElementById('addMachineModal'));
        modal?.hide();

    },
      error:err => {
        this.toaster.error("Error adding machine");
      }
    }
    );
  }


}
