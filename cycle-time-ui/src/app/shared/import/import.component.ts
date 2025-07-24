import { Component, OnInit } from '@angular/core';
import { ImportControllerService } from '../../services/api-import/services/import-controller.service';
import { ImportReport } from "../../services/api-import/models/import-report";
import { ImportRowError } from "../../services/api-import/models/import-row-error";
import {ImportExcel$Params} from "../../services/api-import/fn/import-controller/import-excel";

interface FullImportReport {
  [sheetName: string]: ImportReport;
}

@Component({
  selector: 'app-import',
  standalone: false,
  templateUrl: './import.component.html',
  styleUrls: ['./import.component.css'] // Corrected: use styleUrls (plural)
})
export class ImportComponent implements OnInit {
  selectedFiles: File[] = [];
  uploadProgress: { [key: number]: number } = {}; // Progress for each file
  isDragging: boolean = false;
  isUploading: boolean = false; // Overall uploading state

  overallUploadMessage: string = '';
  overallUploadSuccess: boolean = false;

  fullImportReports: { [key: number]: FullImportReport } = {};

  constructor(private importService: ImportControllerService) { }

  ngOnInit(): void {
  }

  /**
   * Handles file selection via click
   * @param event The change event from the file input
   */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.addFiles(Array.from(input.files));
    }
    // Reset the input value to allow selecting the same file(s) again after removal
    input.value = '';
  }

  /**
   * Handles dragover event on the drop zone
   * @param event The DragEvent
   */
  onDragOver(event: DragEvent): void {
    event.preventDefault(); // Prevent default to allow drop
    event.stopPropagation();
    this.isDragging = true;
  }

  /**
   * Handles dragleave event on the drop zone
   * @param event The DragEvent
   */
  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  /**
   * Handles drop event on the drop zone
   * @param event The DragEvent
   */
  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;

    if (event.dataTransfer && event.dataTransfer.files) {
      this.addFiles(Array.from(event.dataTransfer.files));
    }
  }

  /**
   * Adds files to the selectedFiles array after basic validation
   * @param files The array of files to add
   */
  private addFiles(files: File[]): void {
    this.resetUploadState(); // Clear all previous states before adding new files

    files.forEach(file => {
      // Basic file type and size validation
      if (!file.name.endsWith('.xls') && !file.name.endsWith('.xlsx')) {
        this.overallUploadMessage = `Error: File '${file.name}' is not a valid Excel file. Only .xls and .xlsx are allowed.`;
        this.overallUploadSuccess = false;
        return; // Don't add this file
      }
      const MAX_FILE_SIZE_MB = 5;
      if (file.size > MAX_FILE_SIZE_MB * 1024 * 1024) {
        this.overallUploadMessage = `Error: File '${file.name}' is too large (max ${MAX_FILE_SIZE_MB}MB).`;
        this.overallUploadSuccess = false;
        return; // Don't add this file
      }

      this.selectedFiles.push(file);
    });

    if (this.selectedFiles.length > 0) {
      this.overallUploadMessage = `Selected ${this.selectedFiles.length} file(s) for upload.`;
      this.overallUploadSuccess = true;
    }
  }

  /**
   * Resets the component's state related to file uploads and reports.
   */
  private resetUploadState(): void {
    this.selectedFiles = [];
    this.uploadProgress = {};
    this.isUploading = false;
    this.overallUploadMessage = '';
    this.overallUploadSuccess = false;
    this.fullImportReports = {};
  }

  /**
   * Removes a file from the selected list
   * @param index The index of the file to remove
   */
  removeFile(index: number): void {
    this.selectedFiles.splice(index, 1);
    delete this.uploadProgress[index];
    delete this.fullImportReports[index]; // Remove its report too

    if (this.selectedFiles.length === 0) {
      this.resetUploadState(); // Reset everything if no files are left
    } else {
      // Re-evaluate overall status if some files remain
      let completedFiles = 0;
      for (const key in this.uploadProgress) {
        if (this.uploadProgress[key] === 100) {
          completedFiles++;
        }
      }
      if (this.isUploading) { // Only re-check if an upload was in progress
        this.checkOverallUploadStatus(completedFiles);
      } else {
        this.overallUploadMessage = `Selected ${this.selectedFiles.length} file(s) for upload.`;
        this.overallUploadSuccess = true;
      }
    }
  }

  /**
   * Initiates the file upload process for all selected files
   */
  uploadFiles(): void {
    if (this.selectedFiles.length === 0) {
      this.overallUploadMessage = 'Please select at least one Excel file to upload.';
      this.overallUploadSuccess = false;
      return;
    }

    this.isUploading = true; // Set overall loading state
    this.overallUploadMessage = 'Uploading and processing files... This may take a moment.';
    this.overallUploadSuccess = true; // Assume success initially for optimistic UI

    let completedFileCount = 0; // Tracks how many files have finished their upload attempt

    this.selectedFiles.forEach((file, index) => {
      this.uploadProgress[index] = 0; // Initialize progress for this file

      const params:ImportExcel$Params = {
        body:{
          file: file as File

        }
      };

      this.importService.importExcel(params).subscribe({
        next: (report: FullImportReport) => { // Explicitly type 'report'
          this.uploadProgress[index] = 100;
          this.fullImportReports[index] = report;

          completedFileCount++;
          this.checkOverallUploadStatus(completedFileCount);
        },
        error: (error) => {
          this.uploadProgress[index] = 0;

          // FIX: Ensure 'BackendError' object fully conforms to ImportReport interface
          const errorReport: FullImportReport = {
            "BackendError": { // Use a specific sheet name for backend errors
              sheetName: "BackendError",
              success: false, // Set to false for backend errors
              overallMessage: `File upload and backend processing failed: ${error.message || 'Unknown error.'}`,
              totalRowsProcessed: 0,
              successfulImports: 0,
              skippedRows: 0,
              failedImports: 0, // <--- ADDED THIS FIELD to satisfy ImportReport interface
              errors: [{ // This matches the updated ImportRowError structure
                rowNumber: 0,
                status: "Failed",
                message: `Could not connect to the server or backend processing failed for '${file.name}'. Details: ${error.message || 'Unknown error.'}`, // Use 'message' to match ImportRowError
                entityType: "File Upload", // Use 'entityType' to match updated ImportRowError
                entityIdentifier: file.name,
                fieldName: "N/A",
                problematicValue: "N/A",
                errorCode: "BACKEND_UPLOAD_ERROR"
              }]
            }
          };
          this.fullImportReports[index] = errorReport;

          console.error(`Upload failed for file ${file.name}:`, error);

          completedFileCount++;
          this.checkOverallUploadStatus(completedFileCount);
        }
      });
    });
  }

  /**
   * Checks if all files have completed their upload/processing attempts.
   * Updates the overall upload message and state.
   * @param completedCount The number of files that have completed their processing.
   */
  private checkOverallUploadStatus(completedCount: number): void {
    if (completedCount === this.selectedFiles.length) {
      this.isUploading = false;

      let anyFileHadBackendErrors = false;
      let anyFileHadProcessingErrors = false; // Errors within any sheet of any report
      let anyFileHadSkips = false; // Skips within any sheet of any report

      for (const fileIndex in this.fullImportReports) {
        if (this.fullImportReports.hasOwnProperty(fileIndex)) {
          const report = this.fullImportReports[fileIndex];

          if (report && report["BackendError"]) { // Check for our custom backend error indicator
            anyFileHadBackendErrors = true;
            continue; // This file had a fundamental upload/backend error
          }

          // Iterate through actual sheet reports
          for (const sheetName in report) {
            if (report.hasOwnProperty(sheetName)) {
              const sheetReport = report[sheetName];
              // FIX: Use 'sheetReport.success' (boolean) instead of 'sheetReport.successfulImports' (number)
              if (sheetReport.success === false) { // <--- CORRECTED THIS LINE
                anyFileHadProcessingErrors = true;
              }
              if (sheetReport.skippedRows > 0) {
                anyFileHadSkips = true;
              }
            }
          }
        }
      }

      // Determine the final overall message and success status
      if (anyFileHadBackendErrors) {
        this.overallUploadMessage = 'One or more files failed to upload or connect to the backend. Please check your network and try again.';
        this.overallUploadSuccess = false;
      } else if (anyFileHadProcessingErrors) {
        this.overallUploadMessage = 'Some files processed with errors. Please review the detailed reports below for specific issues.';
        this.overallUploadSuccess = false;
      } else if (anyFileHadSkips) {
        this.overallUploadMessage = 'All files uploaded successfully! Some rows were skipped or had warnings. Please review the detailed reports below.';
        this.overallUploadSuccess = true; // Still considered successful overall, but with warnings (can be styled differently)
      } else {
        this.overallUploadMessage = 'All selected Excel files processed successfully with no issues!';
        this.overallUploadSuccess = true;
      }
    }
  }

  /**
   * Helper to get sheet names for ngFor order within a single FullImportReport
   * Sorts sheets based on a preferred order and then alphabetically.
   * Excludes the custom "BackendError" sheet from the display order for regular sheets.
   */
  getReportSheetNames(report: FullImportReport): string[] {
    if (!report) return [];
    // Define a preferred order for your sheets
    const preferredOrder = [
      "ProductionLines",
      "Projects",
      "Variants",
      "Machines",
      "Steps",
      "CycleTimes"
    ];

    return Object.keys(report)
      .filter(sheetName => sheetName !== "BackendError") // Exclude our custom backend error sheet from normal sheet display
      .sort((a, b) => {
        const indexA = preferredOrder.indexOf(a);
        const indexB = preferredOrder.indexOf(b);

        if (indexA === -1 && indexB === -1) {
          return a.localeCompare(b); // Both not in preferred order, sort alphabetically
        }
        if (indexA === -1) {
          return 1; // A not in preferred order, B is, so B comes after A
        }
        if (indexB === -1) {
          return -1; // B not in preferred order, A is, so A comes before B
        }
        return indexA - indexB; // Sort by preferred order
      });
  }

  /**
   * Helper to check if a sheet report has errors (success: false or errors list is not empty)
   * @param sheetReport The ImportReport for a specific sheet.
   * @returns True if the sheet report indicates failure or contains errors.
   */
  hasSheetErrors(sheetReport: ImportReport): boolean {
    // FIX: Use 'sheetReport.success' (boolean) instead of 'sheetReport.successfulImports' (number)
    // Also, use optional chaining for errors array check
    return sheetReport.success === false || (sheetReport.errors?.length || 0) > 0; // <--- CORRECTED THIS LINE
  }

  /**
   * Helper to check if a sheet report has skips
   * @param sheetReport The ImportReport for a specific sheet.
   * @returns True if the sheet report indicates skipped rows.
   */
  hasSheetSkips(sheetReport: ImportReport): boolean {
    return sheetReport.skippedRows > 0;
  }

  /**
   * Helper to check if a report contains the BackendError sheet (meaning the entire upload failed for that file).
   * @param report The FullImportReport for a specific file.
   * @returns True if the report contains a 'BackendError' sheet.
   */
  isBackendErrorReport(report: FullImportReport): boolean {
    return !!report && !!report["BackendError"];
  }

  /**
   * Get the error details from the BackendError sheet.
   * @param report The FullImportReport for a specific file.
   * @returns The first ImportRowError from the 'BackendError' sheet, or undefined.
   */
  getBackendErrorDetails(report: FullImportReport): ImportRowError | undefined {
    return report && report["BackendError"] ? report["BackendError"].errors[0] : undefined;
  }
}
