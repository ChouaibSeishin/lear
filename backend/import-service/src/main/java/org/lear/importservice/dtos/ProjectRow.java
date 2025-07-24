package org.lear.importservice.dtos;


import lombok.Data;

import java.util.List;

@Data
public class ProjectRow {
    private String name;
    private String description;
    private List<String> productionLines;

}
