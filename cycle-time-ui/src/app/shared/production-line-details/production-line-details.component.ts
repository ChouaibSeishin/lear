import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

import {  BreadcrumbService } from '../../services/breadcrumbs/breadcrumbs.service';
import { ProductionLineResponse } from '../../services/api-machine/models/production-line-response';
import { ProductionLineRequest } from '../../services/api-machine/models/production-line-request';
import { MachineResponse } from '../../services/api-machine/models/machine-response';

import { ProductionLineControllerService } from '../../services/api-machine/services/production-line-controller.service';
import { UpdateProductionLine$Params } from '../../services/api-machine/fn/production-line-controller/update-production-line';

@Component({
  selector: 'app-production-line-details',
  templateUrl: './production-line-details.component.html',
  styleUrls: ['./production-line-details.component.css'],
  standalone: false,
})
export class ProductionLineDetailsComponent implements OnInit {
  productionLineId!: number;
  productionLine!: ProductionLineResponse;
  machines: MachineResponse[] = [];


  constructor(
    private route: ActivatedRoute,
    private productionLineService: ProductionLineControllerService,
    private toaster: ToastrService,
    private titleService: BreadcrumbService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.productionLineId = Number(id);

    if (id) {
      this.productionLineService.getProductionLine({ id: this.productionLineId }).subscribe({
        next: (productionLine) => {
          this.productionLine = productionLine;
          console.log(this.productionLine);
          this.titleService.setCustomLabelForLast(productionLine.name || `Production Line ${id}`);
        },
        error: () => {
          this.toaster.error('Failed to load production line');
        },
      });
    }
  }

  updateProductionLine(): void {
    if (!this.productionLine?.id || !this.productionLine.name || !this.productionLine.description) return;

    const params: UpdateProductionLine$Params = {
      id: this.productionLine.id,
      body: {
        name: this.productionLine.name,
        description: this.productionLine.description,
      },
    };

    this.productionLineService.updateProductionLine(params).subscribe({
      next: () => this.toaster.success('Production line updated successfully'),
      error: () => this.toaster.error('Failed to update production line'),
    });
  }
}
