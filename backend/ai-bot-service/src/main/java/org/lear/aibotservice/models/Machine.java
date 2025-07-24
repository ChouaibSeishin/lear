package org.lear.aibotservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Machine {
    private Long id;
    private String name;
    private Long productionLineId;
    private String brand;
    private String type;
    private List<Step> steps = new ArrayList<>();

   }
