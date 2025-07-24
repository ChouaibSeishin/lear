package org.lear.importservice.dtos;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ImportReport {
    private String sheetName;
    private boolean success;
    private String overallMessage;
    private int totalRowsProcessed;
    private int successfulImports;
    private int skippedRows;
    private List<ImportRowError> errors;

    public ImportReport() {
        this.errors = new ArrayList<>();
        this.success = true; // Assume success initially
        this.overallMessage = "Sheet processed successfully.";
    }

    // New addError methods for more specific error reporting
    public void addError(ImportRowError error) {
        this.errors.add(error);
        this.success = false; // Mark as failed if any error occurs
        this.overallMessage = "Sheet processed with errors or warnings. Please review the detailed report.";
    }

    // Overloaded addSkipped to accept more detail, treating skips as a type of warning/issue
    public void addSkipped(int rowNumber, String message, String entityType, String entityIdentifier, String fieldName, String problematicValue, String errorCode) {
        this.skippedRows++;
        this.errors.add(new ImportRowError(rowNumber, "Skipped", message, entityType, entityIdentifier, fieldName, problematicValue, errorCode));
        this.overallMessage = "Sheet processed with warnings or skipped rows. Please review the detailed report.";
    }

    // Original addSkipped for backward compatibility if needed, though the new one is preferred
    public void addSkipped(int rowNumber, String message, String entityType, String entityIdentifier) {
        this.skippedRows++;
        this.errors.add(new ImportRowError(rowNumber, "Skipped", message, entityType, entityIdentifier, "N/A", null, "SKIPPED_ROW"));
        this.overallMessage = "Sheet processed with warnings or skipped rows. Please review the detailed report.";
    }

    public void incrementSuccessfulImports() {
        this.successfulImports++;
    }

    // Call this at the end of processSheet to finalize the report status
    public void updateOverallStatus() {
        if (!this.errors.isEmpty() || this.skippedRows > 0) {
            this.success = false;
            this.overallMessage = "Sheet processed with errors or warnings. Please review the detailed report.";
        } else {
            this.success = true;
            this.overallMessage = "Sheet processed successfully.";
        }
        if (this.totalRowsProcessed == 0 && this.successfulImports == 0 && this.skippedRows == 0 && this.errors.isEmpty()) {
            this.overallMessage = "Sheet '" + sheetName + "' was empty or contained no processable data.";
        }
    }
}
