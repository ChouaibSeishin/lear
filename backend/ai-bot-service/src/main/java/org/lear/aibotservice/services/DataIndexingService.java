package org.lear.aibotservice.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.lear.aibotservice.config.RAGIndexingProperties;
import org.lear.aibotservice.feignClients.MachineClient;
import org.lear.aibotservice.feignClients.ProjectClient;
import org.lear.aibotservice.feignClients.CycleTimeClient;
import org.lear.aibotservice.models.CycleTime;
import org.lear.aibotservice.models.Machine;
import org.lear.aibotservice.models.ProductionLine;
import org.lear.aibotservice.models.Project;
import org.lear.aibotservice.models.Step;
import org.lear.aibotservice.models.Variant;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Set; // Import Set
import java.util.HashSet; // Import HashSet
import java.util.Objects; // Import Objects for null-safe checks
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataIndexingService {

    private final VectorStoreService vectorStore;
    private final MachineClient machineClient;
    private final ProjectClient projectClient;
    private final CycleTimeClient cycleTimeClient;
    private final RAGIndexingProperties indexingProperties;

    // Lookup Maps for quicker access by ID to Name
    private Map<Long, String> machineNames = Collections.emptyMap();
    private Map<Long, String> productionLineNames = Collections.emptyMap();
    private Map<Long, String> stepNames = Collections.emptyMap();
    private Map<Long, String> projectNames = Collections.emptyMap();
    private Map<Long, String> variantNames = Collections.emptyMap();

    // In-memory caches of all data fetched (for comprehensive document creation)
    private List<Machine> allMachines = Collections.emptyList();
    private List<ProductionLine> allProductionLines = Collections.emptyList();
    private List<Step> allSteps = Collections.emptyList();
    private List<Project> allProjects = Collections.emptyList();
    private List<Variant> allVariants = Collections.emptyList();
    private List<CycleTime> allCycleTimes = Collections.emptyList();

    // Sets to quickly check for existence of cycle times for specific entities
    private Set<Long> projectsWithCycleTimes = Collections.emptySet();
    private Set<Long> variantsWithCycleTimes = Collections.emptySet();
    private Set<Long> machinesWithCycleTimes = Collections.emptySet();
    private Set<Long> stepsWithCycleTimes = Collections.emptySet();


    public DataIndexingService(VectorStoreService vectorStore, MachineClient machineClient,
                               ProjectClient projectClient, CycleTimeClient cycleTimeClient,
                               RAGIndexingProperties indexingProperties) {
        this.vectorStore = vectorStore;
        this.machineClient = machineClient;
        this.projectClient = projectClient;
        this.cycleTimeClient = cycleTimeClient;
        this.indexingProperties = indexingProperties;
    }

    @PostConstruct
    @Async
    public void initializeIndex() {
        if (!indexingProperties.isAutoStart()) {
            log.info("Auto-start disabled, skipping initial indexing");
            return;
        }

        log.info("Starting data indexing with batch size: {}", indexingProperties.getBatchSize());

        // Step 1: Pre-fetch ALL necessary data and populate lookup maps
        // This is crucial for building comprehensive, relationship-rich documents efficiently.
        loadAllDataAndLookupMaps();


        // Step 2: Index comprehensive documents first for rich context
        // These are the "smart chunks"
        indexComprehensiveProjectDocuments();
        indexComprehensiveMachineDocuments();

        // Step 3:  Index granular entities if specific, direct retrieval by ID is needed
        indexMachines();
        indexProductionLines();
        indexSteps();
        indexProjects();
        indexVariants();
        indexCycleTimes();


        log.info("Data indexing completed");
    }

    // Renamed and extended to load ALL data into class members
    private void loadAllDataAndLookupMaps() {
        try {
            // Load all core entities first
            allMachines = machineClient.getAllMachines();
            machineNames = allMachines.stream()
                    .collect(Collectors.toMap(Machine::getId, Machine::getName));
            log.info("Loaded {} machines.", allMachines.size());

            allProductionLines = machineClient.getAllProductionLines();
            productionLineNames = allProductionLines.stream()
                    .collect(Collectors.toMap(ProductionLine::getId, ProductionLine::getName));
            log.info("Loaded {} production lines.", allProductionLines.size());

            allSteps = machineClient.getAllSteps();
            stepNames = allSteps.stream()
                    .collect(Collectors.toMap(Step::getId, Step::getName));
            log.info("Loaded {} steps.", allSteps.size());

            allProjects = projectClient.getAllProjects();
            projectNames = allProjects.stream()
                    .collect(Collectors.toMap(Project::getId, Project::getName));
            log.info("Loaded {} projects.", allProjects.size());

            allVariants = projectClient.getAllVariants();
            variantNames = allVariants.stream()
                    .collect(Collectors.toMap(Variant::getId, Variant::getName));
            log.info("Loaded {} variants.", allVariants.size());

            allCycleTimes = cycleTimeClient.getAll();
            log.info("Loaded {} cycle times.", allCycleTimes.size());

            // NEW: Populate sets for quick 'has cycle time' checks
            projectsWithCycleTimes = new HashSet<>();
            variantsWithCycleTimes = new HashSet<>();
            machinesWithCycleTimes = new HashSet<>();
            stepsWithCycleTimes = new HashSet<>();

            for (CycleTime ct : allCycleTimes) {
                if (ct.getProjectId() != null) {
                    projectsWithCycleTimes.add(ct.getProjectId());
                }
                if (ct.getVariantId() != null) {
                    variantsWithCycleTimes.add(ct.getVariantId());
                }
                if (ct.getMachineId() != null) {
                    machinesWithCycleTimes.add(ct.getMachineId());
                }
                if (ct.getStepId() != null) {
                    stepsWithCycleTimes.add(ct.getStepId());
                }
            }
            log.info("Populated cycle time existence sets.");


        } catch (Exception e) {
            log.error("Failed to load all data and lookup maps: {}", e.getMessage());
            // Ensure all collections are empty on failure to prevent NullPointers later
            allMachines = Collections.emptyList();
            allProductionLines = Collections.emptyList();
            allSteps = Collections.emptyList();
            allProjects = Collections.emptyList();
            allVariants = Collections.emptyList();
            allCycleTimes = Collections.emptyList();
            machineNames = Collections.emptyMap();
            productionLineNames = Collections.emptyMap();
            stepNames = Collections.emptyMap();
            projectNames = Collections.emptyMap();
            variantNames = Collections.emptyMap();
            projectsWithCycleTimes = Collections.emptySet();
            variantsWithCycleTimes = Collections.emptySet();
            machinesWithCycleTimes = Collections.emptySet();
            stepsWithCycleTimes = Collections.emptySet();
        }
    }


    // --- NEW: Comprehensive Indexing Methods ---

    private void indexComprehensiveProjectDocuments() {
        try {
            if (allProjects.isEmpty()) {
                log.warn("No projects loaded for comprehensive indexing. Skipping.");
                return;
            }

            for (Project project : allProjects) {
                StringBuilder contentBuilder = new StringBuilder();
                contentBuilder.append("--- Project Details ---\n");
                contentBuilder.append("Project Name: ").append(Objects.requireNonNullElse(project.getName(), "N/A")).append(" (ID: ").append(project.getId()).append(")\n");
                contentBuilder.append("Description: ").append(Objects.requireNonNullElse(project.getDescription(), "N/A")).append("\n");

                // Include Production Line Info
                if (project.getProductionLineIds() != null && !project.getProductionLineIds().isEmpty()) {
                    contentBuilder.append("Associated Production Lines: ");
                    project.getProductionLineIds().forEach(lineId -> {
                        String lineName = productionLineNames.getOrDefault(lineId, "Unknown Line");
                        contentBuilder.append(lineName).append(" (ID: ").append(lineId).append("); ");
                    });
                    contentBuilder.append("\n");
                }

                // Include Variant Info and their Cycle Times
                List<Variant> projectVariants = allVariants.stream()
                        .filter(v -> Objects.equals(v.getProjectId(), project.getId()))
                        .collect(Collectors.toList());

                if (!projectVariants.isEmpty()) {
                    contentBuilder.append("--- Associated Variants ---\n");
                    for (Variant variant : projectVariants) {
                        contentBuilder.append("  Variant Name: ").append(Objects.requireNonNullElse(variant.getName(), "N/A")).append(" (ID: ").append(variant.getId()).append(")\n");
                        contentBuilder.append("  Variant Status: ").append(Objects.requireNonNullElse(variant.getStatus(), "N/A")).append("\n");

                        // Find Cycle Times for this Variant (within this project)
                        List<CycleTime> variantCycleTimes = allCycleTimes.stream()
                                .filter(ct -> Objects.equals(ct.getVariantId(), variant.getId()) &&
                                        Objects.equals(ct.getProjectId(), project.getId()))
                                .collect(Collectors.toList());

                        if (!variantCycleTimes.isEmpty()) {
                            contentBuilder.append("    --- Cycle Times for this Variant ---\n");
                            for (CycleTime ct : variantCycleTimes) {
                                contentBuilder.append("      - Cycle Time ID: ").append(ct.getId()).append("\n");
                                contentBuilder.append("        Duration: ").append(Objects.requireNonNullElse(ct.getFormattedDuration(), "N/A")).append("\n");
                                contentBuilder.append("        Status: ").append(Objects.requireNonNullElse(ct.getStatus(), "N/A")).append("\n");
                                if (ct.getStartTime() != null) contentBuilder.append("        Start Time: ").append(ct.getStartTime()).append("\n");
                                if (ct.getEndTime() != null) contentBuilder.append("        End Time: ").append(ct.getEndTime()).append("\n");
                                if (ct.getMachineId() != null) contentBuilder.append("        Machine: ").append(machineNames.getOrDefault(ct.getMachineId(), "Unknown")).append(" (ID: ").append(ct.getMachineId()).append(")\n");
                                if (ct.getStepId() != null) contentBuilder.append("        Step: ").append(stepNames.getOrDefault(ct.getStepId(), "Unknown")).append(" (ID: ").append(ct.getStepId()).append(")\n");
                                if (ct.getLineId() != null) contentBuilder.append("        Production Line: ").append(productionLineNames.getOrDefault(ct.getLineId(), "Unknown")).append(" (ID: ").append(ct.getLineId()).append(")\n");
                            }
                        } else {
                            contentBuilder.append("    No Cycle Times recorded for this Variant.\n");
                        }
                    }
                } else {
                    contentBuilder.append("--- No Variants associated with this Project. ---\n");
                }

                String documentContent = contentBuilder.toString();

                boolean hasAnyCycleTimeInProject = projectsWithCycleTimes.contains(project.getId());

                Map<String, Object> metadata = Map.of(
                        "name", Objects.requireNonNullElse(project.getName(), "Unknown Project"),
                        "entityType", "project_comprehensive", // Important: distinct entity type
                        "projectId", project.getId(),
                        "description", Objects.requireNonNullElse(project.getDescription(), "N/A"),
                        "hasCycleTime", hasAnyCycleTimeInProject // <-- NEW METADATA FIELD
                        // You can add other top-level project metadata here if useful for filtering
                );
                vectorStore.addDocument(documentContent, "project_comprehensive", project.getId(), metadata);
            }
            log.info("Finished indexing comprehensive project documents. Total: {}", allProjects.size());
        } catch (Exception e) {
            log.error("Failed to index comprehensive project documents: {}", e.getMessage());
        }
    }

    private void indexComprehensiveMachineDocuments() {
        try {
            if (allMachines.isEmpty()) {
                log.warn("No machines loaded for comprehensive indexing. Skipping.");
                return;
            }

            for (Machine machine : allMachines) {
                StringBuilder contentBuilder = new StringBuilder();
                contentBuilder.append("--- Machine Details ---\n");
                contentBuilder.append("Machine Name: ").append(Objects.requireNonNullElse(machine.getName(), "N/A")).append(" (ID: ").append(machine.getId()).append(")\n");
                contentBuilder.append("Brand: ").append(Objects.requireNonNullElse(machine.getBrand(), "N/A")).append("\n");
                contentBuilder.append("Type: ").append(Objects.requireNonNullElse(machine.getType(), "N/A")).append("\n");
                if (machine.getProductionLineId() != null) {
                    contentBuilder.append("Production Line: ").append(productionLineNames.getOrDefault(machine.getProductionLineId(), "Unknown")).append(" (ID: ").append(machine.getProductionLineId()).append(")\n");
                }

                // Include Step Info and their Cycle Times
                List<Step> machineSteps = allSteps.stream()
                        .filter(s -> Objects.equals(s.getMachineId(), machine.getId()))
                        .collect(Collectors.toList());

                if (!machineSteps.isEmpty()) {
                    contentBuilder.append("--- Associated Steps ---\n");
                    for (Step step : machineSteps) {
                        contentBuilder.append("  Step Name: ").append(Objects.requireNonNullElse(step.getName(), "N/A")).append(" (ID: ").append(step.getId()).append(")\n");
                        contentBuilder.append("  Order Index: ").append(step.getOrderIndex()).append("\n");
                        contentBuilder.append("  Requires Manual Tracking: ").append(step.isRequiresManualTracking()).append("\n");

                        // Find Cycle Times for this Step (within this machine)
                        List<CycleTime> stepCycleTimes = allCycleTimes.stream()
                                .filter(ct -> Objects.equals(ct.getStepId(), step.getId()) &&
                                        Objects.equals(ct.getMachineId(), machine.getId()))
                                .collect(Collectors.toList());

                        if (!stepCycleTimes.isEmpty()) {
                            contentBuilder.append("    --- Cycle Times for this Step ---\n");
                            for (CycleTime ct : stepCycleTimes) {
                                contentBuilder.append("      - Cycle Time ID: ").append(ct.getId()).append("\n");
                                contentBuilder.append("        Duration: ").append(Objects.requireNonNullElse(ct.getFormattedDuration(), "N/A")).append("\n");
                                contentBuilder.append("        Status: ").append(Objects.requireNonNullElse(ct.getStatus(), "N/A")).append("\n");
                                if (ct.getStartTime() != null) contentBuilder.append("        Start Time: ").append(ct.getStartTime()).append("\n");
                                if (ct.getEndTime() != null) contentBuilder.append("        End Time: ").append(ct.getEndTime()).append("\n");
                                if (ct.getProjectId() != null) contentBuilder.append("        Project: ").append(projectNames.getOrDefault(ct.getProjectId(), "Unknown")).append(" (ID: ").append(ct.getProjectId()).append(")\n");
                                if (ct.getVariantId() != null) contentBuilder.append("        Variant: ").append(variantNames.getOrDefault(ct.getVariantId(), "Unknown")).append(" (ID: ").append(ct.getVariantId()).append(")\n");
                            }
                        } else {
                            contentBuilder.append("    No Cycle Times recorded for this Step.\n");
                        }
                    }
                } else {
                    contentBuilder.append("--- No Steps associated with this Machine. ---\n");
                }

                String documentContent = contentBuilder.toString();

                // Determine hasCycleTime metadata for machine
                boolean hasAnyCycleTimeInMachine = machinesWithCycleTimes.contains(machine.getId());

                Map<String, Object> metadata = Map.of(
                        "name", Objects.requireNonNullElse(machine.getName(), "Unknown Machine"),
                        "entityType", "machine_comprehensive", // Important: distinct entity type
                        "machineId", machine.getId(),
                        "hasCycleTime", hasAnyCycleTimeInMachine // <-- NEW METADATA FIELD
                        // Add other top-level machine metadata here if useful for filtering
                );
                vectorStore.addDocument(documentContent, "machine_comprehensive", machine.getId(), metadata);
            }
            log.info("Finished indexing comprehensive machine documents. Total: {}", allMachines.size());
        } catch (Exception e) {
            log.error("Failed to index comprehensive machine documents: {}", e.getMessage());
        }
    }


    // --- Original Granular Indexing Methods (Kept for now, consider removing if comprehensive is sufficient) ---

    private void indexMachines() {
        try {
            // Use the already loaded allMachines
            for (Machine machine : allMachines) {
                String content = formatMachineForIndexing(machine);
                Map<String, Object> metadata = Map.of(
                        "name", Objects.requireNonNullElse(machine.getName(), "Unknown"),
                        "brand", Objects.requireNonNullElse(machine.getBrand(), "N/A"),
                        "type", Objects.requireNonNullElse(machine.getType(), "N/A"),
                        "productionLineId", Objects.requireNonNullElse(machine.getProductionLineId(), -1L)
                );
                vectorStore.addDocument(content, "machine", machine.getId(), metadata);
            }
            log.info("Indexed {} granular machines.", allMachines.size());
        } catch (Exception e) {
            log.error("Failed to index granular machines: {}", e.getMessage());
        }
    }

    private void indexProductionLines() {
        try {
            // Use the already loaded allProductionLines
            for (ProductionLine line : allProductionLines) {
                String content = formatProductionLineForIndexing(line);
                Map<String, Object> metadata = Map.of(
                        "name", Objects.requireNonNullElse(line.getName(), "Unknown")
                );
                vectorStore.addDocument(content, "production_line", line.getId(), metadata);
            }
            log.info("Indexed {} granular production lines.", allProductionLines.size());
        } catch (Exception e) {
            log.error("Failed to index granular production lines: {}", e.getMessage());
        }
    }

    private void indexSteps() {
        try {
            // Use the already loaded allSteps
            for (Step step : allSteps) {
                String content = formatStepForIndexing(step);
                Map<String, Object> metadata = Map.of(
                        "name", Objects.requireNonNullElse(step.getName(), "Unknown"),
                        "orderIndex", step.getOrderIndex(),
                        "requiresManualTracking", step.isRequiresManualTracking(),
                        "machineId", Objects.requireNonNullElse(step.getMachineId(), -1L)
                );
                vectorStore.addDocument(content, "step", step.getId(), metadata);
            }
            log.info("Indexed {} granular steps.", allSteps.size());
        } catch (Exception e) {
            log.error("Failed to index granular steps: {}", e.getMessage());
        }
    }

    private void indexProjects() {
        try {
            // Use the already loaded allProjects
            for (Project project : allProjects) {
                String content = formatProjectForIndexing(project); // This formatter is simple, consider if still needed
                Map<String, Object> metadata = Map.of(
                        "name", Objects.requireNonNullElse(project.getName(), "Unknown"),
                        "description", Objects.requireNonNullElse(project.getDescription(), ""),
                        "productionLineIds", project.getProductionLineIds() != null ? project.getProductionLineIds() : Collections.emptyList()
                );
                vectorStore.addDocument(content, "project", project.getId(), metadata);
            }
            log.info("Indexed {} granular projects.", allProjects.size());
        } catch (Exception e) {
            log.error("Failed to index granular projects: {}", e.getMessage());
        }
    }

    private void indexVariants() {
        try {
            // Use the already loaded allVariants
            for (Variant variant : allVariants) {
                String content = formatVariantForIndexing(variant);
                Map<String, Object> metadata = Map.of(
                        "name", Objects.requireNonNullElse(variant.getName(), "Unknown"),
                        "projectId", Objects.requireNonNullElse(variant.getProjectId(), -1L),
                        "status", Objects.requireNonNullElse(variant.getStatus(), "Unknown")
                );
                vectorStore.addDocument(content, "variant", variant.getId(), metadata);
            }
            log.info("Indexed {} granular variants.", allVariants.size());
        } catch (Exception e) {
            log.error("Failed to index granular variants: {}", e.getMessage());
        }
    }

    private void indexCycleTimes() {
        try {
            // Use the already loaded allCycleTimes
            for (CycleTime cycleTime : allCycleTimes) {
                String content = formatCycleTimeForIndexing(cycleTime);
                Map<String, Object> metadata = Map.of(
                        "projectId", Objects.requireNonNullElse(cycleTime.getProjectId(), -1L),
                        "variantId", Objects.requireNonNullElse(cycleTime.getVariantId(), -1L),
                        "machineId", Objects.requireNonNullElse(cycleTime.getMachineId(), -1L),
                        "lineId", Objects.requireNonNullElse(cycleTime.getLineId(), -1L),
                        "stepId", Objects.requireNonNullElse(cycleTime.getStepId(), -1L),
                        "status", Objects.requireNonNullElse(cycleTime.getStatus(), "N/A")
                );
                vectorStore.addDocument(content, "cycle_time", cycleTime.getId(), metadata);
            }
            log.info("Indexed {} granular cycle times.", allCycleTimes.size());
        } catch (Exception e) {
            log.error("Failed to index granular cycle times: {}", e.getMessage());
        }
    }

    // --- Formatting Methods (Minor tweaks for null safety and clarity) ---

    private String formatMachineForIndexing(Machine machine) {
        StringBuilder content = new StringBuilder();
        content.append("Machine: ").append(Objects.requireNonNullElse(machine.getName(), "N/A")).append("\n");
        content.append("Brand: ").append(Objects.requireNonNullElse(machine.getBrand(), "N/A")).append("\n");
        content.append("Type: ").append(Objects.requireNonNullElse(machine.getType(), "N/A")).append("\n");
        if (machine.getProductionLineId() != null) {
            String productionLineName = productionLineNames.getOrDefault(machine.getProductionLineId(), "Unknown Production Line");
            content.append("Belongs to Production Line: ").append(productionLineName).append(" (ID: ").append(machine.getProductionLineId()).append(")\n");
        }
        // Removed original step listing here as it was rudimentary and comprehensive doc does it better
        return content.toString();
    }

    private String formatProductionLineForIndexing(ProductionLine line) {
        StringBuilder content = new StringBuilder();
        content.append("Production Line ID: ").append(line.getId()).append("\n");
        content.append("Name: ").append(Objects.requireNonNullElse(line.getName(), "N/A")).append("\n");
        return content.toString();
    }

    private String formatStepForIndexing(Step step) {
        StringBuilder content = new StringBuilder();
        content.append("Step: ").append(Objects.requireNonNullElse(step.getName(), "N/A")).append("\n");
        content.append("Order Index: ").append(step.getOrderIndex()).append("\n");
        content.append("Requires Manual Tracking: ").append(step.isRequiresManualTracking()).append("\n");
        if (step.getMachineId() != null) {
            String machineName = machineNames.getOrDefault(step.getMachineId(), "Unknown Machine"); // Corrected to use Long
            content.append("Belongs to Machine: ").append(machineName).append(" (ID: ").append(step.getMachineId()).append(")\n");
        }
        return content.toString();
    }

    private String formatProjectForIndexing(Project project) {
        StringBuilder content = new StringBuilder();
        content.append("Project: ").append(Objects.requireNonNullElse(project.getName(), "N/A")).append("\n");
        content.append("Description: ").append(Objects.requireNonNullElse(project.getDescription(), "N/A")).append("\n");
        if (project.getProductionLineIds() != null && !project.getProductionLineIds().isEmpty()) {
            content.append("Involves Production Lines: ");
            String lines = project.getProductionLineIds().stream()
                    .map(lineId -> productionLineNames.getOrDefault(lineId, "Unknown Line") + " (ID: " + lineId + ")")
                    .collect(Collectors.joining(", "));
            content.append(lines).append("\n");
        }
        // Removed variant listing here, comprehensive doc covers it
        return content.toString();
    }

    private String formatVariantForIndexing(Variant variant) {
        StringBuilder content = new StringBuilder();
        content.append("Variant: ").append(Objects.requireNonNullElse(variant.getName(), "N/A")).append("\n");
        if (variant.getProjectId() != null) {
            String projectName = projectNames.getOrDefault(variant.getProjectId(), "Unknown Project");
            content.append("Belongs to Project: ").append(projectName).append(" (ID: ").append(variant.getProjectId()).append(")\n");
        }
        content.append("Status: ").append(Objects.requireNonNullElse(variant.getStatus(), "N/A")).append("\n");
        return content.toString();
    }

    private String formatCycleTimeForIndexing(CycleTime cycleTime) {
        StringBuilder content = new StringBuilder();
        content.append("Cycle Time Record (ID: ").append(cycleTime.getId()).append(")\n");

        if (cycleTime.getProjectId() != null) {
            String projectName = projectNames.getOrDefault(cycleTime.getProjectId(), "Unknown Project");
            content.append("Project: ").append(projectName).append(" (ID: ").append(cycleTime.getProjectId()).append(")\n");
        }
        if (cycleTime.getVariantId() != null) {
            String variantName = variantNames.getOrDefault(cycleTime.getVariantId(), "Unknown Variant");
            content.append("Variant: ").append(variantName).append(" (ID: ").append(cycleTime.getVariantId()).append(")\n");
        }
        if (cycleTime.getMachineId() != null) {
            String machineName = machineNames.getOrDefault(cycleTime.getMachineId(), "Unknown Machine");
            content.append("Machine: ").append(machineName).append(" (ID: ").append(cycleTime.getMachineId()).append(")\n");
        }
        if (cycleTime.getLineId() != null) {
            String lineName = productionLineNames.getOrDefault(cycleTime.getLineId(), "Unknown Production Line");
            content.append("Production Line: ").append(lineName).append(" (ID: ").append(cycleTime.getLineId()).append(")\n");
        }
        if (cycleTime.getStepId() != null) {
            String stepName = stepNames.getOrDefault(cycleTime.getStepId(), "Unknown Step");
            content.append("Step: ").append(stepName).append(" (ID: ").append(cycleTime.getStepId()).append(")\n");
        }

        content.append("Duration: ").append(Objects.requireNonNullElse(cycleTime.getFormattedDuration(), "N/A")).append("\n");
        content.append("Start Time: ").append(Objects.requireNonNullElse(cycleTime.getStartTime(), "N/A")).append("\n");
        content.append("End Time: ").append(Objects.requireNonNullElse(cycleTime.getEndTime(), "N/A")).append("\n");
        content.append("Recorded by User ID: ").append(Objects.requireNonNullElse(cycleTime.getUserId(), -1L)).append("\n");
        content.append("Status: ").append(Objects.requireNonNullElse(cycleTime.getStatus(), "N/A")).append("\n");

        return content.toString();
    }
    public Long getProductionLineIdByName(String name) {
        if (allProductionLines == null || allProductionLines.isEmpty()) {
            log.warn("Production lines not loaded, cannot get ID for name: {}", name);
            return null;
        }
        return allProductionLines.stream()
                .filter(line -> line.getName() != null && line.getName().equalsIgnoreCase(name))
                .map(ProductionLine::getId)
                .findFirst()
                .orElse(null);
    }
}
