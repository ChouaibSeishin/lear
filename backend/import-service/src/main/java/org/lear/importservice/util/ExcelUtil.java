package org.lear.importservice.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtil {

    public static <T> List<T> readSheet(MultipartFile file, String sheetName, Class<T> clazz) {
        List<T> resultList = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                // Throw an IllegalArgumentException for not found sheets as per your current design
                throw new IllegalArgumentException("Sheet '" + sheetName + "' not found in the Excel file.");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                // If header row is missing, consider the sheet effectively empty for data processing
                System.err.println("WARN: Sheet '" + sheetName + "' is empty or missing a header row. No data will be processed.");
                return resultList;
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    headers.add(cell.getStringCellValue().trim());
                } else {
                    // Important: Add an empty string or null for blank/non-string header cells
                    // ExcelRowMapper relies on headers.size() matching cell index
                    headers.add("");
                }
            }

            // Iterate through data rows (starting from the second row, index 1)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row dataRow = sheet.getRow(i);
                if (dataRow == null) {
                    continue; // Skip entirely blank rows
                }

                // Delegate to ExcelRowMapper to map the current dataRow
                T obj = ExcelRowMapper.mapRow(dataRow, headers, clazz);
                resultList.add(obj);
            }

        } catch (Exception e) {
            // Catch general exceptions during file/sheet reading
            System.err.println("ERROR: Failed to read Excel sheet '" + sheetName + "': " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to read Excel sheet: " + sheetName, e);
        }

        return resultList;
    }

    // Existing helper methods, kept as is
    public static String getStringValue(Row row, int index) {
        Cell cell = row.getCell(index);
        return cell == null || cell.getCellType() == CellType.BLANK ? null : cell.getStringCellValue().trim();
    }

    public static Boolean getBooleanValue(Row row, int intdex) {
        Cell cell = row.getCell(intdex);
        return cell != null && cell.getCellType() == CellType.BOOLEAN ? cell.getBooleanCellValue() : null;
    }

    public static Integer getIntValue(Row row, int index) {
        Cell cell = row.getCell(index);
        return cell == null || cell.getCellType() != CellType.NUMERIC ? null : (int) cell.getNumericCellValue();
    }

    public static Long getLongValue(Row row, int index) {
        Cell cell = row.getCell(index);
        return cell == null || cell.getCellType() != CellType.NUMERIC ? null : (long) cell.getNumericCellValue();
    }

    public static String[] getCommaSeparatedValues(Row row, int index) {
        String value = getStringValue(row, index);
        return value == null || value.trim().isEmpty() ? new String[0] : value.split("\\s*,\\s*");
    }
}
