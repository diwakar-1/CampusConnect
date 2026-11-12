package com.campusconnect.controller;

import com.campusconnect.service.FeedbackService;
import com.campusconnect.service.RegistrationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    private final RegistrationService registrationService;
    private final FeedbackService feedbackService;

    public ReportController(RegistrationService registrationService, FeedbackService feedbackService) {
        this.registrationService = registrationService;
        this.feedbackService = feedbackService;
    }

    @GetMapping("/registrations.csv")
    public ResponseEntity<byte[]> downloadRegistrationsCsv(@RequestParam(value = "eventId", required = false) Long eventId) {
        String csvData = registrationService.exportRegistrationsCsv(eventId);
        byte[] bytes = csvData.getBytes(StandardCharsets.UTF_8);

        String filename = (eventId != null) ? "event-" + eventId + "-registrations.csv" : "all-campusconnect-registrations.csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping("/feedback.csv")
    public ResponseEntity<byte[]> downloadFeedbackCsv(@RequestParam(value = "eventId", required = false) Long eventId) {
        String csvData = feedbackService.exportFeedbackCsv(eventId);
        byte[] bytes = csvData.getBytes(StandardCharsets.UTF_8);

        String filename = (eventId != null) ? "event-" + eventId + "-feedbacks.csv" : "all-campusconnect-feedbacks.csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }
}

