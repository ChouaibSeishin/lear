import { Component, OnInit } from '@angular/core';
import { CycleTime } from "../../services/api-cycletime/models/cycle-time";
import { CycleTimeControllerService } from "../../services/api-cycletime/services/cycle-time-controller.service";
import { CycleTimeRequest } from "../../services/api-cycletime/models/cycle-time-request";
import { ActivatedRoute } from "@angular/router";
import { BreadcrumbService } from "../../services/breadcrumbs/breadcrumbs.service";
import {ToastrService} from "ngx-toastr";

@Component({
  selector: 'app-cycle-time-details',
  standalone: false,
  templateUrl: './cycle-time-details.component.html',
  styleUrls: ['./cycle-time-details.component.css']
})
export class CycleTimeDetailsComponent implements OnInit {

  cycleTimeId!: number;
  // This will ALWAYS hold theoreticalCycleTime and clientCycleTime as OBJECTS for UI binding
  cycleTime: CycleTime = {};

  theoriticalDurationMinutes: number | null = null;
  theoriticalDurationSeconds: number | null = null;
  theoriticalDurationMilliseconds: number | null = null;

  // New properties for clientCycleTime
  clientDurationMinutes: number | null = null;
  clientDurationSeconds: number | null = null;
  clientDurationMilliseconds: number | null = null;

  constructor(
    private cycleTimeService: CycleTimeControllerService,
    private route: ActivatedRoute,
    private titleService: BreadcrumbService,
    private toaster:ToastrService
  ) {}

  ngOnInit(): void {
    this.cycleTimeId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.cycleTimeId) {
      this.cycleTimeService.getById({ id: this.cycleTimeId }).subscribe(cycle => {
          this.titleService.setCustomLabelForLast(cycle.id!.toString() || `Cycle ${this.cycleTimeId}`);

          // Populate all *other* properties of this.cycleTime first
          this.cycleTime = { // Re-initialize to ensure it's a fresh object
            id: cycle.id,
            endTime: cycle.endTime,
            formattedDuration:cycle.formattedDuration,
            isManual: cycle.isManual,
            lineId: cycle.lineId,
            machineId: cycle.machineId,
            projectId: cycle.projectId,
            recordType: cycle.recordType,
            startTime: cycle.startTime,
            status: cycle.status,
            stepId: cycle.stepId,
            userId: cycle.userId,
            variantId: cycle.variantId,
          };


          // Now, specifically handle theoriticalCycleTime
          if (typeof cycle.theoriticalCycleTime === 'string') {
            this.cycleTime.theoriticalCycleTime = this.parseIsoDuration(cycle.theoriticalCycleTime);
          } else if (cycle.theoriticalCycleTime) {
            this.cycleTime.theoriticalCycleTime = cycle.theoriticalCycleTime;
          } else {
            this.cycleTime.theoriticalCycleTime = { seconds: 0, nano: 0, zero: true, negative: false, units: [] };
          }

          // New: Specifically handle clientCycleTime
          if (typeof cycle.clientCycleTime === 'string') {
            this.cycleTime.clientCycleTime = this.parseIsoDuration(cycle.clientCycleTime);
          } else if (cycle.clientCycleTime) {
            this.cycleTime.clientCycleTime = cycle.clientCycleTime;
          } else {
            this.cycleTime.clientCycleTime = { seconds: 0, nano: 0, zero: true, negative: false, units: [] };
          }

          // Load the duration fields from the parsed/existing cycle time into the UI inputs
          this.loadTheoriticalDurationFields();
          this.loadClientDurationFields(); // New: Load client duration fields
        },
        (error) => {
          console.error('Error fetching cycle time details:', error);
          this.toaster.error('Failed to load cycle time details.');
        });
    }
  }

  // Helper to parse ISO 8601 Duration String (e.g., "PT7S", "PT7.030S") into seconds and nano
  private parseIsoDuration(isoDuration: string): { seconds?: number; nano?: number; zero?: boolean; negative?: boolean; units?: Array<{durationEstimated?: boolean; timeBased?: boolean; dateBased?: boolean;}> } {
    const regex = /PT(?:(\d+)M)?(?:(\d+(?:[.,]\d+)?)S)?/;
    const matches = isoDuration.match(regex);

    if (!matches) {
      console.warn('Could not parse ISO duration string:', isoDuration);
      return { seconds: 0, nano: 0, zero: true, negative: false, units: [] };
    }

    let totalSeconds = 0;
    let totalNano = 0;

    const minutesMatch = matches[1];
    const secondsWithDecimal = matches[2];

    if (minutesMatch) {
      totalSeconds += parseInt(minutesMatch, 10) * 60;
    }

    if (secondsWithDecimal) {
      const parts = secondsWithDecimal.replace(',', '.').split('.');
      totalSeconds += parseInt(parts[0] || '0', 10);

      if (parts.length > 1) {
        const fractionalPart = parts[1].padEnd(9, '0').substring(0, 9);
        totalNano = parseInt(fractionalPart, 10);
      }
    }

    return {
      seconds: totalSeconds,
      nano: totalNano,
      zero: (totalSeconds === 0 && totalNano === 0),
      negative: isoDuration.startsWith('-'),
      units: []
    };
  }

  // Helper to convert seconds and nano from UI back to ISO 8601 Duration String
  private formatDurationToISO(seconds: number, nano: number): string {
    if (seconds === 0 && nano === 0) {
      return "PT0S";
    }

    let result = "PT";
    let remainingSeconds = seconds;

    const minutes = Math.floor(remainingSeconds / 60);
    if (minutes > 0) {
      result += `${minutes}M`;
      remainingSeconds %= 60;
    }

    let fractionalSeconds = (nano / 1_000_000_000);
    const totalSecondsWithFraction = remainingSeconds + fractionalSeconds;
    const formattedS = totalSecondsWithFraction.toFixed(3);

    const cleanedS = formattedS.replace(/\.000$/, '');
    const finalS = cleanedS.replace(/(\.\d*?)0+$/, '$1').replace(/\.$/, '');

    if (finalS !== '') {
      result += `${finalS}S`;
    }

    if (result === "PT" && minutes === 0 && (remainingSeconds > 0 || nano > 0)) {
      result = `PT${finalS}S`;
    } else if (result === "PT") {
      return "PT0S";
    }

    return result;
  }

  // Updates the internal `cycleTime.theoriticalCycleTime` object based on UI input fields
  updateTheoriticalCycleTime(): void {
    let totalSeconds = 0;
    let totalNano = 0;

    if (this.theoriticalDurationMinutes !== null) {
      totalSeconds += this.theoriticalDurationMinutes * 60;
    }
    if (this.theoriticalDurationSeconds !== null) {
      totalSeconds += this.theoriticalDurationSeconds;
    }
    if (this.theoriticalDurationMilliseconds !== null) {
      totalNano += this.theoriticalDurationMilliseconds * 1_000_000;
    }

    if (typeof this.cycleTime.theoriticalCycleTime === 'string' || !this.cycleTime.theoriticalCycleTime) {
      this.cycleTime.theoriticalCycleTime = {};
    }

    this.cycleTime.theoriticalCycleTime.seconds = totalSeconds;
    this.cycleTime.theoriticalCycleTime.nano = totalNano;
    this.cycleTime.theoriticalCycleTime.zero = (totalSeconds === 0 && totalNano === 0);
    this.cycleTime.theoriticalCycleTime.negative = false;
    this.cycleTime.theoriticalCycleTime.units = this.cycleTime.theoriticalCycleTime.units ?? [];
  }

  // New: Updates the internal `cycleTime.clientCycleTime` object based on UI input fields
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

    if (typeof this.cycleTime.clientCycleTime === 'string' || !this.cycleTime.clientCycleTime) {
      this.cycleTime.clientCycleTime = {};
    }

    this.cycleTime.clientCycleTime.seconds = totalSeconds;
    this.cycleTime.clientCycleTime.nano = totalNano;
    this.cycleTime.clientCycleTime.zero = (totalSeconds === 0 && totalNano === 0);
    this.cycleTime.clientCycleTime.negative = false;
    this.cycleTime.clientCycleTime.units = this.cycleTime.clientCycleTime.units ?? [];
  }

  // Populates theoritical duration picker fields from the internal `cycleTime.theoriticalCycleTime` object
  loadTheoriticalDurationFields(): void {
    const mcTime = this.cycleTime.theoriticalCycleTime;

    if (mcTime && typeof mcTime === 'object' && mcTime.seconds !== undefined && mcTime.nano !== undefined) {
      let totalSeconds = mcTime.seconds;
      let totalNano = mcTime.nano;

      this.theoriticalDurationMinutes = Math.floor(totalSeconds / 60);
      this.theoriticalDurationSeconds = totalSeconds % 60;
      this.theoriticalDurationMilliseconds = Math.floor(totalNano / 1_000_000);
    } else {
      this.theoriticalDurationMinutes = null;
      this.theoriticalDurationSeconds = null;
      this.theoriticalDurationMilliseconds = null;
    }
  }

  // New: Populates client duration picker fields from the internal `cycleTime.clientCycleTime` object
  loadClientDurationFields(): void {
    const clientTime = this.cycleTime.clientCycleTime;

    if (clientTime && typeof clientTime === 'object' && clientTime.seconds !== undefined && clientTime.nano !== undefined) {
      let totalSeconds = clientTime.seconds;
      let totalNano = clientTime.nano;

      this.clientDurationMinutes = Math.floor(totalSeconds / 60);
      this.clientDurationSeconds = totalSeconds % 60;
      this.clientDurationMilliseconds = Math.floor(totalNano / 1_000_000);
    } else {
      this.clientDurationMinutes = null;
      this.clientDurationSeconds = null;
      this.clientDurationMilliseconds = null;
    }
  }

  // Converts the internal cycleTime object to the API's CycleTimeRequest format
  private convertToCycleTimeRequest(cycleTime: CycleTime): CycleTimeRequest {
    let theoriticalCycleTimeForRequest: string | undefined = undefined;
    let clientCycleTimeForRequest: string | undefined = undefined; // New

    // Convert the internal theoriticalCycleTime object to an ISO 8601 string for the request
    if (cycleTime.theoriticalCycleTime &&
      (cycleTime.theoriticalCycleTime.seconds !== undefined || cycleTime.theoriticalCycleTime.nano !== undefined) &&
      typeof cycleTime.theoriticalCycleTime !== 'string') {
      theoriticalCycleTimeForRequest = this.formatDurationToISO(
        cycleTime.theoriticalCycleTime.seconds ?? 0,
        cycleTime.theoriticalCycleTime.nano ?? 0
      );
    }

    // New: Convert the internal clientCycleTime object to an ISO 8601 string for the request
    if (cycleTime.clientCycleTime &&
      (cycleTime.clientCycleTime.seconds !== undefined || cycleTime.clientCycleTime.nano !== undefined) &&
      typeof cycleTime.clientCycleTime !== 'string') {
      clientCycleTimeForRequest = this.formatDurationToISO(
        cycleTime.clientCycleTime.seconds ?? 0,
        cycleTime.clientCycleTime.nano ?? 0
      );
    }

    // Return the request object, ensure duration fields are strings
    return {
      endTime: cycleTime.endTime,
      isManual: cycleTime.isManual,
      lineId: cycleTime.lineId,
      theoriticalCycleTime: theoriticalCycleTimeForRequest as any, // Cast as 'any' due to type mismatch with generated API model
      clientCycleTime: clientCycleTimeForRequest as any, // New: Cast as 'any'
      machineId: cycleTime.machineId,
      projectId: cycleTime.projectId,
      recordType: cycleTime.recordType,
      startTime: cycleTime.startTime,
      status: cycleTime.status,
      stepId: cycleTime.stepId,
      userId: cycleTime.userId,
      variantId: cycleTime.variantId,
    };
  }

  /**
   * Handles the update logic for the cycle time.
   * It converts the `cycleTime` object to a `CycleTimeRequest` and calls the service.
   */
  updateCycleTime(): void {
    if (this.cycleTime && this.cycleTime.id) {
      // 1. Ensure the internal cycleTime.theoriticalCycleTime and clientCycleTime objects are updated from UI inputs
      this.updateTheoriticalCycleTime();
      this.updateClientCycleTime(); // New: Update client cycle time

      // 2. Convert the full cycleTime object (which has duration objects)
      //    into the CycleTimeRequest format (which needs durations as strings).
      const cycleTimeRequest: CycleTimeRequest = this.convertToCycleTimeRequest(this.cycleTime);

      console.log('Sending update request with body:', JSON.stringify(cycleTimeRequest, null, 2));

      this.cycleTimeService.update({ id: this.cycleTime.id!, body: cycleTimeRequest }).subscribe(
        (updatedCycleTime) => {
          console.log('Cycle Time updated successfully:', updatedCycleTime);

          // IMPORTANT: Update this.cycleTime from the response, maintaining durations as OBJECTS
          // Assign other properties first
          this.cycleTime.id = updatedCycleTime.id;
          this.cycleTime.endTime = updatedCycleTime.endTime;
          this.cycleTime.formattedDuration = updatedCycleTime.formattedDuration;
          this.cycleTime.isManual = updatedCycleTime.isManual;
          this.cycleTime.lineId = updatedCycleTime.lineId;
          this.cycleTime.machineId = updatedCycleTime.machineId;
          this.cycleTime.projectId = updatedCycleTime.projectId;
          this.cycleTime.recordType = updatedCycleTime.recordType;
          this.cycleTime.startTime = updatedCycleTime.startTime;
          this.cycleTime.status = updatedCycleTime.status;
          this.cycleTime.stepId = updatedCycleTime.stepId;
          this.cycleTime.userId = updatedCycleTime.userId;
          this.cycleTime.variantId = updatedCycleTime.variantId;


          // Then specifically handle theoriticalCycleTime, parsing it to an object
          if (typeof updatedCycleTime.theoriticalCycleTime === 'string') {
            this.cycleTime.theoriticalCycleTime = this.parseIsoDuration(updatedCycleTime.theoriticalCycleTime);
          } else if (updatedCycleTime.theoriticalCycleTime) {
            this.cycleTime.theoriticalCycleTime = updatedCycleTime.theoriticalCycleTime;
          } else {
            this.cycleTime.theoriticalCycleTime = { seconds: 0, nano: 0, zero: true, negative: false, units: [] };
          }

          // New: Specifically handle clientCycleTime, parsing it to an object
          if (typeof updatedCycleTime.clientCycleTime === 'string') {
            this.cycleTime.clientCycleTime = this.parseIsoDuration(updatedCycleTime.clientCycleTime);
          } else if (updatedCycleTime.clientCycleTime) {
            this.cycleTime.clientCycleTime = updatedCycleTime.clientCycleTime;
          } else {
            this.cycleTime.clientCycleTime = { seconds: 0, nano: 0, zero: true, negative: false, units: [] };
          }

          this.loadTheoriticalDurationFields(); // Refresh UI inputs for theoritical
          this.loadClientDurationFields(); // New: Refresh UI inputs for client
          this.toaster.success('Cycle Time updated successfully!');
        },
        (error) => {
          console.error('Error updating cycle time:', error);
          const errorMessage = error.error && error.error.message ? error.error.message : 'Failed to update cycle time. Please check your input and try again.';
          alert(`Error: ${errorMessage}`);
        }
      );
    } else {
      console.warn('No cycle time selected for update or missing ID.');
      this.toaster.error('Cannot update: Cycle Time not selected or ID is missing.');
    }
  }
}
