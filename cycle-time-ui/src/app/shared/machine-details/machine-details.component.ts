import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ToastrService } from 'ngx-toastr';
import { Breadcrumb, BreadcrumbService } from '../../services/breadcrumbs/breadcrumbs.service';
import {MachineResponse} from "../../services/api-machine/models/machine-response";
import {StepResponse} from "../../services/api-machine/models/step-response";
import {StepRequest} from "../../services/api-machine/models/step-request";
import {MachineControllerService} from "../../services/api-machine/services/machine-controller.service";
import {StepControllerService} from "../../services/api-machine/services/step-controller.service";
import {UpdateMachine$Params} from "../../services/api-machine/fn/machine-controller/update-machine";
import {ProductionLineControllerService} from "../../services/api-machine/services/production-line-controller.service";
import {ProductionLineResponse} from "../../services/api-machine/models/production-line-response";

@Component({
  selector: 'app-machine-details',
  templateUrl: './machine-details.component.html',
  styleUrl: './machine-details.component.css',
  standalone:false,
})
export class MachineDetailsComponent implements OnInit {
  machineId!: number;
  machine!: MachineResponse;
  productionLines:ProductionLineResponse[]=[];
  steps: StepResponse[] = [];
  editableStep: StepResponse = {};
  newStep: StepRequest = { name: '', description: '', orderIndex: undefined, requiresManualTracking: false };

  constructor(
    private route: ActivatedRoute,
    private machineService: MachineControllerService,
    private stepService: StepControllerService,
    private toaster: ToastrService,
    private titleService: BreadcrumbService,
    private prodLineService:ProductionLineControllerService
  ) {}

  ngOnInit(): void {

    const id = this.route.snapshot.paramMap.get('id');
    this.machineId = Number(id);


    if (id) {
      this.machineService.getMachine({id: this.machineId}).subscribe(machine => {
        this.titleService.setCustomLabelForLast(machine.name || `Machine ${id}`);
        this.machine = machine;
      });
    }


    this.getMachineSteps();
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

  getMachineSteps() {
    this.stepService.getStepsByMachine({ machineId: this.machineId }).subscribe(res => {
      this.steps = res;
    });
  }

  addStep() {
    const request: StepRequest = {
      ...this.newStep,
      machineId: this.machineId
    };

    this.stepService.createStep({ body: request }).subscribe(() => {
      this.newStep = { name: '', description: '', orderIndex: 0, requiresManualTracking: false };
      this.getMachineSteps();
    });
  }

  updateMachine() {
    if (!this.machine?.id || !this.machine.name || !this.machine.description) return;

    const params: UpdateMachine$Params = {
      id: this.machine.id,
      body: {
        name: this.machine.name,
        description: this.machine.description,
        brand: this.machine.brand,
        type: this.machine.type,
        productionLineId:this.machine.productionLineId,
      }
    };

    this.machineService.updateMachine(params).subscribe({
      next: () => this.toaster.success('Machine updated successfully'),
      error: () => this.toaster.error('Failed to update machine')
    });
  }

  deleteStep(id: number) {
    this.stepService.deleteStep({ id: id }).subscribe({
      next: () => {
        this.toaster.success('Step deleted');
        this.steps = this.steps.filter(s => s.id !== id);
        this.getMachineSteps();
      },
      error: () => this.toaster.error('Failed to delete step')
    });
  }

  openEditModal(step: StepResponse) {
    this.editableStep = { ...step };
  }

  updateStep() {
    if (!this.editableStep.id) return;

    const request: StepRequest = {
      name: this.editableStep.name || '',
      description: this.editableStep.description || '',
      orderIndex: this.editableStep.orderIndex || 0,
      requiresManualTracking: this.editableStep.requiresManualTracking || false,
      machineId: this.machineId
    };

    this.stepService.updateStep({
      id: this.editableStep.id,
      body: request
    }).subscribe({
      next: () => {
        this.toaster.success('Step updated');
        const index = this.steps.findIndex(s => s.id === this.editableStep.id);
        if (index > -1) {
          this.steps[index] = { ...this.editableStep };
        }
      },
      error: () => this.toaster.error('Failed to update step')
    });
  }

}
