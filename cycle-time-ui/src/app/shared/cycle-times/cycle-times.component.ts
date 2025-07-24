import { Component, OnInit, OnDestroy } from '@angular/core';
import { interval, Subscription } from 'rxjs';
import { CycleTimeRequest } from "../../services/api-cycletime/models/cycle-time-request";
import { CycleTime } from "../../services/api-cycletime/models/cycle-time";
import { CycleTimeControllerService } from "../../services/api-cycletime/services/cycle-time-controller.service";
import { AuthenticationService } from "../../services/api-auth/services/authentication.service";
import { UserDto } from "../../services/api-auth/models/user-dto";
import { ProductionLineResponse } from "../../services/api-machine/models/production-line-response";
import { StepResponse } from "../../services/api-machine/models/step-response";
import { VariantResponse } from "../../services/api-project/models/variant-response";
import { ProductionLineControllerService } from "../../services/api-machine/services/production-line-controller.service";
import { StepControllerService } from "../../services/api-machine/services/step-controller.service";
import { VariantControllerService } from "../../services/api-project/services/variant-controller.service";
import { MachineResponse } from "../../services/api-machine/models/machine-response";
import {ProjectResponse} from "../../services/api-project/models/project-response";
import {ProjectControllerService} from "../../services/api-project/services/project-controller.service";
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {filter} from "rxjs/operators";

@Component({
  selector: 'app-cycle-times',
  templateUrl: './cycle-times.component.html',
  styleUrls: ['./cycle-times.component.css'],
  standalone: false,
})
export class CycleTimesComponent implements OnInit, OnDestroy {
  cycleRequest: CycleTimeRequest = {};
  machineModel: MachineResponse = {};
  cycleTimes: CycleTime[] = [];
  lines: ProductionLineResponse[] = [];
  steps: StepResponse[] = [];
  variants: VariantResponse[] = [];
  isRunning = false;
  startTime?: string;
  endTime?: string;
  elapsedMilliseconds = 0;
  private startTimestamp?: number;
  selectedLine: ProductionLineResponse = {};
  selectedMachine!: MachineResponse ;
  selectedProject:ProjectResponse={};
  user: UserDto = {};
  private timerSubscription?: Subscription;
  formattedElapsed: string = '';
  filteredCycles: CycleTime[] = [];
  paginatedCycles: CycleTime[] = [];
  searchQuery: string = '';
  currentPage: number = 1;
  itemsPerPage: number = 10;
  recordedAttempts: {
    elapsedMilliseconds: number;
    formattedElapsed: string;
    startTime: string;
    endTime: string;
    lineId?: number;
    machineId?: number;
    stepId?: number;
    lineName?: string;
    machineName?: string;
    stepName?: string;
    projectId?: number;
    variantId?: number;
  }[] = [];

  selectedAttemptIndex: number | null = null;
  saving = false;
  projects: ProjectResponse[]=[];
  selectedVariant: VariantResponse={};
  durationMinutes: number | null = null;
  durationSeconds: number | null = null;
  durationMilliseconds: number | null = null;

  // New properties for clientCycleTime
  clientDurationMinutes: number | null = null;
  clientDurationSeconds: number | null = null;
  clientDurationMilliseconds: number | null = null;

  showTable:Boolean=true;

  constructor(
    private cycleTimeService: CycleTimeControllerService,
    private projectService:ProjectControllerService,
    private userService: AuthenticationService,
    private lineService: ProductionLineControllerService,
    private stepService: StepControllerService,
    private variantService: VariantControllerService,
    private router:Router,
    private route:ActivatedRoute
  ) {
  }

  ngOnInit(): void {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.showTable = !(this.route.firstChild?.snapshot.paramMap.has('id'));
      });
    this.loadCycleTimes();
    this.getUser();
    this.getProjects();
  }

  ngOnDestroy(): void {
    if (this.timerSubscription) {
      this.timerSubscription.unsubscribe();
    }
  }

  getProjects(): void {
    this.projectService.getAllProjects().subscribe({
      next:(res)=>{
        this.projects = res;
      },
      error:err => {
        console.log(err);
      }
    });
  }

  /**
   * Resets the timer state and clears any recorded attempts.
   * This is called when a key dropdown selection changes to ensure a fresh recording context.
   */
  private resetTimerState(): void {
    if (this.isRunning) {
      this.stopTimer(); // Ensure timer is stopped first if it's running
    }
    this.elapsedMilliseconds = 0;
    this.formattedElapsed = '0m 00.000ms'; // Reset display to initial state
    this.startTimestamp = undefined;
    this.startTime = undefined;
    this.endTime = undefined;
    this.recordedAttempts = []; // Clear all previous recorded attempts
    this.selectedAttemptIndex = null; // Deselect any chosen attempt
  }

  onProjectSelected(projectId: number): void {
    this.resetTimerState(); // Reset timer when project changes
    this.selectedProject = this.projects.find(project => project.id == projectId)!;
    this.cycleRequest.projectId = projectId;
    this.variants = this.selectedProject.variants!;
    this.applyFilters();
    // Clear subsequent selections and options
    this.selectedVariant = {};
    this.cycleRequest.variantId = undefined;
    this.lines = [];
    this.selectedLine = {};
    this.cycleRequest.lineId = undefined;
    this.selectedMachine = {};
    this.machineModel.id = undefined;
    this.steps = [];
    this.cycleRequest.stepId = undefined;
  }

  onVariantSelected(variantId:number): void {
    this.resetTimerState(); // Reset timer when variant changes
    this.selectedVariant = this.variants.find(variant => variant.id == variantId)!;
    this.cycleRequest.variantId = variantId;
    this.getProductionLines();

    this.lines = [];
    this.selectedLine = {};
    this.cycleRequest.lineId = undefined;
    this.selectedMachine = {};
    this.machineModel.id = undefined;
    this.steps = [];
    this.cycleRequest.stepId = undefined;
    this.applyFilters();
  }

  getProductionLines(): void {
    let ids:number[] = [];
    this.selectedVariant.productionLineIds?.forEach(id=>ids.push(id));
    this.lineService.getByIds({ids}).subscribe({
      next: (data) => {
        this.lines = data;
      },
      error: (err) => console.error('Failed to load lines', err)
    });
  }

  onLineSelected(lineId: number): void {
    this.resetTimerState(); // Reset timer when line changes
    this.selectedLine = this.lines.find(line => line.id == lineId)! ;
    this.cycleRequest.lineId = lineId;
    this.applyFilters();
    // Clear subsequent selections and options
    this.selectedMachine = {};
    this.machineModel.id = undefined;
    this.steps = [];
    this.cycleRequest.stepId = undefined;
  }

  onMachineSelected(machineId: number): void {
    this.resetTimerState(); // Reset timer when machine changes
    if (this.selectedLine?.machines) {
      this.selectedMachine = this.selectedLine.machines.find(machine => machine.id == machineId)!;
      this.machineModel.id = machineId;
      this.getSteps();
      this.applyFilters();
    }
    this.cycleRequest.stepId = undefined;
    this.steps = []; // Will be repopulated by getSteps
  }

  /**
   * Called when a step is selected. Resets the timer state.
   * @param stepId The ID of the selected step.
   */
  onStepSelected(stepId: number): void {
    this.resetTimerState(); // Reset timer when step changes
    this.cycleRequest.stepId = stepId;
    this.applyFilters();
  }

  getSteps(): void {
    this.stepService.getStepsByMachine({ machineId: this.selectedMachine.id! }).subscribe({
      next: (steps) => {
        this.steps = steps;
      },
      error: err => {
        console.log(err);
      }
    });
  }

  getUser(): void {
    this.userService.loadUser().subscribe({
      next: (user) => {
        this.user = user;
      },
      error: (err) => {
        console.log(err);
      }
    });
  }

  startTimer(): void {
    this.isRunning = true;
    this.startTimestamp = Date.now();
    this.startTime = new Date(this.startTimestamp).toISOString();
    this.cycleRequest.startTime = this.startTime;
    this.startInterval();
    console.log(this.isRunning)
  }

  private startInterval(): void {
    this.timerSubscription = interval(10).subscribe(() => {
      if (this.startTimestamp) {
        const now = Date.now();
        this.elapsedMilliseconds = now - this.startTimestamp;
        this.formattedElapsed = this.formatElapsedTime(this.elapsedMilliseconds);
      }
    });
  }

  private formatElapsedTime(ms: number): string {
    const minutes = Math.floor(ms / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    const milliseconds = ms % 1000;
    return `${minutes}m ${String(seconds).padStart(2, '0')}.${String(milliseconds).padStart(3, '0')}ms`;
  }

  stopTimer(): void {
    if (!this.isRunning) return;

    this.isRunning = false;
    const endTimestamp = Date.now();
    this.endTime = new Date(endTimestamp).toISOString();

    if (this.startTimestamp && this.selectedProject.id != null ) {
      const elapsed = endTimestamp - this.startTimestamp;
      const formatted = this.formatElapsedTime(elapsed);

      this.recordedAttempts.push({
        elapsedMilliseconds: elapsed,
        formattedElapsed: formatted,
        startTime: new Date(this.startTimestamp).toISOString(),
        endTime: this.endTime,
        projectId: this.selectedProject.id,
        variantId: this.selectedVariant.id,
        lineId: this.selectedLine.id,
        machineId: this.selectedMachine.id,
        stepId: this.cycleRequest.stepId,
        lineName: this.selectedLine.name,
        machineName: this.selectedMachine.name,
        stepName: this.steps.find(step => step.id === this.cycleRequest.stepId)?.name,
      });
    }

    if (this.timerSubscription) {
      this.timerSubscription.unsubscribe();
    }
  }

  // --- Add this new helper method ---
  private formatDurationToISO(seconds: number, nano: number): string {
    const fractionalSeconds = seconds + (nano / 1_000_000_000);
    const formattedFractionalSeconds = fractionalSeconds.toFixed(3); // Keep 3 decimal places for milliseconds
    return `PT${formattedFractionalSeconds}S`;
  }

  saveCycleTime(): void {
    if (this.selectedAttemptIndex === null || this.saving) {
      return;
    }

    this.saving = true;
    const selected = this.recordedAttempts[this.selectedAttemptIndex];

    this.cycleRequest.userId = this.user.userId;
    this.cycleRequest.startTime = selected.startTime;
    this.cycleRequest.endTime = selected.endTime;
    this.cycleRequest.status = 'PENDING';
    this.cycleRequest.isManual = true;

    // Call updateTheoriticalCycleTime to ensure this.cycleRequest.theoriticalCycleTime object is populated
    this.updateTheoriticalCycleTime();
    // New: Call updateClientCycleTime to ensure this.cycleRequest.clientCycleTime object is populated
    this.updateClientCycleTime();

    // Create a NEW request object to send, and set duration fields as ISO strings
    const requestToSend: CycleTimeRequest = { ...this.cycleRequest };

    if (this.cycleRequest.theoriticalCycleTime?.seconds !== undefined && this.cycleRequest.theoriticalCycleTime?.nano !== undefined) {
      requestToSend.theoriticalCycleTime = this.formatDurationToISO(
        this.cycleRequest.theoriticalCycleTime.seconds,
        this.cycleRequest.theoriticalCycleTime.nano
      ) as any; // Cast as 'any' because CycleTimeRequest expects an object for theoreticalCycleTime, but backend expects string
    } else {
      requestToSend.theoriticalCycleTime = 'PT0S' as any; // Default to zero duration if not set
    }

    // New: Format clientCycleTime to ISO string if populated
    if (this.cycleRequest.clientCycleTime?.seconds !== undefined && this.cycleRequest.clientCycleTime?.nano !== undefined) {
      requestToSend.clientCycleTime = this.formatDurationToISO(
        this.cycleRequest.clientCycleTime.seconds,
        this.cycleRequest.clientCycleTime.nano
      ) as any; // Cast as 'any' because CycleTimeRequest expects an object for clientCycleTime, but backend expects string
    } else {
      requestToSend.clientCycleTime = 'PT0S' as any; // Default to zero duration if not set
    }


    requestToSend.lineId = selected.lineId;
    requestToSend.machineId = selected.machineId;
    requestToSend.stepId = selected.stepId;
    requestToSend.projectId = selected.projectId;
    requestToSend.variantId = selected.variantId;


    this.cycleTimeService.create({ body: requestToSend }).subscribe({
      next: (newCycle) => {
        this.cycleTimes.push(newCycle);
        this.resetFormAndTimer();
        this.applyFilters();
        this.saving = false;
      },
      error: (err) => {
        console.error('Error saving cycle time:', err);
        this.saving = false;
      },
    });
  }

  private resetFormAndTimer(): void {
    // This method is primarily for resetting the form after a successful save.
    // The timer state is reset by `resetTimerState()` when dropdowns change.
    this.cycleRequest = {};
    this.recordedAttempts = [];
    this.selectedAttemptIndex = null;
    this.elapsedMilliseconds = 0;
    this.startTime = undefined;
    this.endTime = undefined;
    this.startTimestamp = undefined;
    this.formattedElapsed = '';
    this.durationMinutes = null;
    this.durationSeconds = null;
    this.durationMilliseconds = null;

    // Reset client cycle time fields
    this.clientDurationMinutes = null;
    this.clientDurationSeconds = null;
    this.clientDurationMilliseconds = null;
    this.cycleRequest.clientCycleTime = {}; // Ensure it's reset

    this.selectedProject = {};
    this.selectedVariant = {};
    this.selectedLine = {};
    this.selectedMachine = {};
    this.machineModel = {};
    this.variants = [];
    this.lines = [];
    this.steps = [];
    this.cycleRequest.theoriticalCycleTime = {};
  }

  loadCycleTimes(): void {
    this.cycleTimeService.getAll().subscribe({
      next: (data) => {
        this.cycleTimes = data;
        this.applyFilters();
      },
      error: (err) => console.error('Failed to load cycle times', err),
    });
  }

  applyFilters(): void {
    let tempFilteredCycles = [...this.cycleTimes];

    const query = this.searchQuery.trim().toLowerCase();
    if (query) {
      tempFilteredCycles = tempFilteredCycles.filter(cycle => {
        const matchesText =
          cycle.status?.toLowerCase().includes(query);

        const matchesId =
          cycle.projectId?.toString().includes(query) ||
          cycle.variantId?.toString().includes(query) ||
          cycle.lineId?.toString().includes(query) ||
          cycle.machineId?.toString().includes(query) ||
          cycle.stepId?.toString().includes(query);

        return matchesText || matchesId;
      });
    }

    if (this.cycleRequest.projectId) {
      tempFilteredCycles = tempFilteredCycles.filter(cycle =>
        Number(cycle.projectId) == this.cycleRequest.projectId
      );
    }

    if (this.cycleRequest.variantId) {
      tempFilteredCycles = tempFilteredCycles.filter(cycle =>
        Number(cycle.variantId) == this.cycleRequest.variantId
      );
    }

    if (this.cycleRequest.lineId) {
      tempFilteredCycles = tempFilteredCycles.filter(cycle =>
        Number(cycle.lineId) == this.cycleRequest.lineId
      );
    }

    if (this.machineModel.id) {
      tempFilteredCycles = tempFilteredCycles.filter(cycle =>
        Number(cycle.machineId) == this.machineModel.id
      );
    }

    if (this.cycleRequest.stepId) {
      tempFilteredCycles = tempFilteredCycles.filter(cycle =>
        Number(cycle.stepId) == this.cycleRequest.stepId
      );
    }

    this.filteredCycles = tempFilteredCycles;
    this.paginate();
  }

  paginate(): void {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    this.paginatedCycles = this.filteredCycles.slice(start, end);
  }

  changePage(page: number): void {
    this.currentPage = page;
    this.paginate();
  }

  totalPages(): number {
    return Math.ceil(this.filteredCycles.length / this.itemsPerPage);
  }

  sortBy(key: keyof CycleTime): void {
    this.filteredCycles.sort((a, b) => {
      const valueA = a[key] ?? '';
      const valueB = b[key] ?? '';
      if (typeof valueA === 'string' && typeof valueB === 'string') {
        return valueA.localeCompare(valueB);
      }
      return valueA < valueB ? -1 : valueA > valueB ? 1 : 0;
    });
    this.paginate();
  }

  onAttemptSelected(index: number): void {
    this.selectedAttemptIndex = index;
    const attempt = this.recordedAttempts[index];

    this.selectedProject = this.projects.find(p => p.id === attempt.projectId) || {};
    this.cycleRequest.projectId = attempt.projectId;

    if (this.selectedProject.variants) {
      this.variants = this.selectedProject.variants;
    } else {
      this.variants = [];
    }

    this.selectedVariant = this.variants.find(v => v.id === attempt.variantId) || {};
    this.cycleRequest.variantId = attempt.variantId;

    if (this.selectedVariant.productionLineIds) {
      this.getProductionLines();
    } else {
      this.lines = [];
    }

    setTimeout(() => {
      this.selectedLine = this.lines.find(l => l.id === attempt.lineId) || {};
      this.cycleRequest.lineId = attempt.lineId;

      if (this.selectedLine.machines) {
        this.selectedMachine = this.selectedLine.machines.find(m => m.id === attempt.machineId) || {};
        this.machineModel.id = attempt.machineId;
        this.getSteps();
      } else {
        this.selectedMachine = {};
        this.machineModel.id = undefined;
        this.steps = [];
      }

      this.cycleRequest.stepId = attempt.stepId;
    }, 50);
  }

  updateTheoriticalCycleTime(): void {
    let totalSeconds = 0;
    let totalNano = 0;

    if (this.durationMinutes !== null) {
      totalSeconds += this.durationMinutes * 60;
    }
    if (this.durationSeconds !== null) {
      totalSeconds += this.durationSeconds;
    }
    if (this.durationMilliseconds !== null) {
      totalNano += this.durationMilliseconds * 1_000_000;
    }

    if (!this.cycleRequest.theoriticalCycleTime) {
      this.cycleRequest.theoriticalCycleTime = {};
    }

    this.cycleRequest.theoriticalCycleTime.seconds = totalSeconds;
    this.cycleRequest.theoriticalCycleTime.nano = totalNano;

    this.cycleRequest.theoriticalCycleTime.zero = (totalSeconds === 0 && totalNano === 0);
    this.cycleRequest.theoriticalCycleTime.negative = false;
    this.cycleRequest.theoriticalCycleTime.units = [
      {
        durationEstimated: true,
        timeBased: true,
        dateBased: true
      }
    ];
  }

  // New: Method to update clientCycleTime
  updateClientCycleTime(): void {
    let totalSeconds = 0;
    let totalNano = 0;

    if (this.clientDurationMinutes !== null) {
      totalSeconds += this.clientDurationMinutes * 60;
    }
    if (this.clientDurationSeconds !== null) {
      totalSeconds += this.clientDurationSeconds;
    }
    if (this.clientDurationMilliseconds !== null) {
      totalNano += this.clientDurationMilliseconds * 1_000_000;
    }

    if (!this.cycleRequest.clientCycleTime) {
      this.cycleRequest.clientCycleTime = {};
    }

    this.cycleRequest.clientCycleTime.seconds = totalSeconds;
    this.cycleRequest.clientCycleTime.nano = totalNano;

    this.cycleRequest.clientCycleTime.zero = (totalSeconds === 0 && totalNano === 0);
    this.cycleRequest.clientCycleTime.negative = false;
    this.cycleRequest.clientCycleTime.units = [
      {
        durationEstimated: true,
        timeBased: true,
        dateBased: true
      }
    ];
  }


  loadDurationFields(): void {
    const mcTime = this.cycleRequest.theoriticalCycleTime;
    const clcTime = this.cycleRequest.clientCycleTime;
    if (mcTime?.seconds !== undefined && mcTime?.nano !== undefined) {
      let totalSeconds = mcTime.seconds;
      let totalNano = mcTime.nano;

      this.durationMinutes = Math.floor(totalSeconds / 60);
      this.durationSeconds = totalSeconds % 60;
      this.durationMilliseconds = Math.floor(totalNano / 1_000_000);
    } else {
      this.durationMinutes = null;
      this.durationSeconds = null;
      this.durationMilliseconds = null;
    }

    if (clcTime?.seconds !== undefined && clcTime?.nano !== undefined) {
      let totalSeconds = clcTime.seconds;
      let totalNano = clcTime.nano;

      this.clientDurationMinutes = Math.floor(totalSeconds / 60);
      this.clientDurationSeconds = totalSeconds % 60;
      this.clientDurationMilliseconds = Math.floor(totalNano / 1_000_000);
    } else {
      this.clientDurationMinutes = null;
      this.clientDurationSeconds = null;
      this.clientDurationMilliseconds = null;
    }
  }

  viewDetails(id: number | undefined) {
    this.router.navigate(['/cycle-times/cycle-time-details',id]);
  }
}
