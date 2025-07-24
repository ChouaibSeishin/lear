import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProjectControllerService } from '../../services/api-project/services/project-controller.service';
import { VariantControllerService } from '../../services/api-project/services/variant-controller.service';
import { ProjectResponse } from '../../services/api-project/models/project-response';
import { VariantResponse, VariantRequest } from '../../services/api-project/models';
import {UpdateProject$Params} from "../../services/api-project/fn/project-controller/update-project";
import {ToastrService} from "ngx-toastr";
import {Breadcrumb, BreadcrumbService} from "../../services/breadcrumbs/breadcrumbs.service";
import {ProductionLineControllerService} from "../../services/api-machine/services/production-line-controller.service";
import {ProductionLineResponse} from "../../services/api-machine/models/production-line-response";
import {TokenService} from "../../services/api-auth/services/token.service";

@Component({
  selector: 'app-project-details',
  standalone:false,
  templateUrl: './project-details.component.html',
  styleUrl: './project-details.component.css'
})

export class ProjectDetailsComponent implements OnInit {
  projectId!: number;
  lines:ProductionLineResponse[]=[];
  crumb: Breadcrumb = {url: "", label: ""};
  project!: ProjectResponse;
  projectLines:number[]=[];
  variants: VariantResponse[] = [];
  editableVariant: VariantResponse = {};
  newVariant: VariantRequest = {name: '', status: '',productionLineIds:[]};
  tokenService:TokenService;




  constructor(
    private route: ActivatedRoute,
    private projectService: ProjectControllerService,
    private lineService:ProductionLineControllerService,
    private variantService: VariantControllerService,
    private toaster: ToastrService,
    private titleService: BreadcrumbService,
    private token:TokenService
  ) {
    this.tokenService= token;
  }

  ngOnInit(): void {

    this.projectId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.projectId) {
      this.projectService.getProject({id: this.projectId}).subscribe(project => {
        this.titleService.setCustomLabelForLast(project.name || `Project ${this.projectId}`);
        this.project = project;
        this.projectLines = this.project.productionLineIds!;
      });
    }

     this.getLines();
    this.getProjectVariants();

  }
  get filteredLines(): ProductionLineResponse[] {
    return this.lines.filter(line => this.projectLines.includes(line.id!));
  }


  getProjectVariants() {
    this.variantService.getVariantsByProject({projectId: this.projectId}).subscribe(res => {
      this.variants = res;
    });
  }

  addVariant() {
    const request: VariantRequest = {
      ...this.newVariant,
      projectId: this.projectId
    };

    this.variantService.createVariant({body: request}).subscribe(() => {
      this.newVariant = {name: '', status: ''};
      this.getProjectVariants();
    });
  }

  updateProject() {
    if (!this.project?.id || !this.project.name || !this.project.description) return;

    const params: UpdateProject$Params = {
      id: this.project.id,
      body: {
        name: this.project.name,
        description: this.project.description,
        productionLineIds:this.project.productionLineIds
      }
    };

    this.projectService.updateProject(params).subscribe({
      next: () => this.toaster.success('Project updated successfully'),
      error: () => this.toaster.error('Failed to update project')
    });
  }

  deleteVariant(id: number) {
    this.variantService.deleteVariant({id: id}).subscribe({
      next: () => {
        this.toaster.success('Variant deleted');
        this.variants = this.variants.filter(v => v.id !== id);
      },
      error: () => this.toaster.error('Failed to delete variant')
    });
  }


  openEditModal(variant: VariantResponse) {
    this.editableVariant = { ...variant };
  }

  updateVariant() {
    if (!this.editableVariant.id) return;

    this.variantService.updateVariant({
      id: this.editableVariant.id,
      body: this.editableVariant
    }).subscribe({
      next: () => {
        this.toaster.success('Variant updated');

        const index = this.variants.findIndex(v => v.id === this.editableVariant.id);
        if (index > -1) {
          this.variants[index] = { ...this.editableVariant };
        }
      },
      error: () => this.toaster.error('Failed to update variant')
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

}
