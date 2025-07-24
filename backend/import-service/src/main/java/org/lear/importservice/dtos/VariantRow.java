package org.lear.importservice.dtos;

import lombok.Data;

import java.util.List;

@Data
public class VariantRow {
    private String name;
    private String status;
    private List<String> productionLines;
    private String projectName;
}
