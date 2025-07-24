package org.lear.importservice.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.DateUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExcelRowMapper {

    public static <T> T mapRow(Row row, List<String> headers, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                Cell cell = row.getCell(i);

                // Skip if header is invalid or cannot be mapped
                if (header == null || header.trim().isEmpty()) {
                    continue; // No valid header name to map
                }

                Field field;
                try {
                    field = clazz.getDeclaredField(header.trim()); // Trim header to match field name
                } catch (NoSuchFieldException e) {
                    continue; // Ignore if no such field in the DTO matching the header
                }

                field.setAccessible(true);
                Class<?> fieldType = field.getType();

                // Handle blank or null cells consistently for all field types
                if (cell == null || cell.getCellType() == CellType.BLANK) {
                    // For primitive types (int, long, double, boolean), setting null is not allowed.
                    // They will retain their default values (0, 0L, 0.0, false).
                    // For wrapper types (Integer, Long, Double, Boolean), List, String, etc., set to null.
                    if (!fieldType.isPrimitive()) {
                        field.set(instance, null);
                    }
                    continue; // Move to the next cell
                }

                // Handle List<String> fields
                if (fieldType == List.class) {
                    if (cell.getCellType() == CellType.STRING) {
                        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                        if (genericType.getActualTypeArguments()[0].equals(String.class)) {
                            String cellValue = cell.getStringCellValue();
                            if (cellValue != null && !cellValue.trim().isEmpty()) {
                                String[] parts = cellValue.split("\\s*,\\s*"); // Splits by comma, ignoring surrounding whitespace
                                List<String> list = new ArrayList<>();
                                for (String part : parts) {
                                    list.add(part.trim());
                                }
                                field.set(instance, list);
                            } else {
                                field.set(instance, new ArrayList<>()); // Set empty list for blank string value
                            }
                        }
                    } else {
                        // If it's a List field but cell is not a string, consider it empty or invalid
                        field.set(instance, new ArrayList<>());
                    }
                } else {
                    // Handle other standard types
                    switch (cell.getCellType()) {
                        case STRING:
                            String stringValue = cell.getStringCellValue();
                            if (stringValue != null) {
                                stringValue = stringValue.trim();
                            }
                            field.set(instance, stringValue);
                            break;

                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                // Handle dates/times
                                if (fieldType == LocalDateTime.class) {
                                    field.set(instance, cell.getLocalDateTimeCellValue());
                                } else if (fieldType == java.util.Date.class) { // If you still use java.util.Date
                                    field.set(instance, cell.getDateCellValue());
                                } else {
                                    // If a numeric cell is date formatted but field is not a date type,
                                    // you might want to convert it to string or numeric based on preference.
                                    // For now, setting as numeric value if target is numeric, else null.
                                    if (fieldType == Integer.class || fieldType == int.class) {
                                        field.set(instance, (int) cell.getNumericCellValue());
                                    } else if (fieldType == Long.class || fieldType == long.class) {
                                        field.set(instance, (long) cell.getNumericCellValue());
                                    } else if (fieldType == Double.class || fieldType == double.class) {
                                        field.set(instance, cell.getNumericCellValue());
                                    } else {
                                        field.set(instance, null); // Cannot map date to non-date field
                                    }
                                }
                            } else {
                                // Handle general numbers
                                if (fieldType == Integer.class || fieldType == int.class) {
                                    field.set(instance, (int) cell.getNumericCellValue());
                                } else if (fieldType == Long.class || fieldType == long.class) {
                                    field.set(instance, (long) cell.getNumericCellValue());
                                } else if (fieldType == Double.class || fieldType == double.class) {
                                    field.set(instance, cell.getNumericCellValue());
                                } else if (fieldType == String.class) {
                                    // Convert numeric to string to avoid data loss for IDs, etc.
                                    field.set(instance, String.valueOf(cell.getNumericCellValue()));
                                } else {
                                    // Default to double if no specific numeric type matches, or null for others
                                    field.set(instance, cell.getNumericCellValue());
                                }
                            }
                            break;

                        case BOOLEAN:
                            if (fieldType == Boolean.class || fieldType == boolean.class) {
                                field.set(instance, cell.getBooleanCellValue());
                            } else if (fieldType == String.class) {
                                field.set(instance, String.valueOf(cell.getBooleanCellValue()));
                            } else {
                                field.set(instance, null); // Cannot map boolean to non-boolean/non-string
                            }
                            break;

                        case FORMULA:
                            // Attempt to evaluate formula and map based on its result type
                            try {
                                switch (cell.getCachedFormulaResultType()) {
                                    case STRING:
                                        String formulaStringValue = cell.getStringCellValue();
                                        if (formulaStringValue != null) {
                                            formulaStringValue = formulaStringValue.trim();
                                        }
                                        field.set(instance, formulaStringValue);
                                        break;
                                    case NUMERIC:
                                        if (DateUtil.isCellDateFormatted(cell)) {
                                            if (fieldType == LocalDateTime.class) {
                                                field.set(instance, cell.getLocalDateTimeCellValue());
                                            } else {
                                                field.set(instance, null); // Cannot map formula date to non-date field
                                            }
                                        } else if (fieldType == Integer.class || fieldType == int.class) {
                                            field.set(instance, (int) cell.getNumericCellValue());
                                        } else if (fieldType == Long.class || fieldType == long.class) {
                                            field.set(instance, (long) cell.getNumericCellValue());
                                        } else if (fieldType == Double.class || fieldType == double.class) {
                                            field.set(instance, cell.getNumericCellValue());
                                        } else if (fieldType == String.class) {
                                            field.set(instance, String.valueOf(cell.getNumericCellValue()));
                                        } else {
                                            field.set(instance, cell.getNumericCellValue());
                                        }
                                        break;
                                    case BOOLEAN:
                                        if (fieldType == Boolean.class || fieldType == boolean.class) {
                                            field.set(instance, cell.getBooleanCellValue());
                                        } else if (fieldType == String.class) {
                                            field.set(instance, String.valueOf(cell.getBooleanCellValue()));
                                        } else {
                                            field.set(instance, null);
                                        }
                                        break;
                                    case ERROR:
                                    case BLANK: // Though handled by outer BLANK check, good for formula specifics
                                    case _NONE: // No cell type
                                    default:
                                        if (!fieldType.isPrimitive()) {
                                            field.set(instance, null);
                                        }
                                        break;
                                }
                            } catch (Exception e) {
                                System.err.println("WARN: Could not evaluate formula in cell (Row " + row.getRowNum() + ", Col " + i + "). Setting to null. Error: " + e.getMessage());
                                if (!fieldType.isPrimitive()) {
                                    field.set(instance, null);
                                }
                            }
                            break;

                        default:
                            // For ERROR, _NONE, etc., set to null for non-primitive fields
                            if (!fieldType.isPrimitive()) {
                                field.set(instance, null);
                            }
                            break;
                    }
                }
            }
            return instance;
        } catch (Exception e) {
            // Include row number for better debugging in the calling service
            throw new RuntimeException("Failed to map row " + (row != null ? row.getRowNum() + 1 : "N/A") + " to class " + clazz.getSimpleName() + ". Error: " + e.getMessage(), e);
        }
    }
}
