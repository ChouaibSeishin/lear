package org.lear.importservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportRowError {
    private int rowNumber;
    private String status;
    private String errorMessage;
    private String entityType;
    private String entityIdentifier;
    private String fieldName;
    private String problematicValue;
    private String errorCode;
}
