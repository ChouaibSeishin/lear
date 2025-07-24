package org.lear.importservice.service;

import lombok.RequiredArgsConstructor;
import org.lear.importservice.dtos.*; // Ensure ImportReport and ImportRowError are in this package
import org.lear.importservice.feign.CycleTimeServiceClient;
import org.lear.importservice.feign.MachineServiceClient;
import org.lear.importservice.feign.ProjectServiceClient;
import org.lear.importservice.util.ExcelUtil;
import org.lear.importservice.util.ImportConstants;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import feign.FeignException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final ProjectServiceClient projectClient;
    private final MachineServiceClient machineClient;
    private final CycleTimeServiceClient cycleClient;
    private final IdResolverService idResolverService;


    public Map<String, ImportReport> importExcel(MultipartFile file) {
        Map<String, ImportReport> fullImportReport = new LinkedHashMap<>();

        // --- ORDER MATTERS! Production Lines must be processed before Projects/Machines that reference them ---
        processSheet(file, ImportConstants.SHEET_PRODUCTION_LINES, ProductionLineRow.class, this::importProductionLinesInternal, fullImportReport);
        processSheet(file, ImportConstants.SHEET_PROJECTS, ProjectRow.class, this::importProjectsInternal, fullImportReport);
        processSheet(file, ImportConstants.SHEET_VARIANTS, VariantRow.class, this::importVariantsInternal, fullImportReport);
        processSheet(file, ImportConstants.SHEET_MACHINES, MachineRow.class, this::importMachinesInternal, fullImportReport);
        processSheet(file, ImportConstants.SHEET_STEPS, StepRow.class, this::importStepsInternal, fullImportReport);
        processSheet(file, ImportConstants.SHEET_CYCLE_TIMES, CycleTimeRow.class, this::importCycleTimesInternal, fullImportReport);

        return fullImportReport;
    }

    private <T> void processSheet(MultipartFile file, String sheetName, Class<T> clazz,
                                  SheetImportProcessor<T> processor, Map<String, ImportReport> fullImportReport) {
        ImportReport report = new ImportReport();
        report.setSheetName(sheetName);
        fullImportReport.put(sheetName, report);

        try {
            List<T> rows = ExcelUtil.readSheet(file, sheetName, clazz);
            report.setTotalRowsProcessed(rows.size());

            if (rows.isEmpty()) {
                report.setOverallMessage("Sheet '" + sheetName + "' found but no data rows to process.");
                report.setSuccess(true);
                return;
            }

            for (int i = 0; i < rows.size(); i++) {
                T row = rows.get(i);
                int excelRowNumber = i + 2; // Data starts from row 2 (index 1 in list)
                try {
                    processor.processRow(row, report, excelRowNumber);
                } catch (Exception e) {
                    String entityType = sheetName.endsWith("s") ? sheetName.substring(0, sheetName.length() - 1) : sheetName;
                    // Provide a more specific entity identifier if possible, otherwise use row number
                    String rowIdentifier = "Row " + excelRowNumber;
                    report.addError(new ImportRowError(excelRowNumber, "Failed", "Unexpected error processing row: " + e.getMessage(), entityType, rowIdentifier, "Overall Row", String.valueOf(row), "UNEXPECTED_ERROR"));
                    System.err.println("Unhandled exception for row " + excelRowNumber + " in sheet " + sheetName + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IllegalArgumentException e) {
            report.addError(new ImportRowError(0, "Failed", "Sheet parsing error: " + e.getMessage(), "Sheet", sheetName, "N/A", null, "SHEET_PARSING_ERROR"));
            System.err.println("Error reading sheet " + sheetName + ": " + e.getMessage());
        } catch (RuntimeException e) { // Catch the RuntimeException rethrown by ExcelUtil for general read failures
            report.addError(new ImportRowError(0, "Failed", "File reading error for sheet '" + sheetName + "': " + e.getMessage(), "File", file.getOriginalFilename(), "N/A", null, "FILE_READING_ERROR"));
            System.err.println("General error during processing of " + sheetName + " sheet: " + e.getMessage());
            e.printStackTrace();
        } finally {
            report.updateOverallStatus();
        }
    }

    @FunctionalInterface
    private interface SheetImportProcessor<T> {
        void processRow(T row, ImportReport report, int excelRowNumber);
    }

    private void importProductionLinesInternal(ProductionLineRow row, ImportReport report, int excelRowNumber) {
        String lineName = row.getName();
        String entityIdentifier = (lineName != null && !lineName.trim().isEmpty()) ? lineName : "N/A (Empty Name)";

        if (lineName == null || lineName.trim().isEmpty()) {
            report.addSkipped(excelRowNumber, "Production Line name is empty or null. Cannot process.", "ProductionLine", entityIdentifier, "Name", null, "MISSING_REQUIRED_FIELD");
            return;
        }

        try {
            Optional<Long> existingLineId = idResolverService.getLineIdByName(lineName);
            Map<String, Object> body = new HashMap<>();
            body.put("name", lineName.trim());
            body.put("description", row.getDescription());

            if (existingLineId.isPresent()) {
                machineClient.updateLine(existingLineId.get(), body);
                report.incrementSuccessfulImports();
                System.out.println("Updated Production Line: " + lineName + " (ID: " + existingLineId.get() + ")");
            } else {
                machineClient.createLine(body);
                report.incrementSuccessfulImports();
                System.out.println("Created Production Line: " + lineName);
            }
        } catch (FeignException.Conflict e) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "Production Line '" + entityIdentifier + "' conflicts with existing data: " + getFeignErrorMessage(e), "ProductionLine", entityIdentifier, "Name", lineName, "API_CONFLICT"));
        } catch (FeignException e) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "API error for Production Line '" + entityIdentifier + "': Status " + e.status() + " - " + getFeignErrorMessage(e), "ProductionLine", entityIdentifier, "API Call", null, "API_ERROR"));
        } catch (Exception e) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "Error processing Production Line '" + entityIdentifier + "': " + e.getMessage(), "ProductionLine", entityIdentifier, "Overall Row", null, "UNEXPECTED_SAVE_ERROR"));
            e.printStackTrace();
        }
    }

    private void importProjectsInternal(ProjectRow row, ImportReport report, int excelRowNumber) {
        String projectName = row.getName();
        String entityIdentifier = (projectName != null && !projectName.trim().isEmpty()) ? projectName : "N/A (Empty Name)";

        if (projectName == null || projectName.trim().isEmpty()) {
            report.addSkipped(excelRowNumber, "Project name is empty or null. Cannot process.", "Project", entityIdentifier, "Name", null, "MISSING_REQUIRED_FIELD");
            return;
        }

        try {
            Optional<Long> existingProjectId = idResolverService.getProjectIdByName(projectName);
            Map<String, Object> body = new HashMap<>();
            body.put("name", projectName.trim());
            body.put("description", row.getDescription());

            Set<Long> lineIds = new HashSet<>();
            if (row.getProductionLines() != null) {
                for (String lineName : row.getProductionLines()) {
                    if (lineName != null && !lineName.trim().isEmpty()) {
                        Optional<Long> lineId = idResolverService.getLineIdByName(lineName);
                        if (lineId.isPresent()) {
                            lineIds.add(lineId.get());
                        } else {
                            report.addError(new ImportRowError(excelRowNumber, "Warning", "Production Line '" + lineName + "' not found. This line will not be linked.", "Project", entityIdentifier, "Production Lines", lineName, "DEPENDENCY_NOT_FOUND"));
                        }
                    } else {
                        report.addError(new ImportRowError(excelRowNumber, "Warning", "Empty production line name provided for project linkage.", "Project", entityIdentifier, "Production Lines", null, "EMPTY_LINKAGE_NAME"));
                    }
                }
            }
            body.put("productionLineIds", lineIds);

            if (existingProjectId.isPresent()) {
                projectClient.updateProject(existingProjectId.get(), body);
                report.incrementSuccessfulImports();
                System.out.println("Updated Project: " + projectName + " (ID: " + existingProjectId.get() + ")");
            } else {
                projectClient.createProject(body);
                report.incrementSuccessfulImports();
                System.out.println("Created Project: " + projectName);
            }
        } catch (FeignException.Conflict e) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "Project '" + entityIdentifier + "' conflicts with existing data: " + getFeignErrorMessage(e), "Project", entityIdentifier, "Name", projectName, "API_CONFLICT"));
        } catch (FeignException e) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "API error for Project '" + entityIdentifier + "': Status " + e.status() + " - " + getFeignErrorMessage(e), "Project", entityIdentifier, "API Call", null, "API_ERROR"));
        } catch (Exception e) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "Error processing Project '" + entityIdentifier + "': " + e.getMessage(), "Project", entityIdentifier, "Overall Row", null, "UNEXPECTED_SAVE_ERROR"));
            e.printStackTrace();
        }
    }

    private void importVariantsInternal(VariantRow row, ImportReport report, int excelRowNumber) {
        String variantName = row.getName();
        String projectName = row.getProjectName();
        String entityIdentifier = (variantName != null && !variantName.trim().isEmpty()) ? variantName : "N/A (Empty Name)";

        if (variantName == null || variantName.trim().isEmpty()) {
            report.addSkipped(excelRowNumber, "Variant name is empty or null. Cannot process.", "Variant", entityIdentifier, "Name", null, "MISSING_REQUIRED_FIELD");
            return;
        }
        if (projectName == null || projectName.trim().isEmpty()) {
            report.addSkipped(excelRowNumber, "Project name for variant '" + entityIdentifier + "' is empty or null.", "Variant", entityIdentifier, "Project Name", null, "MISSING_REQUIRED_FIELD");
            return;
        }

        try {
            Optional<Long> projectId = idResolverService.getProjectIdByName(projectName);
            if (projectId.isEmpty()) {
                report.addSkipped(excelRowNumber, "Project '" + projectName + "' not found for variant '" + entityIdentifier + "'.", "Variant", entityIdentifier, "Project Name", projectName, "DEPENDENCY_NOT_FOUND");
                return;
            }

            Optional<Long> existingVariantId = idResolverService.getVariantIdByName(variantName);
            Map<String, Object> body = new HashMap<>();
            body.put("name", variantName.trim());
            body.put("status", row.getStatus());
            body.put("projectId", projectId.get());

            Set<Long> lineIds = new HashSet<>();
            if (row.getProductionLines() != null) {
                for (String lineName : row.getProductionLines()) {
                    if (lineName != null && !lineName.trim().isEmpty()) {
                        Optional<Long> lineId = idResolverService.getLineIdByName(lineName);
                        if (lineId.isPresent()) {
                            lineIds.add(lineId.get());
                        } else {
                            report.addError(new ImportRowError(excelRowNumber, "Warning", "Production Line '" + lineName + "' not found.", "Variant", entityIdentifier, "Production Lines", lineName, "DEPENDENCY_NOT_FOUND"));
                        }
                    } else {
                        report.addError(new ImportRowError(excelRowNumber, "Warning", "Empty production line name provided.", "Variant", entityIdentifier, "Production Lines", null, "EMPTY_LINKAGE_NAME"));
                    }
                }
            }
            body.put("productionLineIds", lineIds);

            if (existingVariantId.isPresent()) {
                projectClient.updateVariant(existingVariantId.get(), body);
                report.incrementSuccessfulImports();
                System.out.println("Updated Variant: " + variantName + " (ID: " + existingVariantId.get() + ")");
            } else {
                projectClient.createVariant(body);
                report.incrementSuccessfulImports();
                System.out.println("Created Variant: " + variantName);
            }
        } catch (FeignException.Conflict e) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "Variant '" + entityIdentifier + "' conflicts with existing data: " + getFeignErrorMessage(e), "Variant", entityIdentifier, "Name", variantName, "API_CONFLICT"));
        } catch (FeignException e) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "API error for Variant '" + entityIdentifier + "': Status " + e.status() + " - " + getFeignErrorMessage(e), "Variant", entityIdentifier, "API Call", null, "API_ERROR"));
        } catch (Exception e) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "Error processing Variant '" + entityIdentifier + "': " + e.getMessage(), "Variant", entityIdentifier, "Overall Row", null, "UNEXPECTED_SAVE_ERROR"));
            e.printStackTrace();
        }
    }

    private void importMachinesInternal(MachineRow row, ImportReport report, int excelRowNumber) {
        String machineName = row.getName();
        String lineName = row.getProductionLine();
        String entityIdentifier = (machineName != null && !machineName.trim().isEmpty()) ? machineName : "N/A (Empty Name)";

        if (machineName == null || machineName.trim().isEmpty()) {
            report.addSkipped(excelRowNumber, "Machine name is empty or null. Cannot process.", "Machine", entityIdentifier, "Name", null, "MISSING_REQUIRED_FIELD");
            return;
        }
        if (lineName == null || lineName.trim().isEmpty()) {
            report.addSkipped(excelRowNumber, "Production Line name for machine '" + entityIdentifier + "' is empty or null.", "Machine", entityIdentifier, "Production Line", null, "MISSING_REQUIRED_FIELD");
            return;
        }

        try {
            Optional<Long> productionLineId = idResolverService.getLineIdByName(lineName);
            if (productionLineId.isEmpty()) {
                report.addSkipped(excelRowNumber, "Production Line '" + lineName + "' not found for machine '" + entityIdentifier + "'.", "Machine", entityIdentifier, "Production Line", lineName, "DEPENDENCY_NOT_FOUND");
                return;
            }

            Optional<Long> existingMachineId = idResolverService.getMachineIdByName(machineName);
            Map<String, Object> body = new HashMap<>();
            body.put("name", machineName.trim());
            body.put("brand", row.getBrand());
            body.put("description", row.getDescription());
            body.put("type", row.getType());
            body.put("productionLineId", productionLineId.get());

            if (existingMachineId.isPresent()) {
                machineClient.updateMachine(existingMachineId.get(), body);
                report.incrementSuccessfulImports();
                System.out.println("Updated Machine: " + machineName + " (ID: " + existingMachineId.get() + ")");
            } else {
                machineClient.createMachine(body);
                report.incrementSuccessfulImports();
                System.out.println("Created Machine: " + machineName);
            }
        } catch (FeignException.Conflict e) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "Machine '" + entityIdentifier + "' conflicts with existing data: " + getFeignErrorMessage(e), "Machine", entityIdentifier, "Name", machineName, "API_CONFLICT"));
        } catch (FeignException e) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "API error for Machine '" + entityIdentifier + "': Status " + e.status() + " - " + getFeignErrorMessage(e), "Machine", entityIdentifier, "API Call", null, "API_ERROR"));
        } catch (Exception e) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "Error processing Machine '" + entityIdentifier + "': " + e.getMessage(), "Machine", entityIdentifier, "Overall Row", null, "UNEXPECTED_SAVE_ERROR"));
            e.printStackTrace();
        }
    }

    private void importStepsInternal(StepRow row, ImportReport report, int excelRowNumber) {
        String stepName = row.getName();
        String machineName = row.getMachineName();
        String entityIdentifier = (stepName != null && !stepName.trim().isEmpty()) ? stepName : "N/A (Empty Name)";

        if (stepName == null || stepName.trim().isEmpty()) {
            report.addSkipped(excelRowNumber, "Step name is empty or null. Cannot process.", "Step", entityIdentifier, "Name", null, "MISSING_REQUIRED_FIELD");
            return;
        }
        if (machineName == null || machineName.trim().isEmpty()) {
            report.addSkipped(excelRowNumber, "Machine name for step '" + entityIdentifier + "' is empty or null.", "Step", entityIdentifier, "Machine Name", null, "MISSING_REQUIRED_FIELD");
            return;
        }

        try {
            Optional<Long> machineId = idResolverService.getMachineIdByName(machineName);
            if (machineId.isEmpty()) {
                report.addSkipped(excelRowNumber, "Machine '" + machineName + "' not found for step '" + entityIdentifier + "'.", "Step", entityIdentifier, "Machine Name", machineName, "DEPENDENCY_NOT_FOUND");
                return;
            }

            Optional<Long> existingStepId = idResolverService.getStepIdByName(stepName);
            Map<String, Object> body = new HashMap<>();
            body.put("name", stepName.trim());
            body.put("description", row.getDescription());
            body.put("orderIndex", row.getOrderIndex());
            body.put("requiresManualTracking", row.getRequiresManualTracking());
            body.put("machineId", machineId.get());

            if (existingStepId.isPresent()) {
                machineClient.updateStep(existingStepId.get(), body);
                report.incrementSuccessfulImports();
                System.out.println("Updated Step: " + stepName + " (ID: " + existingStepId.get() + ")");
            } else {
                machineClient.createStep(body);
                report.incrementSuccessfulImports();
                System.out.println("Created Step: " + stepName);
            }
        } catch (FeignException.Conflict e) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "Step '" + entityIdentifier + "' conflicts with existing data: " + getFeignErrorMessage(e), "Step", entityIdentifier, "Name", stepName, "API_CONFLICT"));
        } catch (FeignException e) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "API error for Step '" + entityIdentifier + "': Status " + e.status() + " - " + getFeignErrorMessage(e), "Step", entityIdentifier, "API Call", null, "API_ERROR"));
        } catch (Exception e) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "Error processing Step '" + entityIdentifier + "': " + e.getMessage(), "Step", entityIdentifier, "Overall Row", null, "UNEXPECTED_SAVE_ERROR"));
            e.printStackTrace();
        }
    }

    private void importCycleTimesInternal(CycleTimeRow row, ImportReport report, int excelRowNumber) {
        String projectName = row.getProjectName();
        String variantName = row.getVariantName();
        String lineName = row.getLineName();
        String stepName = row.getStepName();
        String machineName = row.getMachineName();
        String userEmail = row.getUserEmail();

        String entityIdentifier = String.format("Project:'%s', Variant:'%s', Line:'%s', Step:'%s', Machine:'%s', User:'%s'",
                projectName, variantName, lineName, stepName, machineName, userEmail);

        // --- Input Validation & Required Fields Check ---
        // Changed to use addError for clearer reporting if a critical field is missing
        if (projectName == null || projectName.trim().isEmpty()) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "Project name is missing or empty.", "CycleTime", entityIdentifier, "Project Name", null, "MISSING_REQUIRED_FIELD")); return;
        }
        if (variantName == null || variantName.trim().isEmpty()) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "Variant name is missing or empty.", "CycleTime", entityIdentifier, "Variant Name", null, "MISSING_REQUIRED_FIELD")); return;
        }
        if (lineName == null || lineName.trim().isEmpty()) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "Production Line name is missing or empty.", "CycleTime", entityIdentifier, "Line Name", null, "MISSING_REQUIRED_FIELD")); return;
        }
        if (stepName == null || stepName.trim().isEmpty()) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "Step name is missing or empty.", "CycleTime", entityIdentifier, "Step Name", null, "MISSING_REQUIRED_FIELD")); return;
        }
        if (machineName == null || machineName.trim().isEmpty()) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "Machine name is missing or empty.", "CycleTime", entityIdentifier, "Machine Name", null, "MISSING_REQUIRED_FIELD")); return;
        }
        if (userEmail == null || userEmail.trim().isEmpty()) {
            report.addError(new ImportRowError(excelRowNumber, "Failed", "User email is missing or empty.", "CycleTime", entityIdentifier, "User Email", null, "MISSING_REQUIRED_FIELD")); return;
        }

        try {
            Map<String, Object> body = new HashMap<>();

            // --- ID Lookups and Dependency Checks ---
            Long projectId = idResolverService.getProjectIdByName(projectName).orElse(null);
            if (projectId == null) { report.addError(new ImportRowError(excelRowNumber, "Failed", "Project '" + projectName + "' not found.", "CycleTime", entityIdentifier, "Project Name", projectName, "DEPENDENCY_NOT_FOUND")); return; }
            body.put("projectId", projectId);

            Long variantId = idResolverService.getVariantIdByName(variantName).orElse(null);
            if (variantId == null) { report.addError(new ImportRowError(excelRowNumber, "Failed", "Variant '" + variantName + "' not found.", "CycleTime", entityIdentifier, "Variant Name", variantName, "DEPENDENCY_NOT_FOUND")); return; }
            body.put("variantId", variantId);

            Long lineId = idResolverService.getLineIdByName(lineName).orElse(null);
            if (lineId == null) { report.addError(new ImportRowError(excelRowNumber, "Failed", "Production Line '" + lineName + "' not found.", "CycleTime", entityIdentifier, "Line Name", lineName, "DEPENDENCY_NOT_FOUND")); return; }
            body.put("lineId", lineId);

            Long stepId = idResolverService.getStepIdByName(stepName).orElse(null);
            if (stepId == null) { report.addError(new ImportRowError(excelRowNumber, "Failed", "Step '" + stepName + "' not found.", "CycleTime", entityIdentifier, "Step Name", stepName, "DEPENDENCY_NOT_FOUND")); return; }
            body.put("stepId", stepId);

            Long machineId = idResolverService.getMachineIdByName(machineName).orElse(null);
            if (machineId == null) { report.addError(new ImportRowError(excelRowNumber, "Failed", "Machine '" + machineName + "' not found.", "CycleTime", entityIdentifier, "Machine Name", machineName, "DEPENDENCY_NOT_FOUND")); return; }
            body.put("machineId", machineId);

            Long userId = idResolverService.getUserIdByEmail(userEmail).orElse(null);
            if (userId == null) { report.addError(new ImportRowError(excelRowNumber, "Failed", "User '" + userEmail + "' not found. Make sure user exists.", "CycleTime", entityIdentifier, "User Email", userEmail, "DEPENDENCY_NOT_FOUND")); return; }
            body.put("userId", userId);

            // --- Date/Time and Duration Parsing (CRITICAL SECTION) ---
            // Ensure your Excel data for these fields matches ISO-8601 string formats.
            // If not, you'll need a custom parsing utility in ExcelUtil or adjust formats.
            // Example ISO-8601 Duration: PT8H6M12S (8 hours, 6 minutes, 12 seconds)
            // Example ISO-8601 DateTime: 2025-06-17T16:01:04

            String clientCycleTimeStr = row.getClientCycleTime();
            if (clientCycleTimeStr != null && !clientCycleTimeStr.trim().isEmpty()) {
                try {
                    body.put("clientCycleTime", Duration.parse(clientCycleTimeStr.trim()));
                } catch (DateTimeParseException e) {
                    report.addError(new ImportRowError(excelRowNumber, "Failed", "Invalid ClientCycleTime format. Expected ISO-8601 duration (e.g., PT8H6M12S). Found: '" + clientCycleTimeStr + "'", "CycleTime", entityIdentifier, "ClientCycleTime", clientCycleTimeStr, "INVALID_FORMAT"));
                    return;
                }
            } else {
                report.addError(new ImportRowError(excelRowNumber, "Failed", "ClientCycleTime is missing or empty.", "CycleTime", entityIdentifier, "ClientCycleTime", null, "MISSING_REQUIRED_FIELD"));
                return;
            }

            String theoreticalCycleTimeStr = row.getTheoriticalCycleTime();
            if (theoreticalCycleTimeStr != null && !theoreticalCycleTimeStr.trim().isEmpty()) {
                try {
                    body.put("theoriticalCycleTime", Duration.parse(theoreticalCycleTimeStr.trim()));
                } catch (DateTimeParseException e) {
                    report.addError(new ImportRowError(excelRowNumber, "Failed", "Invalid TheoriticalCycleTime format. Expected ISO-8601 duration. Found: '" + theoreticalCycleTimeStr + "'", "CycleTime", entityIdentifier, "TheoriticalCycleTime", theoreticalCycleTimeStr, "INVALID_FORMAT"));
                    return;
                }
            } else {
                report.addError(new ImportRowError(excelRowNumber, "Failed", "TheoriticalCycleTime is missing or empty.", "CycleTime", entityIdentifier, "TheoriticalCycleTime", null, "MISSING_REQUIRED_FIELD"));
                return;
            }

            String startTimeStr = row.getStartTime();
            if (startTimeStr != null && !startTimeStr.trim().isEmpty()) {
                try {
                    body.put("startTime", LocalDateTime.parse(startTimeStr.trim()));
                } catch (DateTimeParseException e) {
                    report.addError(new ImportRowError(excelRowNumber, "Failed", "Invalid StartTime format. Expected ISO-8601 datetime (e.g.,YYYY-MM-DDTHH:MM:SS). Found: '" + startTimeStr + "'", "CycleTime", entityIdentifier, "StartTime", startTimeStr, "INVALID_FORMAT"));
                    return;
                }
            } else {
                report.addError(new ImportRowError(excelRowNumber, "Failed", "StartTime is missing or empty.", "CycleTime", entityIdentifier, "StartTime", null, "MISSING_REQUIRED_FIELD"));
                return;
            }

            String endTimeStr = row.getEndTime();
            if (endTimeStr != null && !endTimeStr.trim().isEmpty()) {
                try {
                    body.put("endTime", LocalDateTime.parse(endTimeStr.trim()));
                } catch (DateTimeParseException e) {
                    report.addError(new ImportRowError(excelRowNumber, "Failed", "Invalid EndTime format. Expected ISO-8601 datetime. Found: '" + endTimeStr + "'", "CycleTime", entityIdentifier, "EndTime", endTimeStr, "INVALID_FORMAT"));
                    return;
                }
            } else {
                report.addError(new ImportRowError(excelRowNumber, "Failed", "EndTime is missing or empty.", "CycleTime", entityIdentifier, "EndTime", null, "MISSING_REQUIRED_FIELD"));
                return;
            }

            // --- Other fields ---
            body.put("isManual", row.getIsManual());
            body.put("status", row.getStatus());
            body.put("recordType", row.getRecordType());

            // --- API Call to CycleTime Service ---
            try {
                cycleClient.createCycleTime(body);
                report.incrementSuccessfulImports();
                System.out.println("Created CycleTime for " + entityIdentifier);
            } catch (FeignException.Conflict e) {
                report.addError(new ImportRowError(excelRowNumber, "Failed", "CycleTime record conflicts with existing data: " + getFeignErrorMessage(e), "CycleTime", entityIdentifier, "API Call", null, "API_CONFLICT"));
            } catch (FeignException e) {
                report.addError(new ImportRowError(excelRowNumber, "Failed", "API error for CycleTime (" + entityIdentifier + "): Status " + e.status() + " - " + getFeignErrorMessage(e), "CycleTime", entityIdentifier, "API Call", null, "API_ERROR"));
            } catch (Exception e) {
                report.addError(new ImportRowError(excelRowNumber, "Failed", "Unexpected error creating CycleTime (" + entityIdentifier + "): " + e.getMessage(), "CycleTime", entityIdentifier, "API Call", null, "UNEXPECTED_SAVE_ERROR"));
                e.printStackTrace();
            }

        } catch (Exception e) { // Catch any other unexpected errors during lookup or initial processing for this row
            report.addError(new ImportRowError(excelRowNumber, "Failed", "General error processing CycleTime row (" + entityIdentifier + "): " + e.getMessage(), "CycleTime", entityIdentifier, "Overall Row", null, "GENERAL_ROW_ERROR"));
            e.printStackTrace();
        }
    }

    // Helper method to extract meaningful message from FeignException
    private String getFeignErrorMessage(FeignException e) {
        try {
            // Attempt to parse the response body, which often contains detailed error messages from the target service
            String content = e.contentUTF8();
            // If the content is very long or contains sensitive info, you might want to truncate it
            return content != null && !content.isEmpty() ? content : e.getMessage();
        } catch (Exception ex) {
            // Fallback to default message if content can't be read
            return e.getMessage();
        }
    }
}
