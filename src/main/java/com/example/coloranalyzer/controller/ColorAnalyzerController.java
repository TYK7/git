package com.example.coloranalyzer.controller;

import com.example.coloranalyzer.service.AnalyzedColor;
import com.example.coloranalyzer.service.ColorAnalyzerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/colors")
public class ColorAnalyzerController {

    private final ColorAnalyzerService colorAnalyzerService;

    @Autowired
    public ColorAnalyzerController(ColorAnalyzerService colorAnalyzerService) {
        this.colorAnalyzerService = colorAnalyzerService;
    }

    @GetMapping("/analyze")
    public ResponseEntity<List<AnalyzedColor>> analyzeUrl(@RequestParam String url) {
        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Collections.singletonList(new AnalyzedColor("Error", "URL parameter is required.", 0L))
            );
        }

        try {
            List<AnalyzedColor> analyzedColors = colorAnalyzerService.getAnalyzedColors(url);

            if (analyzedColors.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            // Check for specific error messages from the service
            // Based on previous task, service returns singletonList with "Error" in hexColor for known errors
            if (analyzedColors.size() == 1 && "Error".equals(analyzedColors.get(0).getHexColor())) {
                String errorMessage = analyzedColors.get(0).getCategory(); // The error message is in the category field
                if (errorMessage.startsWith("Invalid argument")) {
                    return ResponseEntity.badRequest().body(analyzedColors);
                } else if (errorMessage.startsWith("Error processing URL")) {
                     // This could be a 4xx or 5xx depending on the root cause.
                     // For now, let's treat it as a general server-side issue if it's not an invalid argument.
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(analyzedColors);
                }
                 // Fallback for other "Error" types from service
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(analyzedColors);
            }

            return ResponseEntity.ok(analyzedColors);

        } catch (Exception e) {
            // Catch unexpected exceptions from the service or within the controller
            System.err.println("Unexpected error in analyzeUrl endpoint for URL: " + url + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Collections.singletonList(new AnalyzedColor("Error", "An unexpected server error occurred.", 0L))
            );
        }
    }
}
