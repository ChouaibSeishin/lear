package org.lear.importservice.dtos;

import lombok.Data;

@Data
public class MachineRow {
    private String name;
    private String brand;
    private String description;
    private String type;
    private String productionLine;
}
