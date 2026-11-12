package com.campusconnect.controller;

import com.campusconnect.entity.Certificate;
import com.campusconnect.entity.CertificateType;
import com.campusconnect.entity.Event;
import com.campusconnect.service.CertificateService;
import com.campusconnect.service.EventService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CertificateController {

    private final CertificateService certificateService;
    private final EventService eventService;

    public CertificateController(CertificateService certificateService, EventService eventService) {
        this.certificateService = certificateService;
        this.eventService = eventService;
    }

    @GetMapping("/admin/certificates")
    public String adminCertificates(@RequestParam(value = "eventId", required = false) Long eventId, Model model) {
        List<Event> events = eventService.getAllEvents();
        List<Certificate> certificates = (eventId != null)
                ? certificateService.getEventCertificates(eventId)
                : List.of();

        model.addAttribute("events", events);
        model.addAttribute("selectedEventId", eventId);
        model.addAttribute("certificates", certificates);
        model.addAttribute("certTypes", CertificateType.values());

        return "admin/certificates";
    }

    @PostMapping("/admin/certificates/issue/{regId}")
    public String issueSingleCertificate(@PathVariable("regId") Long regId,
                                         @RequestParam(value = "type", defaultValue = "PARTICIPATION") CertificateType type,
                                         @RequestParam(value = "signatory", required = false) String signatory,
                                         @RequestParam("eventId") Long eventId,
                                         RedirectAttributes redirectAttributes) {
        try {
            Certificate cert = certificateService.issueCertificate(regId, type, signatory);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Certificate issued successfully for " + cert.getStudentName() + " (" + cert.getCertificateNumber() + ")");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/events/" + eventId;
    }

    @PostMapping("/admin/certificates/bulk")
    public String bulkIssueCertificates(@RequestParam("eventId") Long eventId,
                                        @RequestParam(value = "type", defaultValue = "PARTICIPATION") CertificateType type,
                                        @RequestParam(value = "signatory", required = false) String signatory,
                                        RedirectAttributes redirectAttributes) {
        try {
            List<Certificate> generated = certificateService.bulkIssueCertificates(eventId, type, signatory);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Bulk generation completed: " + generated.size() + " new certificates created!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/certificates?eventId=" + eventId;
    }

    @GetMapping("/certificates/download/{id}")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable("id") Long id) {
        Certificate cert = certificateService.getCertificateById(id);
        byte[] pdfBytes = certificateService.generatePdf(id);

        String filename = "Certificate-" + cert.getCertificateNumber() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/certificates/verify/{token}")
    public String verifyCertificate(@PathVariable("token") String token, Model model) {
        try {
            Certificate certificate = certificateService.getCertificateByToken(token);
            model.addAttribute("certificate", certificate);
            model.addAttribute("isValid", true);
        } catch (Exception e) {
            model.addAttribute("isValid", false);
            model.addAttribute("token", token);
        }
        return "certificates/verify";
    }
}

