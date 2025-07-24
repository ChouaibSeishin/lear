import { Component, OnInit } from '@angular/core';
import { ChartData, ChartOptions, ChartDataset } from 'chart.js';
import { CycleTime } from '../../services/api-cycletime/models/cycle-time';
import { CycleTimeControllerService } from '../../services/api-cycletime/services/cycle-time-controller.service';
import { ProjectResponse } from '../../services/api-project/models/project-response';
import { VariantResponse } from '../../services/api-project/models/variant-response';
import { ProjectControllerService } from '../../services/api-project/services/project-controller.service';
import { VariantControllerService } from '../../services/api-project/services/variant-controller.service';
import { forkJoin } from 'rxjs';
import {ProductionLineResponse} from "../../services/api-machine/models/production-line-response";
import {ProductionLineControllerService} from "../../services/api-machine/services/production-line-controller.service";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
  standalone: false,
})
export class DashboardComponent implements OnInit {
  cycleTimes: CycleTime[] = [];
  availableProjects: ProjectResponse[] = [];
  availableVariants: VariantResponse[] = [];
  availableLines: ProductionLineResponse[] = []; // New property to store all production lines
  selectedProject: ProjectResponse | undefined = undefined;
  selectedVariant: VariantResponse | undefined = undefined;
  startDate: string | undefined = undefined; // New: Filter by start date
  endDate: string | undefined = undefined; // New: Filter by end date

  // KPI values
  totalCycles = 0;
  avgDuration = 0;
  stdDevDuration = 0;
  minDuration = 0;
  maxDuration = 0;
  manualPct = 0;
  onTimePct = 0;
  totalCyclesPendingStatus = 0;
  totalProductionLinesByProject = 0;

  // Formatted KPI values
  avgFormattedDuration = '';
  stdDevFormattedDuration = '';
  minFormattedDuration = '';
  maxFormattedDuration = '';

  // Chart data
  lineChartData: ChartData<'line'> = { labels: [], datasets: [] };
  histData: ChartData<'bar'> = { labels: [], datasets: [] };
  barByStepData: ChartData<'bar'> = { labels: [], datasets: [] };
  projectLineDistributionData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  variantPieData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  avgCycleTimeByVariantChart: ChartData<'bar'> = { labels: [], datasets: [] };
  totalProductionLinesByProjectChart: ChartData<'bar'> = { labels: [], datasets: [] };

  chartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      tooltip: {
        callbacks: {
          label: (context) => {
            // Updated tooltip for the new chart logic
            if (context.dataset['label'] && context.dataset['label'].includes('Cycle Count')) {
              return `Date: ${context.label}, Count: ${context.raw}`;
            }
            if (context.dataset['label'] === 'Avg Duration' ||
              context.dataset['label'] === 'Avg Cycle Time by Variant') {
              return `Duration: ${this.formatDuration(Number(context.raw))}`;
            }
            if (context.dataset['label'] === 'Count') {
              return `Count: ${context.raw}`;
            }
            return `${context.label}: ${context.raw}`;
          },
        },
      },
    },
  };

  constructor(
    private cycleSvc: CycleTimeControllerService,
    private projectSvc: ProjectControllerService,
    private variantSvc: VariantControllerService,
    private lineSvc: ProductionLineControllerService
  ) {}

  ngOnInit(): void {
    // Fetch all cycle times
    this.cycleSvc.getAll().subscribe((data) => {
      this.cycleTimes = data;

      // Get unique project IDs from cycle times
      const uniqueProjectIds = [...new Set(data.map(c => c.projectId).filter(id => id != null))];
      const projectRequests = uniqueProjectIds.map(id => this.projectSvc.getProject({ id: id! }));

      // Fetch all projects and all production lines in parallel
      forkJoin([
        forkJoin(projectRequests), // Projects
        this.lineSvc.getAllProductionLines() // All production lines
      ]).subscribe(([projects, lines]: [ProjectResponse[], ProductionLineResponse[]]) => {
        this.availableProjects = [...projects].sort((a, b) => (a.name || '').localeCompare(b.name || ''));
        this.availableLines = [...lines].sort((a, b) => (a.name || '').localeCompare(b.name || '')); // Store all lines

        // Get unique variant IDs from cycle times
        const uniqueVariantIds = [...new Set(data.map(c => c.variantId).filter(id => id != null))];
        const variantRequests = uniqueVariantIds.map(id => this.variantSvc.getVariant({ id: id! }));

        // Fetch all variants in parallel
        forkJoin(variantRequests).subscribe((variants: VariantResponse[]) => {
          this.availableVariants = [...variants].sort((a, b) => (a.name || '').localeCompare(b.name || ''));
          this.applyFilters(); // Apply filters once all data is loaded
        });
      });
    });
  }

  filteredCycleTimes(): CycleTime[] {
    return this.cycleTimes.filter(cycle => {
      const projectMatch = !this.selectedProject || cycle.projectId === this.selectedProject.id;
      const variantMatch = !this.selectedVariant || cycle.variantId === this.selectedVariant.id;

      // New: Date filtering
      let dateMatch = true;
      if (this.startDate && cycle.startTime) {
        const startDateTime = new Date(this.startDate).getTime();
        const cycleStartTime = new Date(cycle.startTime).getTime();
        dateMatch = dateMatch && (cycleStartTime >= startDateTime);
      }
      if (this.endDate && cycle.endTime) {
        const endDateTime = new Date(this.endDate).getTime();
        const cycleEndTime = new Date(cycle.endTime).getTime();
        dateMatch = dateMatch && (cycleEndTime <= endDateTime);
      }

      return projectMatch && variantMatch && dateMatch;
    });
  }

  availableVars(projectId: number | undefined) {
    if (projectId) {
      this.variantSvc.getVariantsByProject({ projectId: projectId }).subscribe(
        variants => {
          this.availableVariants = [...variants].sort((a, b) => (a.name || '').localeCompare(b.name || ''));
          this.selectedVariant = undefined;
          this.applyFilters();
        }
      );
    } else {
      const uniqueVariantIds = [...new Set(this.cycleTimes.map(c => c.variantId).filter(id => id != null))];
      const variantRequests = uniqueVariantIds.map(id => this.variantSvc.getVariant({ id: id! }));

      forkJoin(variantRequests).subscribe((variants: VariantResponse[]) => {
        this.availableVariants = [...variants].sort((a, b) => (a.name || '').localeCompare(b.name || ''));
        this.selectedVariant = undefined;
        this.applyFilters();
      });
    }
  }

  private computeKpis(data: CycleTime[]) {
    const durations = data.map((c) => c.duration || 0);
    this.totalCycles = durations.length;

    if (!this.totalCycles) {
      this.avgDuration = 0;
      this.minDuration = 0;
      this.maxDuration = 0;
      this.stdDevDuration = 0;
      this.manualPct = 0;
      this.onTimePct = 0;
      this.totalCyclesPendingStatus = 0;
      this.totalProductionLinesByProject = 0;
      this.avgFormattedDuration = '';
      this.stdDevFormattedDuration = '';
      this.minFormattedDuration = '';
      this.maxFormattedDuration = '';
      return;
    }

    const sum = durations.reduce((a, b) => a + b, 0);
    this.avgDuration = sum / this.totalCycles;
    this.minDuration = Math.min(...durations);
    this.maxDuration = Math.max(...durations);

    const variance =
      durations.reduce((a, d) => a + Math.pow(d - this.avgDuration, 2), 0) / this.totalCycles;
    this.stdDevDuration = Math.sqrt(variance);

    const manualCount = data.filter((c) => c.isManual).length;
    this.manualPct = (manualCount / this.totalCycles) * 100;

    const onTimeCount = data.filter((c) => c.status === 'VALID').length;
    this.onTimePct = (onTimeCount / this.totalCycles) * 100;

    this.totalCyclesPendingStatus = data.filter((c) => c.status === 'PENDING').length;

    if (this.selectedProject && this.selectedProject.productionLineIds) {
      this.totalProductionLinesByProject = this.selectedProject.productionLineIds.length;
    } else {
      this.totalProductionLinesByProject = this.availableProjects.reduce((sum, p) => sum + (p.productionLineIds?.length || 0), 0);
    }

    this.avgFormattedDuration = this.formatDuration(this.avgDuration);
    this.stdDevFormattedDuration = this.formatDuration(this.stdDevDuration);
    this.minFormattedDuration = this.formatDuration(this.minDuration);
    this.maxFormattedDuration = this.formatDuration(this.maxDuration);
  }

  // Updated method: Tracks frequency of Cycle Time Count over Time (per project or total)
  private buildCycleTimeCountOverTime(data: CycleTime[]) {
    const dailyCountsMap = new Map<string, number>(); // Date string (YYYY-MM-DD) -> total cycle count for that day

    // Group data by date
    data.forEach(c => {
      if (c.startTime) {
        const date = new Date(c.startTime).toISOString().split('T')[0]; // YYYY-MM-DD format
        dailyCountsMap.set(date, (dailyCountsMap.get(date) || 0) + 1);
      }
    });

    // Get all unique dates and sort them
    const sortedDates = Array.from(dailyCountsMap.keys()).sort();

    const datasets: ChartDataset<any>[] = [];
    const chartData: number[] = [];

    sortedDates.forEach(date => {
      chartData.push(dailyCountsMap.get(date) || 0);
    });

    let label = 'Total Cycle Count';
    if (this.selectedProject && this.selectedVariant) {
      label = `Cycle Count for ${this.selectedProject.name} -> ${this.selectedVariant.name}`;
    } else if (this.selectedProject) {
      label = `Cycle Count for ${this.selectedProject.name}`;
    } else if (this.selectedVariant) {
      label = `Cycle Count for ${this.selectedVariant.name}`;
    } else {
      label = 'Total Cycle Count (All Projects & Variants)';
    }

    datasets.push({
      data: chartData,
      label: label,
      fill: false,
      backgroundColor:'rgba(247,77,108,0.9)',
      borderColor: 'rgba(247,77,108,0.9)',
      tension: 0.1
    });

    this.lineChartData = {
      labels: sortedDates,
      datasets: datasets
    };
  }


  private buildHistogram(data: CycleTime[]) {
    const buckets = new Map<string, number>();
    data.forEach((c) => {
      const sec = Math.floor((c.duration || 0) / 1000);
      const b = Math.floor(sec / 10) * 10;
      const key = `${b}-${b + 9}s`;
      buckets.set(key, (buckets.get(key) || 0) + 1);
    });

    const labels = (Array.from(buckets.keys()) as string[]).sort((a, b) => {
      const startA = parseInt(a.split('-')[0]);
      const startB = parseInt(b.split('-')[0]);
      return startA - startB;
    });
    const chartData = labels.map((l) => buckets.get(l)!);

    this.histData = {
      labels,
      datasets: [{ data: chartData, label: 'Count', backgroundColor: '#66BB6A' }] as ChartDataset<'bar', number[]>[]
    };
  }

  private buildBarByStep(data: CycleTime[]) {
    const map = new Map<number, number[]>();
    data.forEach((c) => {
      if (c.stepId != null && c.duration != null) {
        const key = c.stepId;
        (map.get(key) || map.set(key, []).get(key)!).push(c.duration);
      }
    });

    const labels = (Array.from(map.keys()) as number[]).sort((a, b) => a - b).map((id) => `Step ${id}`);
    const avgDurations = (Array.from(map.keys()) as number[]).sort((a, b) => a - b).map(key => {
      const arr = map.get(key)!;
      return arr.reduce((a, b) => a + b, 0) / arr.length;
    });

    this.barByStepData = {
      labels,
      datasets: [{ data: avgDurations, label: 'Avg Duration', backgroundColor: '#FFA726' }] as ChartDataset<'bar', number[]>[]
    };
  }

  private buildProjectLineDistribution() {
    const lineNameToProjectCount = new Map<string, number>();

    this.availableProjects.forEach(p => {
      const lineIds = p.productionLineIds || [];
      if (lineIds.length === 0) {
        const label = 'No Line Assigned';
        lineNameToProjectCount.set(label, (lineNameToProjectCount.get(label) || 0) + 1);
      } else {
        lineIds.forEach(id => {
          const line = this.availableLines.find(l => l.id === id);
          const label = line?.name || `Line ${id}`;
          lineNameToProjectCount.set(label, (lineNameToProjectCount.get(label) || 0) + 1);
        });
      }
    });

    const labels = Array.from(lineNameToProjectCount.keys()).sort();
    const data = labels.map(label => lineNameToProjectCount.get(label)!);

    this.projectLineDistributionData = {
      labels,
      datasets: [{
        data: data as number[],
        label: 'Projects by Line',
        backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'],
      }] as ChartDataset<'doughnut', number[]>[],
    };
  }

  private buildAvgCycleTimeByVariant(data: CycleTime[]) {
    const map = new Map<number, number[]>();

    data.forEach(c => {
      if (c.variantId != null && c.duration != null) {
        (map.get(c.variantId) || map.set(c.variantId, []).get(c.variantId)!).push(c.duration);
      }
    });

    const labels: string[] = [];
    const avgDurations: number[] = [];

    const sortedVariantIds = Array.from(map.keys()) as number[];
    sortedVariantIds.sort((a, b) => a - b);

    sortedVariantIds.forEach(variantId => {
      const variant = this.availableVariants.find(v => v.id === variantId);
      const name = variant?.name || `Variant ${variantId}`;
      labels.push(name);
      const durations = map.get(variantId)!;
      const avg = durations.reduce((a, b) => a + b, 0) / durations.length;
      avgDurations.push(avg);
    });

    this.avgCycleTimeByVariantChart = {
      labels,
      datasets: [{ data: avgDurations, label: 'Avg Cycle Time by Variant', backgroundColor: '#78909C' }] as ChartDataset<'bar', number[]>[]
    };
  }

  private buildVariantPieDistribution(data: CycleTime[]) {
    const map = new Map<number, number>();
    data.forEach(c => {
      if (c.variantId != null) {
        map.set(c.variantId, (map.get(c.variantId) || 0) + 1);
      }
    });

    const labels = (Array.from(map.keys()) as number[]).sort((a, b) => a - b).map(id => {
      const variant = this.availableVariants.find(v => v.id === id);
      return variant?.name || `Variant ${id}`;
    });
    const values = labels.map(label => {
      const variant = this.availableVariants.find(v => v.name === label);
      return map.get(variant?.id || -1) || 0;
    });

    this.variantPieData = {
      labels,
      datasets: [{
        data: values,
        label: 'Distribution by Variant',
        backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'],
      }] as ChartDataset<'doughnut', number[]>[],
    };
  }

  private buildTotalProductionLinesByProjectChart() {
    const labels: string[] = [];
    const lineCounts: number[] = [];

    const projectsToChart = this.selectedProject ? [this.selectedProject] : this.availableProjects;

    projectsToChart.forEach(p => {
      if (p.name) {
        labels.push(p.name);
        lineCounts.push(p.productionLineIds?.length || 0);
      }
    });

    this.totalProductionLinesByProjectChart = {
      labels,
      datasets: [{ data: lineCounts, label: 'Production Lines', backgroundColor: '#8D6E63' }] as ChartDataset<'bar', number[]>[],
    };
  }

  private formatDuration(millis: number): string {
    if (millis < 0 || isNaN(millis)) return '00:00:00.00';

    const totalSeconds = Math.floor(millis / 1000);
    const ms = Math.floor((millis % 1000) / 10);

    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    const hh = this.pad(hours, 2);
    const mm = this.pad(minutes, 2);
    const ss = this.pad(seconds, 2);
    const SS = this.pad(ms, 2);

    return `${hh}:${mm}:${ss}.${SS}`;
  }

  private pad(n: number, width: number): string {
    return n.toString().padStart(width, '0');
  }

  onFilterChange() {
    this.applyFilters();
  }

  private applyFilters() {
    const filtered = this.filteredCycleTimes();

    this.computeKpis(filtered);
    this.buildCycleTimeCountOverTime(filtered); // Renamed method call
    this.buildHistogram(filtered);
    this.buildBarByStep(filtered);
    this.buildProjectLineDistribution();
    this.buildAvgCycleTimeByVariant(filtered);
    this.buildVariantPieDistribution(filtered);
    this.buildTotalProductionLinesByProjectChart();
  }
}
