import { Component, OnInit } from '@angular/core';
import { TokenService } from '../../services/api-auth/services/token.service';
import { ToastrService } from 'ngx-toastr';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs/operators';
import {ProductionLineResponse} from "../../services/api-machine/models/production-line-response";
import {ProductionLineRequest} from "../../services/api-machine/models/production-line-request";
import {ProductionLineControllerService} from "../../services/api-machine/services/production-line-controller.service";

@Component({
  selector: 'app-production-line',
  templateUrl: './production-line.component.html',
  styleUrl: './production-line.component.css',
  standalone: false
})
export class ProductionLineComponent implements OnInit {
  tokenService: TokenService;

  productionLines: ProductionLineResponse[] = [];
  filteredProductionLines: ProductionLineResponse[] = [];
  paginatedProductionLines: ProductionLineResponse[] = [];
  newProductionLine: ProductionLineRequest = {};
  searchQuery: string = '';
  showTable: boolean = true;
  currentPage: number = 1;
  itemsPerPage: number = 5;

  private bootstrap: any;

  constructor(
    private productionLineService: ProductionLineControllerService,
    private token: TokenService,
    private toaster: ToastrService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.tokenService = token;
  }

  ngOnInit(): void {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.showTable = !(this.route.firstChild?.snapshot.paramMap.has('id'));
      });
    this.getProductionLines();
  }

  getProductionLines(): void {
    this.productionLineService.getAllProductionLines().subscribe(data => {
      this.productionLines = data;
      this.applyFilters();
    });
  }

  applyFilters(): void {
    const query = this.searchQuery.trim().toLowerCase();
    this.filteredProductionLines = this.productionLines.filter(line =>
      line.name?.toLowerCase().includes(query) ||
      line.description?.toLowerCase().includes(query)
    );
    this.paginate();
  }

  paginate(): void {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    this.paginatedProductionLines = this.filteredProductionLines.slice(start, end);
  }

  changePage(page: number): void {
    this.currentPage = page;
    this.paginate();
  }

  totalPages(): number {
    return Math.ceil(this.filteredProductionLines.length / this.itemsPerPage);
  }

  sortBy(key: keyof ProductionLineResponse): void {
    this.filteredProductionLines.sort((a, b) => {
      const valueA = a[key] ?? '';
      const valueB = b[key] ?? '';
      return valueA < valueB ? -1 : valueA > valueB ? 1 : 0;
    });
    this.paginate();
  }

  goToDetails(id: number | undefined): void {
    this.router.navigate(['production-line-details', id], { relativeTo: this.route });
  }

  deleteProductionLine(id: number): void {
    if (confirm('Are you sure you want to delete this production line?')) {
      this.productionLineService.deleteProductionLine({ id }).subscribe(() => {
        this.productionLines = this.productionLines.filter(l => l.id !== id);
        this.applyFilters();
      });
    }
  }

  submitProductionLine(): void {
    if (!this.newProductionLine.name || !this.newProductionLine.description) return;

    this.productionLineService.createProductionLine({ body: this.newProductionLine }).subscribe({
      next: created => {
        this.productionLines.push(created);
        this.toaster.success("Production Line added successfully");
        this.applyFilters();
        this.newProductionLine = {};

        const modal = this.bootstrap.Modal.getInstance(document.getElementById('addProductionLineModal'));
        modal?.hide();
      },
      error: () => {
        this.toaster.error("Error adding production line");
      }
    });
  }


}
