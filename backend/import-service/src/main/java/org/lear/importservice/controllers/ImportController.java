package org.lear.importservice.controllers;

import lombok.RequiredArgsConstructor;
import org.lear.importservice.dtos.ImportReport; // Import ImportReport
import org.lear.importservice.service.ImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
public class ImportController {
    private final ImportService importService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/excel")
    public ResponseEntity<Map<String, ImportReport>> importExcel(@RequestParam("file") MultipartFile file) {
        Map<String, ImportReport> report = importService.importExcel(file);
        return ResponseEntity.ok(report);
    }
}
