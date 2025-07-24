package org.lear.aibotservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Variant {
    private Long id;
    private String name;
    private Long projectId;
    private String status;

   }
